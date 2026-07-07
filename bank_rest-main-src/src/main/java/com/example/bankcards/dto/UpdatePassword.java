package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static com.example.bankcards.util.ValidationMessages.PASSWORD_PATTERN;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePassword {
    @NotBlank(message = "Старый пароль обязательное поле.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,50}$",
            message = PASSWORD_PATTERN)
    private String oldPassword;

    @NotBlank(message = "Новый пароль обязательное поле.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,50}$",
            message = PASSWORD_PATTERN)
    private String newPassword;
}
