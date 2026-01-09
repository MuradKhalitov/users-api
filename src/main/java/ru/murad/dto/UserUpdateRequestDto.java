package ru.murad.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

@Builder
@Schema(description = "DTO для обновления данных пользователя")
public record UserUpdateRequestDto(
        @Schema(description = "UUID пользователя", example = "b3d8b938-6e84-49f6-a3ec-79830946b73a")
        @NotNull(message = "UUID обязателен")
        UUID uuid,

        @Schema(description = "ФИО пользователя", example = "Иванов Иван Иванович")
        @NotBlank(message = "FIO обязателен")
        String fio,

        @Schema(description = "Номер телефона", example = "+79161234567")
        @NotBlank(message = "Телефон обязателен")
        @Pattern(regexp = "^[+]?\\d{10,15}$", message = "Телефон должен быть в формате +XXXXXXXXXXX (10-15 цифр)")
        String phoneNumber,

        @Schema(description = "URL аватара пользователя", example = "https://example.com/avatar.jpg")
        @NotBlank(message = "Avatar url обязателен")
        @URL(message = "Avatar должен быть валидным URL")
        String avatar,

        @Schema(description = "Роль пользователя", example = "USER")
        @NotBlank(message = "Role обязательна")
        String role
) {
}