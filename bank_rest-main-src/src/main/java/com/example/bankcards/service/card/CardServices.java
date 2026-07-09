package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardServices {

    Page<CardResponse> findAllCards(Pageable pageable);

}
