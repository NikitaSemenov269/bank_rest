package com.example.bankcards.service;

import com.example.bankcards.dto.token.LoginRequest;
import com.example.bankcards.dto.token.TokenRefreshRequest;
import com.example.bankcards.dto.token.TokenResponse;
import com.example.bankcards.dto.user.UserCreateRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService service;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse register(UserCreateRequest request) {
        UserResponse userResponse = service.createNewUser(request);
        return buildTokenResponse(userResponse.getId(), userResponse.getRole());
    }

    public TokenResponse login(LoginRequest request) {
        UserResponse user = service.findByLogin(request.getLogin());
        service.checkPassword(request.getPassword(), user.getId());
        return buildTokenResponse(user.getId(), user.getRole());
    }

    // Обновление токенов по refresh-токену.
    public TokenResponse refresh(TokenRefreshRequest request) {
        UUID userId = jwtTokenProvider.validateRefreshTokenAndGetUserId(request.getRefreshToken());
        Role role = service.findRoleById(userId);
        return buildTokenResponse(userId, role);
    }

    // Собирает TokenResponse с access и refresh токенами.
    private TokenResponse buildTokenResponse(UUID userId, Role role) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .build();
    }
}
