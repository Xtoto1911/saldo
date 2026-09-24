package com.example.saldo.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkspaceRequest(
        @NotBlank(message = "Название пространства обязательно")
        @Size(max = 100, message = "Название пространства — максимум 100 символов")
        String name
) {
}
