package com.example.demo.entity;

public enum TransactionType {
    /** Bonifico tra due conti dell'applicazione. */
    TRANSFER,
    /** Ricarica del proprio conto (denaro che entra dall'esterno). */
    DEPOSIT
}
