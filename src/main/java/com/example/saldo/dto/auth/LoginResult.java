package com.example.saldo.dto.auth;

public record LoginResult(
        String accessToken,
        String refreshToken
) {
}
