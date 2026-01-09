package ru.murad.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO для ответа с данными пользователя")
public class UserResponseDto {

    @Schema(description = "UUID пользователя", example = "b3d8b938-6e84-49f6-a3ec-79830946b73a")
    private UUID uuid;

    @Schema(description = "ФИО пользователя", example = "Иванов Иван Иванович")
    private String fio;

    @Schema(description = "Номер телефона", example = "+79161234567")
    private String phoneNumber;

    @Schema(description = "URL аватара пользователя", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "Роль пользователя", example = "USER")
    private String role;
}