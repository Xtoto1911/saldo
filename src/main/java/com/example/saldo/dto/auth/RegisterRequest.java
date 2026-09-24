package com.example.saldo.dto.auth;

public record RegisterRequest(
        String login,
        String email,
        String password
) {
}
