package ru.murad.dto;

import java.util.UUID;

public record UserResponseDto(
        UUID uuid,
        String fio,
        String phoneNumber,
        String email,
        String avatar,
        String role
) {
}
