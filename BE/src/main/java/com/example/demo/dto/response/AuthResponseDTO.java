package com.example.demo.dto.response;

import java.time.Instant;

public record AuthResponseDTO(
        String token,
        String tokenType,
        Instant expiresAt
) {
}
