package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static com.example.bankcards.util.ValidationMessages.CARDHOLDER_NAME_NOT_BLANK;
import static com.example.bankcards.util.ValidationMessages.CARDHOLDER_NAME_PATTERN;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    @NotBlank(message = CARDHOLDER_NAME_NOT_BLANK)
    @Pattern(regexp = "[a-zA-Zа-яА-ЯёЁ\\s-]{1,100}",
            message = CARDHOLDER_NAME_PATTERN)
    private String name;
}
