package ru.murad.service;


import ru.murad.dto.UserCreateRequestDto;
import ru.murad.dto.UserResponseDto;
import ru.murad.dto.UserUpdateRequestDto;

import java.util.UUID;

public interface UserService {
    UserResponseDto createUser(UserCreateRequestDto request);
    UserResponseDto getUser(UUID uuid);
    UserResponseDto updateUser(UserUpdateRequestDto request);
    void deleteUser(UUID uuid);
}

