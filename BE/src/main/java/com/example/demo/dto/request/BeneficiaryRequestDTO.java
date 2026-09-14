package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BeneficiaryRequestDTO(
        @NotBlank(message = "Il nome è obbligatorio")
        @Size(max = 60, message = "Nome troppo lungo")
        String nome,

        @NotBlank(message = "L'IBAN è obbligatorio")
        String iban
) {
}
