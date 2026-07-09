package com.example.bankcards.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static com.example.bankcards.util.ValidationMessages.LOGIN_PATTERN;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeLoginRequest {
    @NotBlank(message = "Новый логин обязательное поле.")
    @Pattern(regexp = "^[a-zA-Z0-9@#$%^&+=!_-]{5,50}$",
            message = LOGIN_PATTERN)
    private String newLogin;
}
