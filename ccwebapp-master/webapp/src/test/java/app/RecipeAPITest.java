package app;

import app.dao.NutritionInformationRepository;
import app.dao.OrderedListRepository;
import app.dao.RecipeRepository;
import app.dao.UserRepository;
import app.model.NutritionInformation;
import app.model.OrderedList;
import app.model.Recipe;
import app.model.User;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    Application.class, 
    H2TestProfileJPAConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RecipeAPITest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RecipeRepository recipeRepository;
    @MockBean
    private OrderedListRepository orderedListRepository;
    @MockBean
    private NutritionInformationRepository nutritionInformationRepository;

    @Test
    public void saveRecipe() throws Exception {

        // Create test User
        User user = new User("John", "Doe", "Pa$$w0rd", "john.doe@example.com");
        List<User> users = new ArrayList<>();
        String pwd = BCrypt.hashpw("Pa$$w0rd", BCrypt.gensalt());
        User databaseUser = new User("John", "Doe", pwd, "john.doe@example.com");
        users.add(databaseUser);
        Mockito.when(userRepository.findByUsername("john.doe@example.com")).thenReturn(users);

        // Create test recipe
        List<String> ingredients = new ArrayList<>();
        ingredients.add("4 ounces linguine pasta");
        ingredients.add("3 teaspoons Cajun seasoning");
        List<OrderedList> steps = new ArrayList<>();
        OrderedList o = new OrderedList();
        o.setItems("some thing");
        o.setPosition(1);
        steps.add(o);
        NutritionInformation nutrition_information = new NutritionInformation();
        nutrition_information.setSodium_in_mg(20);
        nutrition_information.setProtein_in_grams(20);
        nutrition_information.setCholesterol_in_mg(50.3f);
        nutrition_information.setProtein_in_grams(60.3f);
        nutrition_information.setCarbohydrates_in_grams(60.2f);
        nutrition_information.setCalories(20);
        Recipe r = new Recipe(10, 20, "title", "cuisine", 2,ingredients , steps, nutrition_information);

        mockMvc.perform(post("/v1/recipe").contentType(MediaType.APPLICATION_JSON_UTF8).content(JSON.toJSONString(r)).with(httpBasic("john.doe@example.com", "Pa$$w0rd")))
                .andDo(print())
                .andExpect(status().isCreated());

        r.setCook_time_in_min(2);
        mockMvc.perform(post("/v1/recipe").contentType(MediaType.APPLICATION_JSON_UTF8).content(JSON.toJSONString(r)).with(httpBasic("john.doe@example.com", "Pa$$w0rd")))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}
