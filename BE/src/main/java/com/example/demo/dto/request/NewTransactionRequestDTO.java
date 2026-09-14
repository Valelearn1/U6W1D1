package com.example.demo.dto.request;

import com.example.demo.entity.TransactionCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Nota: l'IBAN del mittente NON e' un dato di input.
 * Viene sempre ricavato dall'utente autenticato, altrimenti chiunque
 * potrebbe disporre bonifici dal conto di un altro.
 */
public record NewTransactionRequestDTO(
        @NotBlank(message = "IBAN destinatario obbligatorio")
        String ibanDestinatario,

        @NotNull(message = "Importo obbligatorio")
        @DecimalMin(value = "0.01", message = "L'importo deve essere positivo")
        @Digits(integer = 10, fraction = 2, message = "Importo non valido: massimo 2 decimali")
        BigDecimal importo,

        TransactionCategory categoria,

        @Size(max = 140, message = "Causale troppo lunga")
        String descrizione
) {
}
