package com.example.bankcards.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static com.example.bankcards.util.ValidationMessages.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequest {
    @NotBlank(message = CARDHOLDER_NAME_NOT_BLANK)
    @Pattern(regexp = "[a-zA-Zа-яА-ЯёЁ\\s-]{1,100}",
            message = "Имя владельца карты должно содержать минимум 1 символ, но не более 100.")
    private String name;

    @NotBlank(message = "Логин обязательное поле.")
    @Pattern(regexp = "^[a-zA-Z0-9@#$%^&+=!_-]{5,50}$",
            message = LOGIN_PATTERN)
    private String login;

    @NotBlank(message = "Email обязательное поле.")
    @Email(message = "Некорректный формат email.")
    private String email;

    @NotBlank(message = "Пароль обязательное поле.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,50}$",
            message = PASSWORD_PATTERN)
    private String password;
}
