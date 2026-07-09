package com.example.bankcards.dto.token;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static com.example.bankcards.util.ValidationMessages.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {
    @NotBlank(message = "Старый логин обязательное поле.")
    @Pattern(regexp = "^[a-zA-Z0-9@#$%^&+=!_-]{5,50}$",
            message = LOGIN_PATTERN)
    private String login;

    @NotBlank(message = "Пароль обязательное поле.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,50}$",
            message = PASSWORD_PATTERN)
    private String password;
}
