package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Token JWT invalidati con il logout.
 * Il JWT di per se' e' stateless e resterebbe valido fino alla scadenza:
 * per far funzionare davvero il logout teniamo una lista dei token revocati
 * e la controlliamo a ogni richiesta.
 */
@Entity
@Table(name = "revoked_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Identificativo univoco del JWT (claim "jti"). */
    @Column(name = "jti", nullable = false, unique = true, length = 64)
    private String jti;

    /** Scadenza originale del token: dopo questa data la riga si puo' cancellare. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at", nullable = false, updatable = false)
    private Instant revokedAt;

    @PrePersist
    protected void onCreate() {
        if (revokedAt == null) {
            revokedAt = Instant.now();
        }
    }
}
