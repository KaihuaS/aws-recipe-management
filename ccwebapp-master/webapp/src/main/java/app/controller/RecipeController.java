package app.controller;

import app.model.Image;
import app.model.Recipe;
import app.model.User;
import app.service.MyStatsDClient;
import app.service.RecipeService;
import app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping(value = "/v1/recipe")
public class RecipeController {
    @Autowired
    private RecipeService recipeService;
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Recipe getRecipie(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().incrementCounter("get recipe");
        Recipe dRecipe = recipeService.getRecipie(id, response);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("get recipe",endTime-startTime);
        return dRecipe;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Recipe updateRecipie(@PathVariable("id") String id, @RequestBody Recipe recipe, HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().incrementCounter("update recipe");
        User currentUser = userService.getUser(request, response);
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        Recipe newRecipe =  recipeService.updateRecipie(id, currentUser.getId(), recipe, response);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("update recipe",endTime-startTime);
        return newRecipe;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Recipe deleteRecipie(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().incrementCounter("delete recipe");
        User currentUser = userService.getUser(request, response);
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        Recipe dRecipe =  recipeService.deleteRecipie(id, currentUser.getId(), response);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("delete recipe",endTime-startTime);
        return dRecipe;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public Recipe createRecipie(@RequestBody Recipe recipe, HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().incrementCounter("create recipe");
        User currentUser = userService.getUser(request, response);
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        recipe.setAuthor_id(currentUser.getId());
        Recipe newRecipe = recipeService.createRecipie(recipe, response);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("create Recipe",endTime-startTime);
        return newRecipe;
    }

    @RequestMapping(value = "/{id}/image", method = RequestMethod.POST)
    public Image attachImage(@PathVariable("id") String id, @RequestPart(value = "recipeImage") MultipartFile recipeImage, HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().incrementCounter("attach image");
        User currentUser = userService.getUser(request, response);
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        Image newImage =  recipeService.attachImage(id, recipeImage, response);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("create user",endTime-startTime);
        return newImage;
    }

    @RequestMapping(value = "/{id}/image/{imageId}", method = RequestMethod.GET)
    public Image getImage(@PathVariable("id") String id, @PathVariable("imageId") String imageId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().incrementCounter("get image");
        Image dImage = recipeService.getImage(id, imageId, response);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("get image",endTime-startTime);
        return dImage;
    }

    @RequestMapping(value = "/{id}/image/{imageId}", method = RequestMethod.DELETE)
    public Recipe deleteImage(@PathVariable("id") String id, @PathVariable("imageId") String imageId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        MyStatsDClient.getStatsDClient().incrementCounter("delete image");
        User currentUser = userService.getUser(request, response);
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        long startTime = System.currentTimeMillis();
        Recipe newRecipe = recipeService.deleteImage(id, currentUser.getId(), imageId, response);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("delete image",endTime-startTime);
        return newRecipe;
    }

    
}
