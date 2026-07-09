package com.example.bankcards.mappers;

import com.example.bankcards.dto.user.UserCreateRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "email", ignore = true)
    UserResponse toDto(User user);

    UserResponse toDtoWithEmail(User user);

    User toEntity(UserCreateRequest newUser);
}