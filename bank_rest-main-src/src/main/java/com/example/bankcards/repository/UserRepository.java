package com.example.bankcards.repository;

import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByName(String name);

    Optional<UserResponse> findByName(String name);

    @Query("SELECT u.role FROM User u WHERE u.id = :userId")
    Optional<Role> findRoleById(UUID userId);

    @Query("SELECT u.passwordHash FROM User u WHERE u.id = :userId")
    Optional<String> findPasswordHashById(UUID userId);

}
