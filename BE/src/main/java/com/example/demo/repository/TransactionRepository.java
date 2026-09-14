package com.example.demo.repository;

import com.example.demo.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /** Storico dei movimenti di un conto: sia inviati che ricevuti, dal piu' recente. */
    List<Transaction> findByIbanMittenteOrIbanDestinatarioOrderByCreatedAtDesc(
            String ibanMittente, String ibanDestinatario);
}
