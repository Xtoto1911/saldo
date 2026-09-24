package com.example.saldo.dto.workspace;

import com.example.saldo.entity.CategoryType;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @Size(max = 100, message = "Название категории — максимум 100 символов")
        String name,
        CategoryType type
) {
}
