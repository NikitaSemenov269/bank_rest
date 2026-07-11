package com.example.bankcards.dto.token;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh-токен обязателен")
    private String refreshToken;
}