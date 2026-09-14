package com.example.demo.dto.response;

import java.time.Instant;
import java.util.UUID;

public record BeneficiaryResponseDTO(
        UUID id,
        String nome,
        String iban,
        /** true se l'IBAN corrisponde a un conto Nexa Bank: solo verso questi si può bonificare. */
        boolean contoInterno,
        Instant createdAt
) {
}
