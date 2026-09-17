package com.example.saldo.dto.workspace;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        UUID fromWalletId,
        String fromWalletName,
        UUID toWalletId,
        String toWalletName,
        BigDecimal amount,
        LocalDateTime occurredAt,
        String comment
) {
}
