package com.example.saldo.dto.workspace;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AttachWalletRequest(
        @NotNull(message = "Кошелёк обязателен")
        UUID walletId
) {
}
