package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "transaction_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, precision = 6, scale = 0)
    private BigDecimal value;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expire_at", nullable = false)
    private Instant expireAt;

    @Column(name = "is_used", nullable = false)
    private boolean used = false;

    /** Tentativi sbagliati: oltre una certa soglia il codice viene bruciato. */
    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (expireAt == null) {
            expireAt = createdAt.plus(10, ChronoUnit.MINUTES);
        }
    }
}
