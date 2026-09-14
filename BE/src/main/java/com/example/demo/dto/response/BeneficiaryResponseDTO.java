package com.example.demo.dto.response;

import java.time.Instant;
import java.util.UUID;

public record BeneficiaryResponseDTO(
        UUID id,
        String nome,
        String iban,
        Instant createdAt
) {
}
