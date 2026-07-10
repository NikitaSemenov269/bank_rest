package com.example.bankcards.repository;

import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    boolean existsByLogin(String login);

    Optional<User> findByEmail(String email);

    Optional<User> findByLogin(String login);

    @Query("SELECT u FROM User u WHERE u.login = :login")
    Optional<User> findByLoginAsAdmin(String login);

    @Query("SELECT u.role FROM User u WHERE u.id = :userId")
    Optional<Role> findRoleById(UUID userId);

    @Query("SELECT u.passwordHash FROM User u WHERE u.id = :userId")
    Optional<String> findPasswordHashById(UUID userId);

}
