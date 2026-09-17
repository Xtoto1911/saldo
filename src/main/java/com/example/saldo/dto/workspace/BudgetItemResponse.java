package com.example.saldo.dto.workspace;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetItemResponse(
        UUID budgetId,
        UUID categoryId,
        String categoryName,
        BigDecimal planned,
        BigDecimal fact
) {
}
