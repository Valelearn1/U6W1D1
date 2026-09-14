package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importo;

    /** Null per le ricariche: il denaro arriva dall'esterno, non da un altro conto. */
    @Column(name = "iban_mitt")
    private String ibanMittente;

    @Column(name = "iban_dest", nullable = false)
    private String ibanDestinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionResult result = TransactionResult.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TransactionType tipo = TransactionType.TRANSFER;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 12)
    private TransactionCategory categoria = TransactionCategory.ALTRO;

    /** Causale scritta dall'utente, opzionale. */
    @Column(name = "descrizione", length = 140)
    private String descrizione;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
