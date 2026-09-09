package com.example.saldo.dto.workspace;

import java.math.BigDecimal;

public record WalletRequest(
        String name,
        String currency,
        BigDecimal initialBalance)    {
}
