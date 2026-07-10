package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.models.Card;
import com.example.bankcards.entity.models.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.mappers.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.card.CardServicesImpl;
import com.example.bankcards.service.user.UserService;
import com.example.bankcards.util.CardEncryptorAndDecrypt;
import com.example.bankcards.util.SecurityUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServicesImplTest {

    private static MockedStatic<SecurityUtils> securityUtilsMock;

    @Mock
    private CardEncryptorAndDecrypt cardEncryptor;
    @Mock
    private UserService userService;
    @Mock
    private CardRepository repository;
    @Mock
    private CardMapper mapper;
    @InjectMocks
    private CardServicesImpl cardService;

    private UUID cardId;
    private UUID userId;
    private Card card;
    private User user;

    @BeforeAll
    static void setUpStatic() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterAll
    static void tearDownStatic() {
        securityUtilsMock.close();
    }

    @BeforeEach
    void setUp() {
        cardId = UUID.randomUUID();
        userId = UUID.randomUUID();

        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

        user = User.builder()
                .id(userId)
                .name("Vasya")
                .cardsOfUser(new HashSet<>())
                .build();

        card = Card.builder()
                .id(cardId)
                .cardNumberEncrypted("encrypted-1234")
                .cardNumberLast4("1234")
                .cardholderName("Vasya")
                .validityPeriod("12.2030")
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .user(user)
                .build();
    }

    @Nested
    class CreateNewCard {

        @Test
        void shouldCreateCard_WhenValidRequest() {
            CardCreateRequest request = CardCreateRequest.builder()
                    .cardNumber("4111111111111111")
                    .cardholderName("Vasya")
                    .validityPeriod("12.2030")
                    .build();

            when(userService.findByLoginAsAdmin("vasya123")).thenReturn(user);
            when(mapper.toEntity(request)).thenReturn(new Card());
            when(cardEncryptor.encrypt("4111111111111111")).thenReturn("encrypted");
            when(mapper.toDto(any())).thenReturn(CardResponse.builder().build());

            CardResponse result = cardService.createNewCard("vasya123", request);

            assertNotNull(result);
            verify(cardEncryptor).encrypt("4111111111111111");
        }

        @Test
        void shouldThrowException_WhenNameDoesNotMatch() {
            CardCreateRequest request = CardCreateRequest.builder()
                    .cardNumber("4111111111111111")
                    .cardholderName("Petya")
                    .validityPeriod("12.2030")
                    .build();

            when(userService.findByLoginAsAdmin("vasya123")).thenReturn(user);

            assertThrows(ConflictException.class,
                    () -> cardService.createNewCard("vasya123", request));
        }

        @Test
        void shouldThrowException_WhenValidityPeriodExpired() {
            CardCreateRequest request = CardCreateRequest.builder()
                    .cardNumber("4111111111111111")
                    .cardholderName("Vasya")
                    .validityPeriod("01.2020")
                    .build();

            assertThrows(CardExpiredException.class,
                    () -> cardService.createNewCard("vasya123", request));
        }
    }

    @Nested
    class UpdateCard {

        @Test
        void shouldUpdateCardholderName() {
            CardUpdateRequest request = CardUpdateRequest.builder()
                    .cardholderName("New Name")
                    .validityPeriod("12.2030")
                    .build();

            when(repository.findById(cardId)).thenReturn(Optional.of(card));
            when(mapper.toDto(card)).thenReturn(CardResponse.builder().build());

            cardService.updateCard(cardId, request);

            assertEquals("New Name", card.getCardholderName());
        }

        @Test
        void shouldUpdateValidityPeriod() {
            CardUpdateRequest request = CardUpdateRequest.builder()
                    .cardholderName("Vasya")
                    .validityPeriod("06.2031")
                    .build();

            when(repository.findById(cardId)).thenReturn(Optional.of(card));
            when(mapper.toDto(card)).thenReturn(CardResponse.builder().build());

            cardService.updateCard(cardId, request);

            assertEquals("06.2031", card.getValidityPeriod());
        }

        @Test
        void shouldThrowException_WhenCardNotFound() {
            when(repository.findById(cardId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> cardService.updateCard(cardId, any()));
        }
    }

    @Nested
    class DeleteCard {

        @Test
        void shouldDeleteCardAndRemoveFromUser() {
            user.getCardsOfUser().add(card);

            when(repository.findById(cardId)).thenReturn(Optional.of(card));

            cardService.deleteCard(cardId);

            verify(repository).delete(card);
            assertTrue(user.getCardsOfUser().isEmpty());
            assertNull(card.getUser());
        }

        @Test
        void shouldThrowException_WhenCardNotFound() {
            when(repository.findById(cardId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> cardService.deleteCard(cardId));
        }
    }

    @Nested
    class GetCardBalance {

        @Test
        void shouldReturnBalance_WhenCardActive() {
            when(repository.findByUserIdAndCardId(userId, cardId)).thenReturn(Optional.of(card));

            BigDecimal balance = cardService.getCardBalance(cardId);

            assertEquals(new BigDecimal("1000.00"), balance);
        }

        @Test
        void shouldThrowException_WhenCardNotActive() {
            card.setStatus(CardStatus.BLOCKED);
            when(repository.findByUserIdAndCardId(userId, cardId)).thenReturn(Optional.of(card));

            assertThrows(ConflictException.class,
                    () -> cardService.getCardBalance(cardId));
        }
    }

    @Nested
    class TransferBetweenCards {

        private Card givingCard;
        private Card receivingCard;

        @BeforeEach
        void setUp() {
            givingCard = Card.builder()
                    .id(UUID.randomUUID())
                    .balance(new BigDecimal("500.00"))
                    .status(CardStatus.ACTIVE)
                    .validityPeriod("12.2030")
                    .user(user)
                    .build();

            receivingCard = Card.builder()
                    .id(UUID.randomUUID())
                    .balance(new BigDecimal("200.00"))
                    .status(CardStatus.ACTIVE)
                    .validityPeriod("12.2030")
                    .user(user)
                    .build();
        }

        @Test
        void shouldTransferMoney_WhenValidData() {
            when(repository.findByUserIdAndCardId(userId, givingCard.getId()))
                    .thenReturn(Optional.of(givingCard));
            when(repository.findByUserIdAndCardId(userId, receivingCard.getId()))
                    .thenReturn(Optional.of(receivingCard));

            cardService.transferBetweenCards(receivingCard.getId(), givingCard.getId(), new BigDecimal("100.00"));

            assertEquals(new BigDecimal("400.00"), givingCard.getBalance());
            assertEquals(new BigDecimal("300.00"), receivingCard.getBalance());
        }

        @Test
        void shouldThrowException_WhenSameCard() {
            assertThrows(ConflictException.class,
                    () -> cardService.transferBetweenCards(cardId, cardId, new BigDecimal("100.00")));
        }

        @Test
        void shouldThrowException_WhenInsufficientFunds() {
            when(repository.findByUserIdAndCardId(userId, givingCard.getId()))
                    .thenReturn(Optional.of(givingCard));
            when(repository.findByUserIdAndCardId(userId, receivingCard.getId()))
                    .thenReturn(Optional.of(receivingCard));

            assertThrows(InsufficientFundsException.class,
                    () -> cardService.transferBetweenCards(
                            receivingCard.getId(), givingCard.getId(), new BigDecimal("600.00")));
        }

        @Test
        void shouldThrowException_WhenAmountBelowMinimum() {
            assertThrows(InvalidAmountException.class,
                    () -> cardService.transferBetweenCards(
                            receivingCard.getId(), givingCard.getId(), new BigDecimal("10.00")));
        }
    }

    @Nested
    class SetStatusCard {

        @Test
        void shouldSetStatus() {
            when(repository.findById(cardId)).thenReturn(Optional.of(card));

            cardService.setStatusCard(cardId, CardStatus.BLOCKED);

            assertEquals(CardStatus.BLOCKED, card.getStatus());
        }

        @Test
        void shouldNotChange_WhenSameStatus() {
            when(repository.findById(cardId)).thenReturn(Optional.of(card));

            cardService.setStatusCard(cardId, CardStatus.ACTIVE);

            assertEquals(CardStatus.ACTIVE, card.getStatus());
        }

        @Test
        void shouldThrowException_WhenActivatingExpiredCard() {
            card.setStatus(CardStatus.EXPIRED);
            when(repository.findById(cardId)).thenReturn(Optional.of(card));

            assertThrows(ConflictException.class,
                    () -> cardService.setStatusCard(cardId, CardStatus.ACTIVE));
        }
    }

    @Nested
    class RequestBlockCard {

        @Test
        void shouldSetPendingBlock() {
            when(repository.findByUserIdAndCardId(userId, cardId)).thenReturn(Optional.of(card));

            cardService.requestBlockCard(cardId);

            assertEquals(CardStatus.PENDING_BLOCK, card.getStatus());
        }

        @Test
        void shouldThrowException_WhenCardNotActive() {
            card.setStatus(CardStatus.BLOCKED);
            when(repository.findByUserIdAndCardId(userId, cardId)).thenReturn(Optional.of(card));

            assertThrows(ConflictException.class,
                    () -> cardService.requestBlockCard(cardId));
        }
    }

    @Nested
    class ApproveRejectBlock {

        @Test
        void shouldApproveBlock() {
            card.setStatus(CardStatus.PENDING_BLOCK);
            when(repository.findById(cardId)).thenReturn(Optional.of(card));

            cardService.approveBlockCard(cardId);

            assertEquals(CardStatus.BLOCKED, card.getStatus());
        }

        @Test
        void shouldRejectBlock() {
            card.setStatus(CardStatus.PENDING_BLOCK);
            when(repository.findById(cardId)).thenReturn(Optional.of(card));

            cardService.rejectBlockCard(cardId);

            assertEquals(CardStatus.ACTIVE, card.getStatus());
        }

        @Test
        void shouldThrowException_WhenNotPendingBlock() {
            when(repository.findById(cardId)).thenReturn(Optional.of(card));

            assertThrows(ConflictException.class,
                    () -> cardService.approveBlockCard(cardId));
        }
    }

    @Nested
    class GetAllCardsOfUser {

        @Test
        void shouldReturnDecryptedCards() {
            Card card = Card.builder()
                    .id(cardId)
                    .cardNumberEncrypted("encrypted-1234")
                    .cardNumberLast4("1234")
                    .cardholderName("Vasya")
                    .status(CardStatus.ACTIVE)
                    .balance(BigDecimal.ZERO)
                    .build();

            Page<Card> cardPage = new PageImpl<>(List.of(card));
            CardResponse response = CardResponse.builder().build();

            when(repository.findAllByUserId(eq(userId), any(Pageable.class))).thenReturn(cardPage);
            when(mapper.toDtoForOwner(card)).thenReturn(response);
            when(cardEncryptor.decrypt("encrypted-1234")).thenReturn("4111111111111111");

            Page<CardResponse> result = cardService.getAllCardsOfUser(Pageable.unpaged());

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(cardEncryptor).decrypt("encrypted-1234");
            verify(mapper).toDtoForOwner(card);
        }

        @Test
        void shouldReturnEmptyPage_WhenNoCards() {
            when(repository.findAllByUserId(eq(userId), any(Pageable.class)))
                    .thenReturn(Page.empty());

            Page<CardResponse> result = cardService.getAllCardsOfUser(Pageable.unpaged());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetAllCards {

        @Test
        void shouldReturnAllCards() {
            Page<Card> cardPage = new PageImpl<>(List.of(card));
            CardResponse response = CardResponse.builder().build();

            when(repository.findAll(any(Pageable.class))).thenReturn(cardPage);
            when(mapper.toDto(card)).thenReturn(response);

            Page<CardResponse> result = cardService.getAllCards(Pageable.unpaged());

            assertEquals(1, result.getTotalElements());
            verify(mapper).toDto(card);
        }

        @Test
        void shouldReturnEmptyPage_WhenNoCards() {
            when(repository.findAll(any(Pageable.class))).thenReturn(Page.empty());

            Page<CardResponse> result = cardService.getAllCards(Pageable.unpaged());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetAllCardsByStatus {

        @Test
        void shouldReturnCardsByStatus() {
            card.setStatus(CardStatus.PENDING_BLOCK);
            Page<Card> cardPage = new PageImpl<>(List.of(card));
            CardResponse response = CardResponse.builder().build();

            when(repository.findAllByCardStatus(eq(CardStatus.PENDING_BLOCK), any(Pageable.class)))
                    .thenReturn(cardPage);
            when(mapper.toDto(card)).thenReturn(response);

            Page<CardResponse> result = cardService.getAllCardsByStatus(
                    CardStatus.PENDING_BLOCK, Pageable.unpaged());

            assertEquals(1, result.getTotalElements());
            verify(repository).findAllByCardStatus(eq(CardStatus.PENDING_BLOCK), any());
        }

        @Test
        void shouldReturnEmptyPage_WhenNoCardsWithStatus() {
            when(repository.findAllByCardStatus(eq(CardStatus.EXPIRED), any(Pageable.class)))
                    .thenReturn(Page.empty());

            Page<CardResponse> result = cardService.getAllCardsByStatus(
                    CardStatus.EXPIRED, Pageable.unpaged());

            assertTrue(result.isEmpty());
        }
    }
}