package com.example.bankcards.controller;

import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.dto.user.UserUpdateRequest;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AdminControllerUsers.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerUsersTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getUser_ShouldReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = UserResponse.builder()
                .id(userId)
                .name("Vasya")
                .build();

        when(userService.findById(userId)).thenReturn(response);

        mockMvc.perform(get("/api/admin/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vasya"));

        verify(userService).findById(userId);
    }

    @Test
    void getUserByLogin_ShouldReturnOk() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .name("Vasya")
                .build();

        when(userService.findByLogin("vasya123")).thenReturn(response);

        mockMvc.perform(get("/api/admin/users/by-login/{login}", "vasya123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vasya"));

        verify(userService).findByLogin("vasya123");
    }

    @Test
    void updateUser_ShouldReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        UserUpdateRequest request = UserUpdateRequest.builder()
                .name("New Name")
                .email("new@mail.ru")
                .build();

        UserResponse response = UserResponse.builder()
                .id(userId)
                .name("New Name")
                .build();

        when(userService.updateUser(eq(userId), any())).thenReturn(response);

        mockMvc.perform(put("/api/admin/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).updateUser(eq(userId), any());
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/admin/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }

    @Test
    void changeRole_ShouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(userService).changeRole(userId);

        mockMvc.perform(put("/api/admin/users/{id}/role", userId))
                .andExpect(status().isNoContent());

        verify(userService).changeRole(userId);
    }
}