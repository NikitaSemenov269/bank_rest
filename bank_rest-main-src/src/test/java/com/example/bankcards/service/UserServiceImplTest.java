package com.example.bankcards.service;

import com.example.bankcards.dto.user.*;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.models.Card;
import com.example.bankcards.entity.models.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mappers.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.user.UserServiceImpl;
import com.example.bankcards.util.SecurityUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static MockedStatic<SecurityUtils> securityUtilsMock;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository repository;
    @Mock
    private UserMapper mapper;
    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
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
        userId = UUID.randomUUID();
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

        user = User.builder()
                .id(userId)
                .name("Vasya")
                .login("vasya123")
                .email("vasya@mail.ru")
                .passwordHash("$2a$10$hashed")
                .role(Role.ROLE_USER)
                .cardsOfUser(new HashSet<>())
                .build();
    }

    @Nested
    class CreateNewUser {

        @Test
        void shouldCreateUser() {
            UserCreateRequest request = UserCreateRequest.builder()
                    .name("Vasya")
                    .login("vasya123")
                    .email("vasya@mail.ru")
                    .password("P@ssw0rd123")
                    .build();

            User userAfterMapper = User.builder().email("vasya@mail.ru").build();

            when(repository.existsByLogin("vasya123")).thenReturn(false);
            when(passwordEncoder.encode("P@ssw0rd123")).thenReturn("$2a$10$hashed");
            when(mapper.toEntity(request)).thenReturn(userAfterMapper);
            when(mapper.toDto(userAfterMapper)).thenReturn(
                    UserResponse.builder().email("vasya@mail.ru").build()
            );

            UserResponse result = userService.createNewUser(request);

            assertNotNull(result);
            assertEquals("vasya@mail.ru", result.getEmail());
            verify(repository).save(any());
        }

        @Test
        void shouldThrowException_WhenLoginExists() {
            UserCreateRequest request = UserCreateRequest.builder().login("vasya123").build();
            when(repository.existsByLogin("vasya123")).thenReturn(true);

            assertThrows(ConflictException.class, () -> userService.createNewUser(request));
            verify(repository, never()).save(any());
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void shouldUpdateNameAndEmail() {
            UserUpdateRequest request = UserUpdateRequest.builder()
                    .name("New Name")
                    .email("new@mail.ru")
                    .build();

            when(repository.findById(userId)).thenReturn(Optional.of(user));
            when(mapper.toDto(user)).thenReturn(UserResponse.builder().name("New Name").build());

            UserResponse result = userService.updateUser(userId, request);

            assertEquals("New Name", user.getName());
            assertEquals("new@mail.ru", user.getEmail());
            assertEquals("New Name", result.getName());
        }

        @Test
        void shouldNotUpdate_WhenSameValues() {
            UserUpdateRequest request = UserUpdateRequest.builder()
                    .name("Vasya")
                    .email("vasya@mail.ru")
                    .build();

            when(repository.findById(userId)).thenReturn(Optional.of(user));
            when(mapper.toDto(user)).thenReturn(UserResponse.builder().build());

            userService.updateUser(userId, request);

            assertEquals("Vasya", user.getName());
            assertEquals("vasya@mail.ru", user.getEmail());
        }

        @Test
        void shouldThrowException_WhenUserNotFound() {
            when(repository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> userService.updateUser(userId, any()));
        }
    }

    // ==================== deleteUser ====================

    @Nested
    class DeleteUser {

        @Test
        void shouldDeleteUserAndDetachCards() {
            Card card = mock(Card.class);
            user.getCardsOfUser().add(card);

            when(repository.findById(userId)).thenReturn(Optional.of(user));

            userService.deleteUser(userId);

            verify(card).setUser(null);
            verify(repository).delete(user);
            assertTrue(user.getCardsOfUser().isEmpty());
        }

        @Test
        void shouldThrowException_WhenUserNotFound() {
            when(repository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.deleteUser(userId));
        }
    }

    // ==================== changeLogin ====================

    @Nested
    class ChangeLogin {

        @Test
        void shouldChangeLogin() {
            ChangeLoginRequest request = ChangeLoginRequest.builder()
                    .newLogin("newLogin")
                    .build();

            when(repository.existsByLogin("newLogin")).thenReturn(false);
            when(repository.findById(userId)).thenReturn(Optional.of(user));

            userService.changeLogin(request);

            assertEquals("newLogin", user.getLogin());
        }

        @Test
        void shouldThrowException_WhenLoginExists() {
            ChangeLoginRequest request = ChangeLoginRequest.builder()
                    .newLogin("takenLogin")
                    .build();

            when(repository.existsByLogin("takenLogin")).thenReturn(true);

            assertThrows(ConflictException.class, () -> userService.changeLogin(request));
            verify(repository, never()).findById(any());
        }

        @Test
        void shouldThrowException_WhenUserNotFound() {
            ChangeLoginRequest request = ChangeLoginRequest.builder()
                    .newLogin("newLogin")
                    .build();

            when(repository.existsByLogin("newLogin")).thenReturn(false);
            when(repository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.changeLogin(request));
        }
    }

    // ==================== changePassword ====================

    @Nested
    class ChangePassword {

        @Test
        void shouldChangePassword_WhenOldPasswordCorrect() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .oldPassword("OldP@ssw0rd")
                    .newPassword("NewP@ssw0rd123")
                    .build();

            when(repository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("OldP@ssw0rd", "$2a$10$hashed")).thenReturn(true);
            when(passwordEncoder.encode("NewP@ssw0rd123")).thenReturn("$2a$10$newhashed");

            userService.changePassword(request);

            assertEquals("$2a$10$newhashed", user.getPasswordHash());
        }

        @Test
        void shouldThrowException_WhenOldPasswordIncorrect() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .oldPassword("WrongPassword")
                    .newPassword("NewP@ssw0rd123")
                    .build();

            when(repository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("WrongPassword", "$2a$10$hashed")).thenReturn(false);

            assertThrows(NotFoundException.class, () -> userService.changePassword(request));
        }
    }

    // ==================== changeRole ====================

    @Nested
    class ChangeRole {

        @Test
        void shouldChangeRoleToAdmin() {
            when(repository.findById(userId)).thenReturn(Optional.of(user));

            userService.changeRole(userId);

            assertEquals(Role.ROLE_ADMIN, user.getRole());
        }

        @Test
        void shouldDoNothing_WhenAlreadyAdmin() {
            user.setRole(Role.ROLE_ADMIN);
            when(repository.findById(userId)).thenReturn(Optional.of(user));

            userService.changeRole(userId);

            assertEquals(Role.ROLE_ADMIN, user.getRole());
        }

        @Test
        void shouldThrowException_WhenUserNotFound() {
            when(repository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.changeRole(userId));
        }
    }

    // ==================== checkPassword ====================

    @Nested
    class CheckPassword {

        @Test
        void shouldPass_WhenPasswordCorrect() {
            when(repository.findPasswordHashById(userId)).thenReturn(Optional.of("$2a$10$hashed"));
            when(passwordEncoder.matches("P@ssw0rd123", "$2a$10$hashed")).thenReturn(true);

            assertDoesNotThrow(() -> userService.checkPassword("P@ssw0rd123", userId));
        }

        @Test
        void shouldThrowException_WhenPasswordIncorrect() {
            when(repository.findPasswordHashById(userId)).thenReturn(Optional.of("$2a$10$hashed"));
            when(passwordEncoder.matches("WrongPassword", "$2a$10$hashed")).thenReturn(false);

            assertThrows(NotFoundException.class,
                    () -> userService.checkPassword("WrongPassword", userId));
        }

        @Test
        void shouldThrowException_WhenUserNotFound() {
            when(repository.findPasswordHashById(userId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> userService.checkPassword("P@ssw0rd123", userId));
        }
    }

    // ==================== findById ====================

    @Nested
    class FindById {

        @Test
        void shouldReturnUser() {
            when(repository.findById(userId)).thenReturn(Optional.of(user));
            when(mapper.toDto(user)).thenReturn(UserResponse.builder().id(userId).name("Vasya").build());

            UserResponse result = userService.findById(userId);

            assertEquals(userId, result.getId());
            assertEquals("Vasya", result.getName());
        }

        @Test
        void shouldThrowException_WhenNotFound() {
            when(repository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.findById(userId));
        }
    }

    // ==================== findByLogin ====================

    @Nested
    class FindByLogin {

        @Test
        void shouldReturnUser() {
            when(repository.findByLogin("vasya123")).thenReturn(Optional.of(user));
            when(mapper.toDto(user)).thenReturn(UserResponse.builder().name("Vasya").build());

            UserResponse result = userService.findByLogin("vasya123");

            assertEquals("Vasya", result.getName());
        }

        @Test
        void shouldThrowException_WhenNotFound() {
            when(repository.findByLogin("unknown")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.findByLogin("unknown"));
        }
    }

    // ==================== findByEmail ====================

    @Nested
    class FindByEmail {

        @Test
        void shouldReturnUserWithEmail() {
            when(repository.findByEmail("vasya@mail.ru")).thenReturn(Optional.of(user));
            when(mapper.toDtoWithEmail(user)).thenReturn(
                    UserResponse.builder().email("vasya@mail.ru").build());

            UserResponse result = userService.findByEmail("vasya@mail.ru");

            assertEquals("vasya@mail.ru", result.getEmail());
        }

        @Test
        void shouldThrowException_WhenNotFound() {
            when(repository.findByEmail("unknown@mail.ru")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> userService.findByEmail("unknown@mail.ru"));
        }
    }

    // ==================== findRoleById ====================

    @Nested
    class FindRoleById {

        @Test
        void shouldReturnRole() {
            when(repository.findRoleById(userId)).thenReturn(Optional.of(Role.ROLE_USER));

            Role result = userService.findRoleById(userId);

            assertEquals(Role.ROLE_USER, result);
        }

        @Test
        void shouldThrowException_WhenNotFound() {
            when(repository.findRoleById(userId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.findRoleById(userId));
        }
    }

    // ==================== findByLoginAsAdmin ====================

    @Nested
    class FindByLoginAsAdmin {

        @Test
        void shouldReturnUserEntity() {
            when(repository.findByLoginAsAdmin("vasya123")).thenReturn(Optional.of(user));

            User result = userService.findByLoginAsAdmin("vasya123");

            assertEquals(userId, result.getId());
        }

        @Test
        void shouldThrowException_WhenNotFound() {
            when(repository.findByLoginAsAdmin("unknown")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> userService.findByLoginAsAdmin("unknown"));
        }
    }
}