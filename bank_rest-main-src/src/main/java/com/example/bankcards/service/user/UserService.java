package com.example.bankcards.service.user;


import com.example.bankcards.dto.user.UserCreateRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.enums.Role;

import java.util.UUID;

public interface UserService {

    UserResponse createNewUser(UserCreateRequest newUser);

    UserResponse findByName(String name);

    Role findRoleById(UUID userId);

    void changePassword(UUID id, String oldPassword, String newPassword);

    void checkPassword(String rawPassword, UUID userId);
}
