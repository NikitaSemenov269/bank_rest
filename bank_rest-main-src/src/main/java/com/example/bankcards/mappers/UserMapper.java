package com.example.bankcards.mappers;

import com.example.bankcards.dto.user.UserCreateRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    UserResponse toDto(User user);

    User toEntity(UserCreateRequest newUser);

    @Mapping(target = "passwordHash", ignore = true)
    List<UserResponse> toListDto(List<User> users);
}
