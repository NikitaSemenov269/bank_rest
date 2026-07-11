package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.service.card.CardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/cards")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
@RequiredArgsConstructor
public class UserControllerCards {
    private final CardService service;

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getMyCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.getAllCardsOfUser(pageable));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getCardBalance(id));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        service.transferBetweenCards(request.getReceivingCardId(), request.getGivingCardId(), request.getAmount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/request-block")
    public ResponseEntity<Void> requestBlock(@PathVariable @NotNull UUID id) {
        service.requestBlockCard(id);
        return ResponseEntity.ok().build();
    }
}
