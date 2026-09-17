package com.example.saldo.dto.workspace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetRequest(
        @NotNull(message = "Категория обязательна")
        UUID categoryId,

        @NotBlank(message = "Период обязателен")
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Период — в формате yyyy-MM")
        String period,

        @NotNull(message = "Плановая сумма обязательна")
        @DecimalMin(value = "0.01", message = "Плановая сумма должна быть больше 0")
        BigDecimal amount
) {
}
