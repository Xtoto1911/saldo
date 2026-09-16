package com.example.saldo.dto.workspace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateTransactionRequest(
        UUID walletId,

        UUID categoryId,

        @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
        BigDecimal amount,

        LocalDateTime occurredAt,

        @Size(max = 1000, message = "Комментарий — максимум 1000 символов")
        String comment
) {
}
