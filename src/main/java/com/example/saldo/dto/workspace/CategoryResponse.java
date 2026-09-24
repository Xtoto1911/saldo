package com.example.saldo.dto.workspace;

import com.example.saldo.entity.CategoryType;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        CategoryType type) {
}
