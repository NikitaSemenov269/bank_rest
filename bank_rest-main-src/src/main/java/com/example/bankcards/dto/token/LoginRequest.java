package com.example.bankcards.dto.token;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static com.example.bankcards.util.ValidationMessages.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// Дублирует UserCreateRequest, но для возможности более простого масштабирования оставляю так.
public class LoginRequest {
    @NotBlank(message = CARDHOLDER_NAME_NOT_BLANK)
    @Pattern(regexp = "[a-zA-Zа-яА-ЯёЁ\\s-]{1,100}",
            message = CARDHOLDER_NAME_PATTERN)
    private String name;

    @NotBlank(message = "Пароль обязательное поле.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,50}$",
            message = PASSWORD_PATTERN)
    private String password;
}
