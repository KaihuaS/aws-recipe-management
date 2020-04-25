package app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class Recipe {
    //id*	string($uuid)
    //example: d290f1ee-6c54-4b01-90e6-d701748f0851
    //readOnly: true
    //created_ts	string($date-time)
    //example: 2016-08-29T09:12:33.001Z
    //readOnly: true
    //updated_ts	string($date-time)
    //example: 2016-08-29T09:12:33.001Z
    //readOnly: true
    //author_id*	string($uuid)
    //example: d290f1ee-6c54-4b01-90e6-d701748f0851
    //readOnly: true
    //cook_time_in_min*	integer
    //example: 15
    //multipleOf: 5
    //prep_time_in_min*	integer
    //example: 15
    //multipleOf: 5
    //total_time_in_min	integer
    //example: 15
    //multipleOf: 5
    //readOnly: true
    //title*	string
    //example: Creamy Cajun Chicken Pasta
    //cusine*	string
    //example: Italian
    //servings*	integer
    //example: 2
    //minimum: 1
    //maximum: 5

    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date created_ts;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date updated_ts;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String author_id;
    private int cook_time_in_min;
    private int prep_time_in_min;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int total_time_in_min;

    private String title;
    private String cuisine;
    private int servings;
    @Transient
    private Image image;

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Recipe() {
        super();
    }

    public Recipe(int cook_time_in_min, int prep_time_in_min, String title, String cuisine, int servings, List<String> ingredients, List<OrderedList> steps, NutritionInformation nutrition_information) {
        this.id = UUID.randomUUID().toString();
        this.cook_time_in_min = cook_time_in_min;
        this.prep_time_in_min = prep_time_in_min;
        this.title = title;
        this.cuisine = cuisine;
        this.servings = servings;
        this.ingredients = ingredients;
        this.steps = steps;
        this.nutrition_information = nutrition_information;
    }

    @Transient
    private List<String> ingredients;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String database_Ingredients;

    @Transient
    private List<OrderedList> steps;

    @Transient
    private NutritionInformation nutrition_information;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreated_ts() {
        return created_ts;
    }

    public void setCreated_ts(Date created_ts) {
        this.created_ts = created_ts;
    }

    public Date getUpdated_ts() {
        return updated_ts;
    }

    public void setUpdated_ts(Date updated_ts) {
        this.updated_ts = updated_ts;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public int getCook_time_in_min() {
        return cook_time_in_min;
    }

    public void setCook_time_in_min(int cook_time_in_min) {
        this.cook_time_in_min = cook_time_in_min;
    }

    public int getPrep_time_in_min() {
        return prep_time_in_min;
    }

    public void setPrep_time_in_min(int prep_time_in_min) {
        this.prep_time_in_min = prep_time_in_min;
    }

    public int getTotal_time_in_min() {
        return total_time_in_min;
    }

    public void setTotal_time_in_min(int total_time_in_min) {
        this.total_time_in_min = total_time_in_min;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<OrderedList> getSteps() {
        return steps;
    }

    public void setSteps(List<OrderedList> steps) {
        this.steps = steps;
    }

    public NutritionInformation getNutrition_information() {
        return nutrition_information;
    }

    public void setNutrition_information(NutritionInformation nutrition_information) {
        this.nutrition_information = nutrition_information;
    }

    public String getDatabase_Ingredients() {
        return database_Ingredients;
    }

    public void setDatabase_Ingredients(String database_Ingredients) {
        this.database_Ingredients = database_Ingredients;
    }

    public void createBasicInfo() {
        id = UUID.randomUUID().toString();
        total_time_in_min = cook_time_in_min + prep_time_in_min;
        created_ts = new Date();
        updated_ts = new Date();
        database_Ingredients = "";
        for (String s : ingredients) {
            database_Ingredients += s + ";";
        }
        database_Ingredients = database_Ingredients.substring(0, database_Ingredients.length() - 1);
    }

    public void updateBasicInfo() {
        total_time_in_min = cook_time_in_min + prep_time_in_min;
        updated_ts = new Date();
        database_Ingredients = "";
        for (String s : ingredients) {
            database_Ingredients += s + ";";
        }
        database_Ingredients = database_Ingredients.substring(0, database_Ingredients.length() - 1);
    }

    public boolean check() {
        if (cook_time_in_min % 5 != 0 || prep_time_in_min % 5 != 0) {
            return false;
        }
        if (servings > 5 || servings < 1) {
            return false;
        }
        if (steps.size() == 0) {
            return false;
        }
        for (OrderedList ol : steps) {
            if (ol.getPosition() < 1) {
                return false;
            }
        }
        if (StringUtils.isEmpty(title) || StringUtils.isEmpty(cuisine)) {
            return false;
        }
        if (ingredients.size() == 0) {
            return false;
        }
        return true;
    }
}
