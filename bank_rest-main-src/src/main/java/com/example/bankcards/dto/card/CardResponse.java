package com.example.bankcards.dto.card;

import com.example.bankcards.entity.enums.CardStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardResponse {
    private UUID id;
    private String cardholderName;
    private String validityPeriod;
    private String maskedCardNumber;
    private String fullCardNumber;  // Только для владельца.
    private BigDecimal balance;   // Только для владельца.
    private CardStatus cardStatus;
}
