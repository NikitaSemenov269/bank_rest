package com.example.bankcards.controller;

import com.example.bankcards.dto.user.ChangeLoginRequest;
import com.example.bankcards.dto.user.ChangePasswordRequest;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserControllerUsers.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class UserControllerUsersTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void changeLogin_ShouldReturnNoContent() throws Exception {
        ChangeLoginRequest request = ChangeLoginRequest.builder()
                .newLogin("newLogin123")
                .build();

        doNothing().when(userService).changeLogin(any());

        mockMvc.perform(put("/api/user/users/change-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).changeLogin(any());
    }

    @Test
    void changePassword_ShouldReturnNoContent() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldP@ssw0rd")
                .newPassword("NewP@ssw0rd123")
                .build();

        doNothing().when(userService).changePassword(any());

        mockMvc.perform(put("/api/user/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(any());
    }
}