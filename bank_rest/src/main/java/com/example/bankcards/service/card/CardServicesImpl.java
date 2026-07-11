package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.models.Card;
import com.example.bankcards.entity.models.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.mappers.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.user.UserService;
import com.example.bankcards.util.CardEncryptorAndDecrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.example.bankcards.util.SecurityUtils.getCurrentUserId;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServicesImpl implements CardService {

    private final CardEncryptorAndDecrypt cardEncryptor;
    private final UserService userService;
    private final CardRepository repository;
    private final CardMapper mapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM.yyyy");

    @Override
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
            throw new DataIntegrityViolationException("Карта уже существует.");
        }
        card.setUser(user);
        log.info("Карта успешно добавлена.");

        return mapper.toDto(card);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponse updateCard(UUID idCard, CardUpdateRequest cardUpdateRequest) {
        log.info("Попытка обновления данных карты.");

        Card card = repository.findById(idCard).orElseThrow(
                () -> new NotFoundException("Карта не найдена.")
        );

        if (!card.getCardholderName().equals(cardUpdateRequest.getCardholderName())) {
            card.setCardholderName(cardUpdateRequest.getCardholderName());
        }

        if (!card.getValidityPeriod().equals(cardUpdateRequest.getValidityPeriod())) {
            card.setValidityPeriod(cardUpdateRequest.getValidityPeriod());
        }
        return mapper.toDto(card);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCard(UUID id) {
        log.info("Попытка удалить карту с id: {}", id);
        Card card = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Карта не найдена."));
        User user = card.getUser();
        if (user != null) {
            user.getCardsOfUser().remove(card);
            card.setUser(null);
        }
        repository.delete(card);
        log.info("Успешное удаление карты с id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Page<CardResponse> getAllCardsOfUser(Pageable pageable) {
        log.info("Попытка получения всех карт пользователя.");
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
    public Page<CardResponse> getAllCards(Pageable pageable) {
        log.info("Попытка получения всех карт.");
        return repository.findAll(pageable)
                .map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardResponse> getAllCardsByStatus(CardStatus status, Pageable pageable) {
        log.info("Попытка получения всех карт c статусом: {}.", status);
        return repository.findAllByCardStatus(status, pageable)
                .map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public BigDecimal getCardBalance(UUID id) {
        log.info("Попытка получения баланса карты c id: {}.", id);
        Card card = repository.findByUserIdAndCardId(getCurrentUserId(), id)
                .orElseThrow(() -> new NotFoundException("Карта не найдена."));

        if (card.getStatus() != CardStatus.ACTIVE) {
            log.info("Карта не активна! Обратитесь к администратору.");
            throw new ConflictException("Карта не активна. Статус: " + card.getStatus());
        }

        checkExpiryAndMark(card.getId(), card.getValidityPeriod());
        return card.getBalance();
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void transferBetweenCards(UUID receivingCardID, UUID givingCardID, BigDecimal amount) {
        log.info("Попытка перевода средств между картами c id: {} и {}.", receivingCardID, givingCardID);
        if (receivingCardID.equals(givingCardID)) {
            throw new ConflictException("Нельзя произвести перевод на ту же карту.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Сумма перевода должна быть положительной.");
        }

        if (amount.compareTo(new BigDecimal("50.00")) < 0) {
            throw new InvalidAmountException("Минимальная сумма перевода: 50.00");
        }

        Card receivingCard = repository.findByUserIdAndCardId(getCurrentUserId(), receivingCardID).orElseThrow(
                () -> new NotFoundException("Карта-получатель не найдена."));

        Card givingCard = repository.findByUserIdAndCardId(getCurrentUserId(), givingCardID).orElseThrow(
                () -> new NotFoundException("Карта-отправитель не найдена."));

        if (receivingCard.getStatus() != CardStatus.ACTIVE || givingCard.getStatus() != CardStatus.ACTIVE) {
            throw new ConflictException("Карт не активна. receivingCard: " + receivingCard.getStatus()
                    + " givingCard: " + givingCard.getStatus());
        }

        checkExpiryAndMark(receivingCard.getId(), receivingCard.getValidityPeriod());
        checkExpiryAndMark(givingCard.getId(), givingCard.getValidityPeriod());

        BigDecimal givingCardBalance = givingCard.getBalance().setScale(2, RoundingMode.HALF_UP);

        if (givingCardBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств для совершения операции.");
        }

        BigDecimal receivingCardBalance = receivingCard.getBalance().setScale(2, RoundingMode.HALF_UP);

        receivingCard.setBalance(receivingCardBalance.add(amount));
        givingCard.setBalance(givingCardBalance.subtract(amount));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ADMIN')")
    public void setStatusCard(UUID id, CardStatus status) {
        log.info("Попытка изменить статус карты c id: {}.", id);
        Card card = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Карта не найдена"));

        if (card.getStatus().equals(status)) {
            return;
        }
        if (status.equals(CardStatus.ACTIVE) && card.getStatus().equals(CardStatus.EXPIRED)) {
            throw new ConflictException("Нельзя активировать карту с истекшим сроком действия.");
        }
        if (status.equals(CardStatus.ACTIVE)) {
            checkExpiryAndMark(id, card.getValidityPeriod());
        }
        card.setStatus(status);
        log.info("Успешное изменение статуса карты c id: {} на {}", id, status);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void requestBlockCard(UUID cardId) {
        log.info("Попытка отправки запроса на блокировку карты c id: {}.", cardId);
        Card card = repository.findByUserIdAndCardId(getCurrentUserId(), cardId)
                .orElseThrow(() -> new NotFoundException("Карта не найдена."));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new ConflictException("Карта не активна. Статус: " + card.getStatus());
        }
        card.setStatus(CardStatus.PENDING_BLOCK);
        log.info("Успешное отправление запроса  на блокировку карты c id: {}.", cardId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void approveBlockCard(UUID cardId) {
        log.info("Попытка одобрения блокировки карты c id: {}.", cardId);
        Card card = repository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта не найдена."));

        if (card.getStatus() != CardStatus.PENDING_BLOCK) {
            throw new ConflictException("Карта не в статусе ожидания блокировки.");
        }
        card.setStatus(CardStatus.BLOCKED);
        log.info("Блокировки карты c id: {} одобрена.", cardId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void rejectBlockCard(UUID cardId) {
        log.info("Попытка отклонить блокировку карты c id: {}.", cardId);
        Card card = repository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта не найдена."));

        if (card.getStatus() != CardStatus.PENDING_BLOCK) {
            throw new ConflictException("Карта не в статусе ожидания блокировки.");
        }
        card.setStatus(CardStatus.ACTIVE);
        log.info("Блокировки карты c id: {} отклонена.", cardId);
    }

    // Только для тестов в swagger.
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void depositCard(UUID cardId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Сумма пополнения должна быть положительной.");
        }
        Card card = repository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта не найдена"));
        card.setBalance(card.getBalance().add(amount));
    }

    private void validateValidityPeriod(String validityPeriod) {
        YearMonth period = YearMonth.parse(validityPeriod, FORMATTER);
        if (period.isBefore(YearMonth.now())) {
            throw new CardExpiredException("Срок действия карты уже истёк.");
        }
    }

    private void checkExpiryAndMark(UUID id, String period) {
        try {
            validateValidityPeriod(period);
        } catch (CardExpiredException e) {
            setStatusCard(id, CardStatus.EXPIRED);
            throw e;
        }
    }
}

