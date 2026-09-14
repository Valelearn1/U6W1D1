package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record ExecuteTransactionRequestDTO(
        @NotNull(message = "ID transazione obbligatorio")
        UUID transactionId,

        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "Il codice deve avere 6 cifre")
        String transactionToken
) {
}
