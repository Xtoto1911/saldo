package com.example.saldo.repository;

import com.example.saldo.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findAllByWorkspaceIdAndPeriod(UUID workspaceId, YearMonth period);

    Optional<Budget> findByIdAndWorkspaceId(UUID id, UUID workspaceId);

    boolean existsByWorkspaceIdAndCategoryIdAndPeriod(UUID workspaceId, UUID categoryId, YearMonth period);
}
