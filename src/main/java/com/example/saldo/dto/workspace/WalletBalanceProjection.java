package com.example.saldo.dto.workspace;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletBalanceProjection {

    UUID getId();

    String getName();

    String getCurrency();

    BigDecimal getBalance();
}
