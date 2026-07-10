package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.entity.enums.CardStatus;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AdminControllerCards.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerCardsTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CardService cardService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCards_ShouldReturnOk() throws Exception {
        when(cardService.getAllCards(any())).thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/admin/cards")
                        .param("page", "0")
                        .param("size", "15"))
                .andExpect(status().isOk());

        verify(cardService).getAllCards(any());
    }

    @Test
    void createCard_ShouldReturnCreated() throws Exception {
        CardCreateRequest request = CardCreateRequest.builder()
                .cardNumber("4111111111111111")
                .cardholderName("Vasya")
                .validityPeriod("01.2030")
                .build();

        CardResponse response = CardResponse.builder()
                .id(UUID.randomUUID())
                .cardholderName("Vasya")
                .cardStatus(CardStatus.ACTIVE)
                .build();

        when(cardService.createNewCard(eq("vasya123"), any())).thenReturn(response);

        mockMvc.perform(post("/api/admin/cards")
                        .param("login", "vasya123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(cardService).createNewCard(eq("vasya123"), any());
    }

    @Test
    void updateCard_ShouldReturnOk() throws Exception {
        UUID cardId = UUID.randomUUID();
        CardUpdateRequest request = CardUpdateRequest.builder()
                .cardholderName("New Name")
                .validityPeriod("12.2030")
                .build();

        CardResponse response = CardResponse.builder()
                .id(cardId)
                .cardholderName("New Name")
                .build();

        when(cardService.updateCard(eq(cardId), any())).thenReturn(response);

        mockMvc.perform(put("/api/admin/cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(cardService).updateCard(eq(cardId), any());
    }

    @Test
    void deleteCard_ShouldReturnNoContent() throws Exception {
        UUID cardId = UUID.randomUUID();
        doNothing().when(cardService).deleteCard(cardId);

        mockMvc.perform(delete("/api/admin/cards/{id}", cardId))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard(cardId);
    }

    @Test
    void setStatus_ShouldReturnNoContent() throws Exception {
        UUID cardId = UUID.randomUUID();
        doNothing().when(cardService).setStatusCard(cardId, CardStatus.BLOCKED);

        mockMvc.perform(put("/api/admin/cards/{id}/status", cardId)
                        .param("status", "BLOCKED"))
                .andExpect(status().isNoContent());

        verify(cardService).setStatusCard(cardId, CardStatus.BLOCKED);
    }

    @Test
    void approveBlock_ShouldReturnNoContent() throws Exception {
        UUID cardId = UUID.randomUUID();
        doNothing().when(cardService).approveBlockCard(cardId);

        mockMvc.perform(put("/api/admin/cards/{id}/approve-block", cardId))
                .andExpect(status().isNoContent());

        verify(cardService).approveBlockCard(cardId);
    }

    @Test
    void rejectBlock_ShouldReturnNoContent() throws Exception {
        UUID cardId = UUID.randomUUID();
        doNothing().when(cardService).rejectBlockCard(cardId);

        mockMvc.perform(put("/api/admin/cards/{id}/reject-block", cardId))
                .andExpect(status().isNoContent());

        verify(cardService).rejectBlockCard(cardId);
    }

    @Test
    void getCardsByStatus_ShouldReturnOk() throws Exception {
        when(cardService.getAllCardsByStatus(eq(CardStatus.PENDING_BLOCK), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/admin/cards/by-status")
                        .param("status", "PENDING_BLOCK")
                        .param("page", "0")
                        .param("size", "15"))
                .andExpect(status().isOk());

        verify(cardService).getAllCardsByStatus(eq(CardStatus.PENDING_BLOCK), any());
    }
}