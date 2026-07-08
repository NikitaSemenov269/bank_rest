package com.example.bankcards.mappers;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.entity.models.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "fullCardNumber", ignore = true)
    CardResponse toDto(Card card);

    @Mapping(target = "maskedCardNumber", ignore = true)
    @Mapping(target = "fullCardNumber", ignore = true)
    CardResponse toDtoForOwner(Card card);

    Card toEntity(CardCreateRequest newCard);

    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "fullCardNumber", ignore = true)
    List<CardResponse> toListDto(List<Card> cards);
}
