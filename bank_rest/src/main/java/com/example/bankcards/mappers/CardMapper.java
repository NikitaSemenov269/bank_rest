package com.example.bankcards.mappers;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.entity.models.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "cardStatus", source = "status")
    @Mapping(target = "maskedCardNumber", expression = "java(\"**** **** **** \" + card.getCardNumberLast4())")
    CardResponse toDto(Card card);

    @Mapping(target = "cardStatus", source = "status")
    CardResponse toDtoForOwner(Card card);

    Card toEntity(CardCreateRequest newCard);
}