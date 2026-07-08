package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.UserCreateRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.models.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mappers.UserMapper;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
        if (repository.existsByName(newUser.getName())) {
            throw new ConflictException("Пользователь с именем '" + newUser.getName() + "' уже существует");
        }

        String hashedPassword = passwordEncoder.encode(newUser.getPassword());

        User user = mapper.toEntity(newUser);
        user.setPasswordHash(hashedPassword);

        repository.save(user);
        return mapper.toDto(user);
    }

    // Только для админов:
    // (пока закомментировано)

    @Transactional
    public void deleteUser(UUID id) {
        // пока пусто
    }

    // Только для пользователей
    @Override
    @Transactional
    public void changePassword(UUID id, String oldPassword, String newPassword) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        checkPassword(oldPassword, user.getPasswordHash());
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Пользователь с именем " + name + " не найден."));
    }

    @Override
    @Transactional(readOnly = true)
    public Role findRoleById(UUID userId) {
        return repository.findRoleById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден."));
    }

    @Override
    @Transactional(readOnly = true)
    public void checkPassword(String rawPassword, UUID userId) {
        String encodedPassword = repository.findPasswordHashById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден."));
        checkPassword(rawPassword, encodedPassword);
    }

    private void checkPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new NotFoundException("Неверный пароль.");
        }
    }
}
