package com.example.bankcards.mappers;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.entity.models.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "balance", ignore = true)
    CardResponse toDto(Card card);

    CardResponse toDtoForOwner(Card card);

    Card toEntity(CardCreateRequest newCard);
}