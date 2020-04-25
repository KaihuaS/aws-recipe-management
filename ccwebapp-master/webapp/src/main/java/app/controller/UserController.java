package app.controller;

import app.model.User;
import app.service.MyStatsDClient;
import app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yi
 */
@RestController
@RequestMapping(value = "/v1/user")
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/self", method = RequestMethod.GET)
    public User getUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().incrementCounter("get user");
        User dUser = userService.getUser(request, response);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("get user",endTime-startTime);
        return dUser;
    }

    @RequestMapping(value = "/self", method = RequestMethod.PUT)
    public User updateUser(@RequestBody User user,
                           HttpServletRequest request, HttpServletResponse response) throws IOException {
                            MyStatsDClient.getStatsDClient().incrementCounter("update user");
        long startTime = System.currentTimeMillis();
        User updateUser = userService.updateUser(user, request, response);
        long endTime = System.currentTimeMillis();
            MyStatsDClient.getStatsDClient().recordExecutionTime("update user",endTime-startTime);
        return updateUser;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public User createUser(@RequestBody User user,
                           HttpServletResponse response) throws IOException {
        try {
            long startTime = System.currentTimeMillis();
            MyStatsDClient.getStatsDClient().incrementCounter("create user");
            User newUser = userService.saveUser(user, response);
            long endTime = System.currentTimeMillis();
            MyStatsDClient.getStatsDClient().recordExecutionTime("create user",endTime-startTime);
            return newUser;
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "illegal parameter");
            return null;
        }
    }
}
