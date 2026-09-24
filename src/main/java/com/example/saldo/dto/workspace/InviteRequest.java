package com.example.saldo.dto.workspace;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteRequest(
        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный email")
        String email
) {
}
