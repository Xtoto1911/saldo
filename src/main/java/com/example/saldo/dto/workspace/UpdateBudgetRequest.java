package com.example.saldo.dto.workspace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateBudgetRequest(
        @NotNull(message = "Плановая сумма обязательна")
        @DecimalMin(value = "0.01", message = "Плановая сумма должна быть больше 0")
        BigDecimal amount
) {
}
