package com.example.saldo.repository;

import com.example.saldo.entity.Category;
import com.example.saldo.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByWorkspaceId(UUID workspaceId);

    Optional<Category> findByIdAndWorkspaceId(UUID id, UUID workspace);

    boolean existsByWorkspaceIdAndNameIgnoreCaseAndType(UUID ws, String name, CategoryType type);

}
