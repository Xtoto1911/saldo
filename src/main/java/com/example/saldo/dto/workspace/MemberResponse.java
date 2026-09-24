package com.example.saldo.dto.workspace;

import com.example.saldo.entity.Role;

import java.util.UUID;

public record MemberResponse(
        UUID userId,
        String login,
        String email,
        Role role
) {
}
