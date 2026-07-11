package com.example.bankcards.entity.models;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "card_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "card_number_encrypted", nullable = false)
    private String cardNumberEncrypted;

    @Column(name = "card_number_last4", nullable = false, length = 4)
    private String cardNumberLast4;

    @Column(name = "cardholder_name", nullable = false)
    private String cardholderName;

    @Column(name = "validity_period", nullable = false, length = 10)
    private String validityPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_status", nullable = false)
    @Builder.Default
    private CardStatus status = CardStatus.ACTIVE;

    @Column(name = "balance", nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
