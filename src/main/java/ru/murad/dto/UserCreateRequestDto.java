package ru.murad.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

@Builder
@Schema(description = "DTO для создания нового пользователя")
public record UserCreateRequestDto(
        @Schema(description = "ФИО пользователя", example = "Иванов Иван Иванович")
        @NotBlank(message = "FIO обязателен")
        String fio,

        @Schema(description = "Номер телефона", example = "+79161234567")
        @NotBlank(message = "Телефон обязателен")
        @Pattern(regexp = "^[+]?\\d{10,15}$", message = "Телефон должен быть в формате +XXXXXXXXXXX (10-15 цифр)")
        String phoneNumber,

        @Schema(description = "Email пользователя", example = "user@example.com")
        @NotBlank(message = "Email обязателен")
        @Email(message = "Email должен быть валидным")
        String email,


        @Schema(description = "URL аватара пользователя", example = "https://example.com/avatar.jpg")
        @NotBlank(message = "Avatar url обязателен")
        @URL(message = "Avatar должен быть валидным URL")
        String avatar,

        @Schema(description = "Роль пользователя", example = "USER")
        @NotBlank(message = "Role обязательна")
        String role
) {
}

