package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static com.example.bankcards.util.ValidationMessages.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardUpdateRequest {
    @NotBlank(message = CARDHOLDER_NAME_NOT_BLANK)
    @Pattern(regexp = "[a-zA-Zа-яА-ЯёЁ\\s-]{1,100}",
            message = CARDHOLDER_NAME_PATTERN)
    private String cardholderName;

    @NotBlank(message = "Период действия карты обязательное поле.")
    @Pattern(regexp = "\\d{2}\\.\\d{2}\\.\\d{4}",
            message = VALIDITY_PERIOD_PATTERN)
    private String validityPeriod;
}
