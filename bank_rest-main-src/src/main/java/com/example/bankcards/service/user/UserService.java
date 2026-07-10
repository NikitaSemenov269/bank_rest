package com.example.bankcards.service.user;


import com.example.bankcards.dto.user.*;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.models.User;

import java.util.UUID;

public interface UserService {

    UserResponse createNewUser(UserCreateRequest newUser);

    UserResponse updateUser(UUID id, UserUpdateRequest updateRequest);

    void deleteUser(UUID id);

    void changeRole(UUID id);

    UserResponse findByEmail(String email);

    Role findRoleById(UUID userId);

    UserResponse findById(UUID id);

    UserResponse findByLogin(String login);

    User findByLoginAsAdmin(String login);

    void changePassword(ChangePasswordRequest changePasswordRequest);

    void changeLogin(ChangeLoginRequest changeLoginRequest);

    void checkPassword(String rawPassword, UUID userId);

}
