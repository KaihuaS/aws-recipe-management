package app.service;

import app.dao.ImageRepository;
import app.dao.NutritionInformationRepository;
import app.dao.OrderedListRepository;
import app.dao.RecipeRepository;
import app.model.Image;
import app.model.NutritionInformation;
import app.model.OrderedList;
import app.model.Recipe;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class RecipeService {


    @Autowired
    private NutritionInformationRepository nir;
    @Autowired
    private OrderedListRepository olr;
    @Autowired
    private RecipeRepository rr;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private SNSService SNSService;
    @Autowired
    private ImageRepository imageRepository;
    @Value("${cloud.aws.domain.name}")
    private String domainName;

    public Recipe createRecipie(Recipe recipe, HttpServletResponse response) throws IOException {
        //Set Recipie information
        if (!recipe.check()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "wrong parameters");
            return null;
        }
        recipe.createBasicInfo();

        long startTime = System.currentTimeMillis();
        rr.save(recipe);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("database save recipe",endTime-startTime);

        //Set Nutrition information
        if (recipe.getNutrition_information() != null) {
            NutritionInformation ni = recipe.getNutrition_information();

            if (StringUtils.isEmpty(ni.getCalories()) || StringUtils.isEmpty(ni.getCarbohydrates_in_grams())
                    || StringUtils.isEmpty(ni.getCholesterol_in_mg()) || StringUtils.isEmpty(ni.getProtein_in_grams()) || StringUtils.isEmpty(ni.getSodium_in_mg())) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "wrong parameter");
                return null;
            } else {
                ni.setId(UUID.randomUUID().toString());
                ni.setRecipie_id(recipe.getId());

                nir.save(ni);
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "wrong parameter");
            return null;
        }

        //Set OrderedList
        for (OrderedList ol : recipe.getSteps()) {
            ol.setId(UUID.randomUUID().toString());
            ol.setRecipie_id(recipe.getId());

            olr.save(ol);
        }

        response.setStatus(HttpServletResponse.SC_CREATED);

        return recipe;
    }

    public Recipe getRecipie(String id, HttpServletResponse response) throws IOException {

        long startTime = System.currentTimeMillis();
        Recipe r = rr.findById(id);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("database find by recipe id",endTime-startTime);


        if (r == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such recipe");
            return null;
        }
        //get ingredients
        r.setIngredients(Arrays.asList(r.getDatabase_Ingredients().split(";")));

        //Get steps
        List<OrderedList> steps = olr.findByRecipieId(id);
        r.setSteps(steps);

        //Get Nutrition information
        List<NutritionInformation> nlist = nir.findByRecipieId(id);
        r.setNutrition_information(nlist.get(0));

        List<Image> imageList = imageRepository.findByRecipieId(id);
        if (imageList.size() != 0) {
            r.setImage(imageList.get(0));
        }
        return r;
    }

    public Recipe deleteRecipie(String id, String userid, HttpServletResponse response) throws IOException {
        Recipe r = this.getRecipie(id, response);

        if (r == null) {
            return null;
        }

        if (!userid.equals(r.getAuthor_id())) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Wrong author");
            return null;
        }

        olr.deleteByRecipieId(r.getId());
        nir.deleteByRecipieId(r.getId());
        imageRepository.deleteByRecipieId(r.getId());

        long startTime = System.currentTimeMillis();
        rr.delete(r);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("database delete recipe",endTime-startTime);

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return null;
    }

    public Recipe updateRecipie(String id, String userid, Recipe recipe, HttpServletResponse response) throws IOException {
        Recipe r = this.getRecipie(id, response);


        if (r == null) {
            return null;
        }

        if (!recipe.check()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "wrong parameters");
            return null;
        }
        r.setCook_time_in_min(recipe.getCook_time_in_min());
        r.setPrep_time_in_min(recipe.getPrep_time_in_min());
        r.setCuisine(recipe.getCuisine());
        r.setIngredients(recipe.getIngredients());
        r.setServings(recipe.getServings());
        r.setTitle(recipe.getTitle());

        r.updateBasicInfo();

        if (!userid.equals(r.getAuthor_id())) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "wrong author");
            return null;
        }

        if (recipe.getNutrition_information() != null) {
            //Set Nutrition information
            NutritionInformation ni = recipe.getNutrition_information();

            NutritionInformation oni = r.getNutrition_information();
            if (StringUtils.isEmpty(ni.getCalories()) || StringUtils.isEmpty(ni.getCarbohydrates_in_grams())
                    || StringUtils.isEmpty(ni.getCholesterol_in_mg()) || StringUtils.isEmpty(ni.getProtein_in_grams()) || StringUtils.isEmpty(ni.getSodium_in_mg())) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "wrong parameter");
                return null;
            } else {
                oni.setCalories(ni.getCalories());
                oni.setCarbohydrates_in_grams(ni.getCarbohydrates_in_grams());
                oni.setCholesterol_in_mg(ni.getCholesterol_in_mg());
                oni.setProtein_in_grams(ni.getProtein_in_grams());
                oni.setSodium_in_mg(ni.getSodium_in_mg());
            }
            nir.save(oni);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "wrong parameter");
            return null;
        }

        if (recipe.getSteps() != null && recipe.getSteps().size() > 0) {

            //delete old steps
            olr.deleteByRecipieId(r.getId());

            //Set New steps
            for (OrderedList ol : recipe.getSteps()) {

                ol.setId(UUID.randomUUID().toString());
                ol.setRecipie_id(r.getId());
                olr.save(ol);
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "wrong parameter");
            return null;
        }

        long startTime = System.currentTimeMillis();
        rr.save(r);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("database update recipe",endTime-startTime);

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return r;
    }

    public Recipe getLatest(HttpServletResponse response) throws IOException {
        List<Recipe> recipes = rr.getLatest();
        if (recipes.size() > 0) {
            Recipe r = recipes.get(0);
            return this.getRecipie(r.getId(), response);
        } else {
            return null;
        }
    }

    public Image attachImage(String id, MultipartFile image, HttpServletResponse response) throws IOException {
        Recipe r = this.getRecipie(id, response);
        if (r == null) {
            return null;
        }
        String fileExtension = FilenameUtils.getExtension(image.getOriginalFilename());
        String filename = UUID.randomUUID().toString() + "." + fileExtension;
        if ("jpeg".equals(fileExtension) || "png".equals(fileExtension) || "jpg".equals(fileExtension)) {
            File convFile = new File(filename);
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(image.getBytes());
            fos.close();

            Image i = s3Service.uploadFileTos3bucket(filename, convFile);
            i.setRecipeId(r.getId());

            long startTime = System.currentTimeMillis();
            imageRepository.save(i);
            long endTime = System.currentTimeMillis();
            MyStatsDClient.getStatsDClient().recordExecutionTime("database save image",endTime-startTime);

            r.setImage(i);
            response.setStatus(HttpServletResponse.SC_CREATED);
            convFile.delete();

            return i;
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }


    }

    public Recipe deleteImage(String id, String userid, String imageId, HttpServletResponse response) throws IOException {
        Recipe r = this.getRecipie(id, response);
        if (r == null) {
            return null;
        }

        if (!userid.equals(r.getAuthor_id())) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Wrong author");
            return null;
        }

        Image i = imageRepository.findById(imageId);
        if (i == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        if (!i.getRecipeId().equals(id)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        s3Service.deleteFileFromS3Bucket(i.getUrl());

        long startTime = System.currentTimeMillis();
        imageRepository.delete(i);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("database delete image",endTime-startTime);

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return r;
    }

    public Image getImage(String id, String imageId, HttpServletResponse response) throws IOException {
        Recipe r = this.getRecipie(id, response);
        if (r == null) {
            return null;
        }

        long startTime = System.currentTimeMillis();
        Image i = imageRepository.findById(imageId);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("database save recipe",endTime-startTime);
        
        if (i == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        if (!i.getRecipeId().equals(id)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        return i;
    }

	public void getMyRecipes(String id, String username, String token, HttpServletResponse response) {
        List<Recipe> recipes = this.rr.findByUserId(id);
        
        String urls = "";
        for (Recipe r : recipes){
            urls = urls+"\n"+domainName+"/v1/recipe/"+r.getId();
        }
        
        SNSService.publishMessage("email request", urls, username, token);

	}

}
