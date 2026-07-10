package com.example.bankcards.controller;

import com.example.bankcards.dto.token.LoginRequest;
import com.example.bankcards.dto.token.TokenRefreshRequest;
import com.example.bankcards.dto.token.TokenResponse;
import com.example.bankcards.dto.user.UserCreateRequest;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturnCreated() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("Vasya")
                .login("vasya123")
                .email("vasya@mail.ru")
                .password("P@ssw0rd123")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("access-token-mock")
                .refreshToken("refresh-token-mock")
                .tokenType("Bearer")
                .expiresIn(600)
                .build();

        when(authService.register(any(UserCreateRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token-mock"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-mock"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(600));

        verify(authService).register(any(UserCreateRequest.class));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenNameBlank() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("")
                .login("vasya123")
                .email("vasya@mail.ru")
                .password("P@ssw0rd123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenInvalidEmail() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("Vasya")
                .login("vasya123")
                .email("not-an-email")
                .password("P@ssw0rd123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"weak", "12345678", "NoSpecialChar1"})
    void register_ShouldReturnBadRequest_WhenWeakPassword(String weakPassword) throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("Vasya")
                .login("vasya123")
                .email("vasya@mail.ru")
                .password(weakPassword)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void login_ShouldReturnOk() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .login("vasya123")
                .password("P@ssw0rd123")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("access-token-mock")
                .refreshToken("refresh-token-mock")
                .tokenType("Bearer")
                .expiresIn(600)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-mock"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-mock"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_ShouldReturnBadRequest_WhenLoginBlank() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .login("")
                .password("P@ssw0rd123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenPasswordBlank() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .login("vasya123")
                .password("")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void refresh_ShouldReturnOk() throws Exception {
        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .tokenType("Bearer")
                .expiresIn(600)
                .build();

        when(authService.refresh(any(TokenRefreshRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(600));

        verify(authService).refresh(any(TokenRefreshRequest.class));
    }

    @Test
    void refresh_ShouldReturnBadRequest_WhenRefreshTokenBlank() throws Exception {
        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken("")
                .build();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).refresh(any());
    }
}