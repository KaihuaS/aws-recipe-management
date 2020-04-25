package app;

import app.dao.UserRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        Application.class, 
        H2TestProfileJPAConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserAPITest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;


    @Test
    public void saveUser() throws Exception {
        User user = new User("John", "Doe", "Pa$$w0rd", "john.doe@example.com");
        String pwd = BCrypt.hashpw("Pa$$w0rd", BCrypt.gensalt());
        User databaseUser = new User("John", "Doe", pwd, "john.doe@example.com");
        Mockito.when(userRepository.save(user)).thenReturn(databaseUser);

        // Test save user service
        this.mockMvc.perform(post("/v1/user").contentType(MediaType.APPLICATION_JSON_UTF8).content(JSON.toJSONString(user)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

        List<User> users = new ArrayList<>();
        users.add(databaseUser);
        Mockito.when(userRepository.findByUsername("john.doe@example.com")).thenReturn(users);

        // Test get user service
        mockMvc.perform(get("/v1/user/self").with(httpBasic("john.doe@example.com", "Pa$$w0rd")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

        // Test update user service
        Map<String,String> userMap = new HashMap<>();
        userMap.put("first_name","Jane");
        mockMvc.perform(put("/v1/user/self").contentType(MediaType.APPLICATION_JSON_UTF8).content(JSON.toJSONString(userMap)).with(httpBasic("john.doe@example.com", "Pa$$w0rd")))
                .andDo(print())
                .andExpect(status().isNoContent());
    }


}
