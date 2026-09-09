package com.example.saldo.repository;

import com.example.saldo.dto.workspace.WalletBalanceProjection;
import com.example.saldo.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Query(value = """
        SELECT
            w.id AS id,
            w.name AS name,
            w.currency AS currency,

            w.initial_balance
            + COALESCE(tx.transaction_balance, 0)
            + COALESCE(incoming.incoming_amount, 0)
            - COALESCE(outgoing.outgoing_amount, 0)
            AS balance

        FROM wallet w

        JOIN workspace_wallet ww
            ON ww.wallet_id = w.id

        LEFT JOIN (
            SELECT
                t.wallet_id,
                SUM(
                    CASE
                        WHEN c.type = 'INCOME'
                            THEN t.amount
                        WHEN c.type = 'EXPENSE'
                            THEN -t.amount
                        ELSE 0
                    END
                ) AS transaction_balance
            FROM transaction t
            JOIN category c
                ON c.id = t.category_id
            GROUP BY t.wallet_id
        ) tx
            ON tx.wallet_id = w.id

        LEFT JOIN (
            SELECT
                to_wallet_id AS wallet_id,
                SUM(amount) AS incoming_amount
            FROM transfer
            GROUP BY to_wallet_id
        ) incoming
            ON incoming.wallet_id = w.id

        LEFT JOIN (
            SELECT
                from_wallet_id AS wallet_id,
                SUM(amount) AS outgoing_amount
            FROM transfer
            GROUP BY from_wallet_id
        ) outgoing
            ON outgoing.wallet_id = w.id

        WHERE ww.workspace_id = :workspaceId
        """,
            nativeQuery = true)
    List<WalletBalanceProjection> findWalletsWithBalanceByWorkspaceId(
            @Param("workspaceId") UUID workspaceId
    );


}
