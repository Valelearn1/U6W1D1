package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Contatto salvato in rubrica, per non dover riscrivere l'IBAN ogni volta.
 * Ogni rubrica appartiene a un singolo utente.
 */
@Entity
@Table(
        name = "beneficiaries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "iban"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false, length = 34)
    private String iban;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
