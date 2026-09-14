package com.example.demo.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String nome,
        String cognome,
        String email,
        String iban,
        BigDecimal saldo,
        Instant createdAt,
        boolean active
) {
}
