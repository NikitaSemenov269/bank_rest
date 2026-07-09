package com.example.bankcards.repository;

import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.models.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    Page<Card> findAll(Pageable pageable);

    Page<Card> findAllByUserId(UUID userId, Pageable pageable);

    Page<Card> findAllByCardStatus(CardStatus status, Pageable pageable);

    Optional<Card> findByUserIdAndCardId(UUID userId, UUID cardId);

}
