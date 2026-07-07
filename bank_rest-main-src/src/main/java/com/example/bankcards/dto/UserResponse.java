package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private UUID id;
    private String name;
    private Role role = Role.ROLE_USER;
}
