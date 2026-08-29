package com.example.saldo.dto.auth;

public record LoginRequest(
        String email,
        String password
) {
}
