package com.example.saldo.dto.workspace;

import java.util.UUID;

public record OwnedWalletResponse(
        UUID id,
        String name,
        String currency
) {
}
