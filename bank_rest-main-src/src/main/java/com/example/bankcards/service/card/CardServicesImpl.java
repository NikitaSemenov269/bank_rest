package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.models.Card;
import com.example.bankcards.entity.models.User;
import com.example.bankcards.exception.CardExpiredException;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mappers.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.user.UserService;
import com.example.bankcards.util.CardEncryptorAndDecrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.example.bankcards.util.SecurityUtils.getCurrentUserId;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServicesImpl implements CardServices {

    private final CardEncryptorAndDecrypt cardEncryptor;
    private final UserService userService;
    private final CardRepository repository;
    private final CardMapper mapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponse createNewCard(String login, CardCreateRequest cardCreateRequest) {
        log.info("Попытка создания новой карты.");
        validateValidityPeriod(cardCreateRequest.getValidityPeriod());

        User user = userService.findByLoginAsAdmin(login);

        if (!user.getName().equals(cardCreateRequest.getCardholderName())) {
            throw new ConflictException("Имя пользователя и держателя карты не совпадают.");
        }

        String cardNumber = cardCreateRequest.getCardNumber();

        Card card = mapper.toEntity(cardCreateRequest);
        card.setCardNumberEncrypted(cardEncryptor.encrypt(cardNumber));
        card.setCardNumberLast4(cardNumber.substring(cardNumber.length() - 4));

        if (!user.getCardsOfUser().add(card)) {
            throw new ConflictException("Карта уже существует.");
        }
        card.setUser(user);
        log.info("Карта успешно добавлена.");

        return mapper.toDto(card);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponse updateCard(UUID idCard, CardUpdateRequest cardUpdateRequest) {
        log.info("Попытка обновления данных карты.");

        Card card = repository.findById(idCard).orElseThrow(
                () -> new NotFoundException("Карта не найдена.")
        );

        if (!card.getStatus().equals(CardStatus.ACTIVE)) {
            throw new ConflictException("Статус карты не соответствует ожидаемому. " +
                    "Статус: " + card.getStatus());
        }
        if (!card.getCardholderName().equals(cardUpdateRequest.getCardholderName())) {
            card.setCardholderName(cardUpdateRequest.getCardholderName());
        }

        if (!card.getValidityPeriod().equals(cardUpdateRequest.getValidityPeriod())) {
            try {
                validateValidityPeriod(cardUpdateRequest.getValidityPeriod());
                card.setValidityPeriod(cardUpdateRequest.getValidityPeriod());
            } catch (CardExpiredException e) {
                setStatusCard(idCard, CardStatus.EXPIRED);
                throw e;
            }
        }
        return mapper.toDto(card);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCard(UUID id) {
        Card card = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Карта не найдена."));
        User user = card.getUser();
        if (user != null) {
            user.getCardsOfUser().remove(card);
            card.setUser(null);
        }
        repository.delete(card);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Page<CardResponse> findAllCardsOfUser(Pageable pageable) {
        return repository.findAllByUserId(getCurrentUserId(), pageable)
                .map(card -> {
                    CardResponse response = mapper.toDtoForOwner(card);
                    response.setFullCardNumber(cardEncryptor.decrypt(card.getCardNumberEncrypted()));
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardResponse> findAllCards(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public BigDecimal showBalanceOfCard(UUID id) {
        Card card = repository.findByUserIdAndCardId(getCurrentUserId(), id).orElseThrow(
                () -> new NotFoundException("Карта не найдена.")
        );
        return card.getBalance();
    }

    // Обработать исключение в хандлере
    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void transferBetweenCardsOfUser(UUID receivingCardID, UUID givingCardID, BigDecimal amount) {
        if (receivingCardID.equals(givingCardID)) {
            throw new ConflictException("Нельзя произвести перевод на ту же карту.");
        }

        Card receivingCard = repository.findByUserIdAndCardId(getCurrentUserId(), receivingCardID).orElseThrow(
                () -> new NotFoundException("Карта-получатель не найдена."));

        Card givingCard = repository.findByUserIdAndCardId(getCurrentUserId(), givingCardID).orElseThrow(
                () -> new NotFoundException("Карта-отправитель не найдена."));

        if (receivingCard.getStatus() != CardStatus.ACTIVE || givingCard.getStatus() != CardStatus.ACTIVE) {
            throw new ConflictException("Карт не активна. receivingCard: " + receivingCard.getStatus()
                    + " givingCard: " + givingCard.getStatus());
        }

        try {
            validateValidityPeriod(receivingCard.getValidityPeriod());
        } catch (CardExpiredException e) {
            setStatusCard(receivingCard.getId(), CardStatus.EXPIRED);
            throw e;
        }

        try {
            validateValidityPeriod(givingCard.getValidityPeriod());
        } catch (CardExpiredException e) {
            setStatusCard(givingCard.getId(), CardStatus.EXPIRED);
            throw e;
        }
        BigDecimal givingCardBalance = givingCard.getBalance().setScale(3, RoundingMode.HALF_UP);

        if (givingCardBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств для совершения операции.");
        }

        BigDecimal receivingCardBalance = receivingCard.getBalance().setScale(3, RoundingMode.HALF_UP);
        amount = amount.setScale(3, RoundingMode.HALF_UP);

        receivingCard.setBalance(receivingCardBalance.add(amount));
        givingCard.setBalance(givingCardBalance.subtract(amount));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ADMIN')")
    public void setStatusCard(UUID id, CardStatus status) {
        Card card = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Карта не найдена"));
        card.setStatus(status);
    }

    private void validateValidityPeriod(String validityPeriod) {
        LocalDate period = LocalDate.parse(validityPeriod, FORMATTER);
        if (period.isBefore(LocalDate.now())) {
            throw new CardExpiredException("Срок действия карты уже истёк.");
        }
    }
}

