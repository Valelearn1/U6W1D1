package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginWithCodeRequestDTO(
        @NotBlank
        @Email(message = "Email non valida")
        String email,

        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "Il codice deve avere 6 cifre")
        String codice
) {
}
