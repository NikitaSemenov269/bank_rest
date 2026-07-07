package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
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
    private CardStatus cardStatus;
    private BigDecimal balance;   // Только для владельца.
    private String maskedCardNumber;
    private String fullCardNumber;  // Только для владельца.
}
