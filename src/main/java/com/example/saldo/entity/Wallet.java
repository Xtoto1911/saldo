package com.example.saldo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(
            name = "initial_balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency;

    @OneToMany(mappedBy = "wallet")
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    @Transient
    public BigDecimal getBalance() {
        BigDecimal transactionsBalance = transactions.stream()
                .map(transaction -> {
                    if (transaction.getCategory().getType() == CategoryType.INCOME) {
                        return transaction.getAmount();
                    }

                    return transaction.getAmount().negate();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return initialBalance.add(transactionsBalance);
    }
}
