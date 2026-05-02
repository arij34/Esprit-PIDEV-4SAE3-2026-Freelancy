package tn.esprit.challengeservice.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.challengeservice.clients.UserDto;
import tn.esprit.challengeservice.clients.UserServiceClient;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserTestController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceClient userServiceClient;

    @Test
    void pingUserService_shouldReturnOk() throws Exception {
        when(userServiceClient.ping()).thenReturn(Map.of("status", "ok"));

        mockMvc.perform(get("/test-user-service/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void getCurrentUser_shouldReturnUser() throws Exception {
        UserDto user = new UserDto(1L, "Test", "User", "test@example.com", "FREELANCER", true);
        when(userServiceClient.getCurrentUser("Bearer tkn")).thenReturn(user);

        mockMvc.perform(get("/test-user-service/me").header("Authorization", "Bearer tkn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
