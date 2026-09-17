package com.example.saldo.repository;

import com.example.saldo.dto.workspace.CategorySpending;
import com.example.saldo.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @EntityGraph(attributePaths = {
            "wallet",
            "category"
    })
    Page<Transaction>  findAllByWorkspace_Id(
            UUID workspaceId,
            Pageable pageable
    );

    Optional<Transaction> findByIdAndWorkspace_Id(UUID id, UUID workspaceId);

    boolean existsByCategoryId(UUID categoryId);

    @Query("""
        select t.category.id as categoryId, sum(t.amount) as total
        from Transaction t join t.category c
        where t.workspace.id = :workspaceId
            and c.type = 'EXPENSE'
            and t.occurredAt >= :from
            and t.occurredAt < :to
        group by t.category.id
    """)
    List<CategorySpending> sumExpensesByCategory(
            @Param("workspaceId") UUID workspaceId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
