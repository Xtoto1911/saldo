package com.example.saldo.dto.workspace;

import java.math.BigDecimal;
import java.util.UUID;

public interface CategorySpending {
    UUID getCategoryId();

    BigDecimal getTotal();
}
