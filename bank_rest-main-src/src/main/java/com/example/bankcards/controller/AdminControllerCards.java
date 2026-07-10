package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.card.CardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminControllerCards {

    private final CardService service;

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "cardholderName") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(service.getAllCards(pageable));
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(
            @RequestParam @NotBlank(message = "Логин обязательное поле.")
            String login,
            @Valid @RequestBody CardCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createNewCard(login, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardResponse> updateCard(
            @PathVariable UUID id,
            @Valid @RequestBody CardUpdateRequest request) {
        return ResponseEntity.ok(service.updateCard(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID id) {
        service.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> setStatus(
            @PathVariable UUID id,
            @RequestParam @NotNull CardStatus status) {
        service.setStatusCard(id, status);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve-block")
    public ResponseEntity<Void> approveBlock(@PathVariable UUID id) {
        service.approveBlockCard(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reject-block")
    public ResponseEntity<Void> rejectBlock(@PathVariable UUID id) {
        service.rejectBlockCard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-status")
    public ResponseEntity<Page<CardResponse>> getCardsByStatus(
            @RequestParam @NotNull CardStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.getAllCardsByStatus(status, pageable));
    }
}