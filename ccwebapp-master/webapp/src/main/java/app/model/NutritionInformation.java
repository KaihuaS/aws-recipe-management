package app.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class NutritionInformation {
    //calories*	integer
    //example: 100
    //cholesterol_in_mg*	number($float)
    //example: 4
    //sodium_in_mg*	integer
    //example: 100
    //carbohydrates_in_grams*	number($float)
    //example: 53.7
    //protein_in_grams*	number($float)
    //example: 53.7

    @Id
    private String id;
    private String recipie_id;
    private int calories;
    private float cholesterol_in_mg;
    private int sodium_in_mg;
    private float carbohydrates_in_grams;
    private float protein_in_grams;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipie_id() {
        return recipie_id;
    }

    public void setRecipie_id(String recipie_id) {
        this.recipie_id = recipie_id;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public float getCholesterol_in_mg() {
        return cholesterol_in_mg;
    }

    public void setCholesterol_in_mg(float cholesterol_in_mg) {
        this.cholesterol_in_mg = cholesterol_in_mg;
    }

    public int getSodium_in_mg() {
        return sodium_in_mg;
    }

    public void setSodium_in_mg(int sodium_in_mg) {
        this.sodium_in_mg = sodium_in_mg;
    }

    public float getCarbohydrates_in_grams() {
        return carbohydrates_in_grams;
    }

    public void setCarbohydrates_in_grams(float carbohydrates_in_grams) {
        this.carbohydrates_in_grams = carbohydrates_in_grams;
    }

    public float getProtein_in_grams() {
        return protein_in_grams;
    }

    public void setProtein_in_grams(float protein_in_grams) {
        this.protein_in_grams = protein_in_grams;
    }
}
