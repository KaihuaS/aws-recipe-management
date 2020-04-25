package app.controller;

import app.model.Recipe;
import app.model.User;
import app.service.MyStatsDClient;
import app.service.RecipeService;
import app.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping(value = "/v1")
public class LatestController {
    @Autowired
    private RecipeService recipeService;
    @Autowired
    private UserService userService;

    Logger logger = LoggerFactory.getLogger(LatestController.class);

    @RequestMapping(value = "/recipes", method = RequestMethod.GET)
    public Recipe getRecipie(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        long startTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().incrementCounter("get latest recipe");
        Recipe dRecipe =  recipeService.getLatest(response);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("get latest recipe",endTime-startTime);
        logger.debug("Get latest recipe controller");
        return dRecipe;
    }

    @RequestMapping(value = "/myrecipes", method = RequestMethod.POST)
    public void MyRecipes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = userService.getUser(request, response);
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final String authorization = request.getHeader("Authorization");
        String base64Credentials = authorization.substring("Basic".length()).trim();
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(credDecoded, StandardCharsets.UTF_8);

        recipeService.getMyRecipes(currentUser.getId(),currentUser.getEmail_address(),credentials,response);
    }
}
