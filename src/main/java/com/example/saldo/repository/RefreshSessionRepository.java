package com.example.saldo.repository;

import com.example.saldo.entity.RefreshSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, UUID> {

    @Modifying
    @Query("""
            update RefreshSession r
            set r.revokedAt = :revokedAt
            where r.familyId = :familyId
                and r.revokedAt is null
    """)
    int revokeFamily(
            @Param("familyId") UUID familyId,
            @Param("revokedAt") Instant revokedAt
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select r
        from RefreshSession r
        where r.id = :id
    """)
    Optional<RefreshSession> findByIdForUpdate(
            @Param("id") UUID id
    );
}
