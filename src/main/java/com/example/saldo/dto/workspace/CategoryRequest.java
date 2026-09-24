package com.example.saldo.dto.workspace;

import com.example.saldo.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Название категории обязательно")
        @Size(max = 100, message = "Название категории — максимум 100 символов")
        String name,

        @NotNull(message = "Тип категории обязателен")
        CategoryType type
) {
}
