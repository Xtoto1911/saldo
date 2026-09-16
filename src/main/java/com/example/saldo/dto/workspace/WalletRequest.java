package com.example.saldo.dto.workspace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record WalletRequest(
        @NotBlank(message = "Название кошелька обязательно")
        @Size(max = 100, message = "Название кошелька — максимум 100 символов")
        String name,

        @NotBlank(message = "Валюта обязательна")
        @Size(min = 3, max = 3, message = "Валюта — код из 3 букв")
        String currency,

        @NotNull(message = "Начальный баланс обязателен")
        @DecimalMin(value = "0.00", message = "Начальный баланс не может быть отрицательным")
        BigDecimal initialBalance) {
}
