package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "Il nome è obbligatorio")
        String nome,

        @NotBlank(message = "Il cognome è obbligatorio")
        String cognome,

        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "Email non valida")
        String email,

        @NotBlank(message = "La password è obbligatoria")
        @Size(min = 8, message = "La password deve avere almeno 8 caratteri")
        String password
) {
}
