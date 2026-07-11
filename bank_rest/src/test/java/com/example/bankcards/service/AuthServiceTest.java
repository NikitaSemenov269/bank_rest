package com.example.bankcards.service;

import com.example.bankcards.dto.token.LoginRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @InjectMocks
    private AuthService authService;

    @Test
    void login_ShouldThrowException_WhenWrongPassword() {
        LoginRequest request = new LoginRequest("vasya123", "wrong");
        UserResponse user = UserResponse.builder().id(UUID.randomUUID()).role(Role.ROLE_USER).build();

        when(userService.findByLogin("vasya123")).thenReturn(user);
        doThrow(new NotFoundException("Неверный пароль"))
                .when(userService).checkPassword("wrong", user.getId());

        assertThrows(NotFoundException.class, () -> authService.login(request));
        verify(jwtTokenProvider, never()).generateAccessToken(any(), any());
    }
}
