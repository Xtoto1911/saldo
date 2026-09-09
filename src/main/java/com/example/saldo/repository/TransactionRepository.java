package com.example.saldo.repository;

import com.example.saldo.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
