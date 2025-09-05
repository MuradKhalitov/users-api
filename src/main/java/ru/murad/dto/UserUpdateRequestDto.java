package ru.murad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

public record UserUpdateRequestDto(
        @NotNull(message = "UUID обязателен")
        UUID uuid,

        @NotBlank(message = "FIO обязателен")
        String fio,

        @NotBlank(message = "Телефон обязателен")
        @Pattern(regexp = "^[+]?\\d{10,15}$", message = "Телефон должен быть в формате +XXXXXXXXXXX (10-15 цифр)")
        String phoneNumber,

        @NotBlank(message = "Avatar url обязателен")
        @URL(message = "Avatar должен быть валидным URL")
        String avatar,

        @NotBlank(message = "Role обязательна")
        String role
) {
}

