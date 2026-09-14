package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginCodeRequestDTO(
        @NotBlank
        @Email(message = "Email non valida")
        String email
) {
}
