package com.example.saldo.repository;

import com.example.saldo.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    Page<Transfer> findAllByWorkspace_Id(UUID workspaceId, Pageable pageable);
}
