package com.example.saldo.dto.workspace;

import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        String name) {
}
