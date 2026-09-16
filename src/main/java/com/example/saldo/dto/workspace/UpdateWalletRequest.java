package com.example.saldo.dto.workspace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateWalletRequest(
        @Size(max = 100, message = "Название кошелька — максимум 100 символов")
        String name,

        @DecimalMin(value = "0.00", message = "Начальный баланс не может быть отрицательным")
        BigDecimal initialBalance
) {
}
