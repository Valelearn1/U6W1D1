package com.example.demo.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponseDTO(
        UUID id,
        BigDecimal importo,
        /** Null per le ricariche. */
        String ibanMittente,
        String ibanDestinatario,
        String result,
        String tipo,
        String categoria,
        String descrizione,
        Instant createdAt,
        /** "OUT" se il denaro esce dal conto di chi guarda, "IN" se entra. */
        String direzione
) {
}
