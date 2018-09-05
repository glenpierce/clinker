package com.vince.retailmanager.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vince.retailmanager.entity.User;
import com.vince.retailmanager.service.UserService;
import com.vince.retailmanager.service.businessService.ApplicationTestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationTestConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createUserSuccess() throws Exception {
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEnabled(true);
        ObjectMapper mapper = new ObjectMapper();
        String newUserJSON = mapper.writeValueAsString(user);
        RequestBuilder requestBuilder = post("/users")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .content(newUserJSON)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .with(csrf());
        ;
        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated());

    }

    @Test
    public void createUserUnauthorized() throws Exception {
        RequestBuilder requestBuilder = post("/users")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .with(csrf());
        ;
        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isUnauthorized());

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createUserError() throws Exception {
        User user = new User();
        user.setPassword("password");
        user.setEnabled(true);
        ObjectMapper mapper = new ObjectMapper();
        String newUserJSON = mapper.writeValueAsString(user);
        RequestBuilder requestBuilder = post("/users")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .content(newUserJSON)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .with(csrf());
        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());

    }

}