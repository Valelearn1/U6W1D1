package com.example.demo.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositRequestDTO(
        @NotNull(message = "Importo obbligatorio")
        @DecimalMin(value = "0.01", message = "L'importo deve essere positivo")
        @DecimalMax(value = "5000.00", message = "Ricarica massima: 5.000 €")
        @Digits(integer = 10, fraction = 2, message = "Importo non valido: massimo 2 decimali")
        BigDecimal importo
) {
}
