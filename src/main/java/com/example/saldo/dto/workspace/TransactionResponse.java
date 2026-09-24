package com.example.saldo.dto.workspace;

import com.example.saldo.entity.CategoryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID walletId,
        String walletName,
        UUID categoryId,
        String categoryName,
        CategoryType categoryType,
        BigDecimal amount,
        LocalDateTime occurredAt,
        String comment) {
}
