package com.example.bankcards.controller;

import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.card.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserControllerCards.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class UserControllerCardsTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CardService cardService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getMyCards_ShouldReturnOk() throws Exception {
        when(cardService.getAllCardsOfUser(any())).thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/user/cards")
                        .param("page", "0")
                        .param("size", "15"))
                .andExpect(status().isOk());

        verify(cardService).getAllCardsOfUser(any());
    }

    @Test
    void getBalance_ShouldReturnOk() throws Exception {
        UUID cardId = UUID.randomUUID();
        when(cardService.getCardBalance(cardId)).thenReturn(new BigDecimal("150.75"));

        mockMvc.perform(get("/api/user/cards/{id}/balance", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(150.75));

        verify(cardService).getCardBalance(cardId);
    }

    @Test
    void transfer_ShouldReturnOk() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .receivingCardId(UUID.randomUUID())
                .givingCardId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .build();

        doNothing().when(cardService).transferBetweenCards(any(), any(), any());

        mockMvc.perform(post("/api/user/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(cardService).transferBetweenCards(
                eq(request.getReceivingCardId()),
                eq(request.getGivingCardId()),
                eq(request.getAmount()));
    }

    @Test
    void requestBlock_ShouldReturnOk() throws Exception {
        UUID cardId = UUID.randomUUID();
        doNothing().when(cardService).requestBlockCard(cardId);

        mockMvc.perform(post("/api/user/cards/{id}/request-block", cardId))
                .andExpect(status().isOk());

        verify(cardService).requestBlockCard(cardId);
    }
}