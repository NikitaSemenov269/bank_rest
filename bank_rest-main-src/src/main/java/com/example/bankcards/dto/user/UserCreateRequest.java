package com.example.bankcards.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static com.example.bankcards.util.ValidationMessages.CARDHOLDER_NAME_NOT_BLANK;
import static com.example.bankcards.util.ValidationMessages.PASSWORD_PATTERN;

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

    @NotBlank(message = "Пароль обязательное поле.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,50}$",
            message = PASSWORD_PATTERN)
    private String password;
}
