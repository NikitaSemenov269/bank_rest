package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface CardService {

    CardResponse updateCard(UUID idCard, CardUpdateRequest cardUpdateRequest);

    CardResponse createNewCard(String login, CardCreateRequest cardCreateRequest);

    void deleteCard(UUID id);

    Page<CardResponse> getAllCardsOfUser(Pageable pageable);

    Page<CardResponse> getAllCards(Pageable pageable);

    Page<CardResponse> getAllCardsByStatus(CardStatus status, Pageable pageable);

    BigDecimal getCardBalance(UUID id);

    void transferBetweenCards(UUID receivingCardID, UUID givingCardID, BigDecimal amount);

    void setStatusCard(UUID id, CardStatus status);

    void requestBlockCard(UUID cardId);

    void approveBlockCard(UUID cardId);

    void rejectBlockCard(UUID cardId);
}
