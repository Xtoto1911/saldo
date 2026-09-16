package com.example.saldo.dto.workspace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionRequest(
        @NotNull(message = "Кошелёк обязателен")
        UUID walletId,

        @NotNull(message = "Категория обязательна")
        UUID categoryId,

        @NotNull(message = "Сумма обязательна")
        @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
        BigDecimal amount,

        @NotNull(message = "Дата операции обязательна")
        LocalDateTime occurredAt,

        @Size(max = 1000, message = "Комментарий — максимум 1000 символов")
        String comment
) {
}
