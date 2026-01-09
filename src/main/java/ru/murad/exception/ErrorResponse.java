package ru.murad.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
@Schema(description = "DTO для ошибок API")
public record ErrorResponse(
        @Schema(description = "Временная метка ошибки", example = "2024-01-15T10:30:00Z")
        Instant timestamp,

        @Schema(description = "HTTP статус код", example = "404")
        int status,

        @Schema(description = "Тип ошибки", example = "Not Found")
        String error,

        @Schema(description = "Сообщение об ошибке", example = "Пользователь не найден")
        String message,

        @Schema(description = "Путь запроса", example = "/api/users")
        String path,

        @Schema(description = "Детали ошибки", example = "[\"UUID должен быть валидным\", \"Пользователь не существует\"]")
        List<String> details
) {
}

