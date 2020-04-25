package app.service;

import app.dao.UserRepository;
import app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

/**
 * @author yi
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User saveUser(User u, HttpServletResponse response) throws IOException {
        if (StringUtils.isEmpty(u.getFirst_name()) || StringUtils.isEmpty(u.getLast_name())
                || StringUtils.isEmpty(u.getEmail_address()) || StringUtils.isEmpty(u.getPassword())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "empty parameter");
            return null;
        } else {
            u = new User(u.getFirst_name(), u.getLast_name(), u.getPassword(), u.getEmail_address());
        }

        if (!u.getPassword().matches("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "illegal password");
            return null;
        }
        if (!u.getEmail_address().matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|" +
                "\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@" +
                "(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?" +
                "|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]" +
                ":(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "illegal email address");
            return null;
        }
        if (userRepository.findByUsername(u.getEmail_address()).size() > 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "user exists");
            return null;
        }
        u.setPassword(BCrypt.hashpw(u.getPassword(), BCrypt.gensalt()));

        long startTime = System.currentTimeMillis();
        userRepository.save(u);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("database save User",endTime-startTime);

        response.setStatus(HttpServletResponse.SC_CREATED);
        return u;
    }

    public User getUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            try {
                if ("".equals(values[0])) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return null;
                }
                
                long startTime = System.currentTimeMillis();
                User currentUser = userRepository.findByUsername(values[0]).get(0);
                long endTime = System.currentTimeMillis();
                MyStatsDClient.getStatsDClient().recordExecutionTime("database find User by name",endTime-startTime);
                if (BCrypt.checkpw(values[1], currentUser.getPassword())) {
                    return currentUser;
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return null;
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }
        }

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return null;
    }


    public User updateUser(User user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = getUser(request, response);
        if (currentUser==null) {
            return null;
        }

        // No illegal parameters
        if (!StringUtils.isEmpty(user.getEmail_address()) || !StringUtils.isEmpty(user.getAccount_created()) ||
                !StringUtils.isEmpty(user.getAccount_updated()) || !StringUtils.isEmpty(user.getId())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "wrong parameter");
            return null;
        }

        currentUser.setAccount_updated(new Date());
        if (!StringUtils.isEmpty(user.getFirst_name())){
            currentUser.setFirst_name(user.getFirst_name());
        }
        if (!StringUtils.isEmpty(user.getLast_name())){
            currentUser.setLast_name(user.getLast_name());
        }
        if (!StringUtils.isEmpty(user.getPassword())){
            if (user.getPassword().matches("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}")) {
                currentUser.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "illegal password");
                return null;
            }
        }

        long startTime = System.currentTimeMillis();
        userRepository.save(currentUser);
        long endTime = System.currentTimeMillis();
        MyStatsDClient.getStatsDClient().recordExecutionTime("database update User",endTime-startTime);

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return currentUser;
    }

}
