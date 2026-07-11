package com.example.bankcards.dto.user;

import com.example.bankcards.entity.enums.Role;
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
    private String email; // чувствительные данные.
    private Role role = Role.ROLE_USER;
}
