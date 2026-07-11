package com.example.bankcards.repository;

import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.models.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    Page<Card> findAll(Pageable pageable);

    Page<Card> findAllByUserId(UUID userId, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.status = :status")
    Page<Card> findAllByCardStatus(@Param("status") CardStatus status, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.id = :cardId")
    Optional<Card> findByUserIdAndCardId(@Param("userId") UUID userId, @Param("cardId") UUID cardId);
}
