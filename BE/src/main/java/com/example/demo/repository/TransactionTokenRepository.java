package com.example.demo.repository;

import com.example.demo.entity.TransactionToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionTokenRepository extends JpaRepository<TransactionToken, UUID> {

    /**
     * Il codice valido di una transazione e' sempre l'ultimo emesso e non ancora usato.
     * Lo cerchiamo per transazione (non per valore) cosi' possiamo contare i tentativi
     * sbagliati invece di limitarci a non trovare nulla.
     */
    Optional<TransactionToken> findTopByTransaction_IdAndUsedFalseOrderByCreatedAtDesc(UUID transactionId);
}
