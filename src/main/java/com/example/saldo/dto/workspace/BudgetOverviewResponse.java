package com.example.saldo.dto.workspace;

import java.math.BigDecimal;
import java.util.List;

public record BudgetOverviewResponse(
        String period,
        List<BudgetItemResponse> items,
        BigDecimal totalPlanned,
        BigDecimal totalFact
) {
}
