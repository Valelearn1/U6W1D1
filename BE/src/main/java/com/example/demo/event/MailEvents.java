package com.example.demo.event;

/**
 * Eventi pubblicati dai service quando c'e' una mail da inviare.
 * L'invio vero avviene solo dopo il COMMIT della transazione
 * (vedi MailEventListener): cosi' una SMTP lenta non tiene occupata
 * la connessione al database, e un errore di invio non annulla
 * un'operazione gia' andata a buon fine.
 */
public final class MailEvents {

    private MailEvents() {
    }

    public record RegistrationRequested(String email, String nome, String token) {
    }

    public record LoginCodeRequested(String email, String code) {
    }

    public record TransactionCodeRequested(String email, String code, String importo, String destinatario) {
    }
}
