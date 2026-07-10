package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.*;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.models.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mappers.UserMapper;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.example.bankcards.util.SecurityUtils.getCurrentUserId;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserResponse createNewUser(UserCreateRequest newUser) {
        log.info("Попытка создания нового пользователя.");
        if (repository.existsByLogin(newUser.getLogin())) {
            throw new ConflictException("Пользователь с логином '" + newUser.getLogin() + "' уже существует");
        }

        String hashedPassword = passwordEncoder.encode(newUser.getPassword());

        User user = mapper.toEntity(newUser);
        user.setPasswordHash(hashedPassword);

        repository.save(user);
        log.info("Пользователь успешно создан.");
        // Доп. шаг для возможности проверить корректность сохранения email.
        UserResponse userResponse = mapper.toDto(user);
        userResponse.setEmail(user.getEmail());
        return userResponse;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest updateRequest) {
        log.info("Попытка обновления данных пользователя с id: {}", id);
        User user = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь не найден.")
        );

        if (!user.getName().equals(updateRequest.getName())) {
            user.setName(updateRequest.getName());
            log.info("Имя пользователя успешно обновлено.");
        }
        if (!user.getEmail().equals(updateRequest.getEmail())) {
            user.setEmail(updateRequest.getEmail());
            log.info("Почта пользователя успешно обновлена.");
        }
        return mapper.toDto(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Попытка удаления пользователя с id: {}", id);
        User user = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь не найден.")
        );
        user.getCardsOfUser().forEach(card -> card.setUser(null)); // Отвязка карт от конкретного пользователя.
        user.getCardsOfUser().clear();
        repository.delete(user);
        log.info("Пользователь успешно удален.");
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void changeLogin(ChangeLoginRequest changeLoginRequest) {
        if (repository.existsByLogin(changeLoginRequest.getNewLogin())) {
            throw new ConflictException("Логин '" + changeLoginRequest.getNewLogin() + "' уже занят.");
        }

        User user = repository.findById(getCurrentUserId()).orElseThrow(
                () -> new NotFoundException("Пользователь не найден"));

        user.setLogin(changeLoginRequest.getNewLogin());
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = repository.findById(getCurrentUserId()).orElseThrow(
                () -> new NotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPasswordHash())) {
            throw new NotFoundException("Неверный старый пароль.");
        }

        user.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void changeRole(UUID id) {
        User user = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь не найден"));

        if (user.getRole().equals(Role.ROLE_ADMIN)) {
            log.info("Пользователь уже является администратором.");
            return;
        }
        user.setRole(Role.ROLE_ADMIN);
    }

    @Override
    @Transactional(readOnly = true)
    public void checkPassword(String oldPassword, UUID userId) {
        String encodedPassword = repository.findPasswordHashById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        if (!passwordEncoder.matches(oldPassword, encodedPassword)) {
            throw new NotFoundException("Неверный пароль.");
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден.")));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByLogin(String login) {
        return mapper.toDto(repository.findByLogin(login).orElseThrow(
                () -> new NotFoundException("Пользователь не найден.")
        ));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public User findByLoginAsAdmin(String login) {
        return repository.findByLoginAsAdmin(login).orElseThrow(
                () -> new NotFoundException("Пользователь не найден.")
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        return mapper.toDtoWithEmail(repository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден.")));
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional(readOnly = true)
    public Role findRoleById(UUID userId) {
        return repository.findRoleById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден."));
    }

    private void existsById(UUID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Пользователь не найден.");
        }
    }
}
