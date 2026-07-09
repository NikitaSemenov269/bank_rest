package com.example.bankcards.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.UUID;

import static com.example.bankcards.util.ValidationMessages.CARDHOLDER_NAME_PATTERN;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    @Pattern(regexp = "[a-zA-Zа-яА-ЯёЁ\\s-]{1,100}",
            message = CARDHOLDER_NAME_PATTERN)
    private String name;

    @Email(message = "Некорректный формат email.")
    private String email;
}
