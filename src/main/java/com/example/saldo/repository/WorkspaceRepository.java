package com.example.saldo.repository;

import com.example.saldo.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {


    @Query("""
        select distinct w
        from Workspace w
        join w.workspaceMembers wm
        where wm.user.id = :userId
    """)
    List<Workspace> findAllByUserId(@Param("userId") UUID userId);


}
