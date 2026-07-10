package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TransferRequest {
    @NotNull(message = "Айди карты-получателя обязательное поле.")
    private UUID receivingCardId;
    @NotNull(message = "Айди карты-отправителя обязательное поле.")
    private UUID givingCardId;
    @NotNull @Positive
    private BigDecimal amount;
}
