package com.example.saldo.repository;

import com.example.saldo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByWorkspaceId(UUID workspaceId);
}
