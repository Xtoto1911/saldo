package com.example.saldo.dto.auth;

public record LoginRequest(
        String login,
        String password
) {
}
