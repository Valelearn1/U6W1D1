package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendRegistrationEmail(String to, String nome, String token) {
        String link = "%s/verifica?email=%s&codice=%s".formatted(
                frontendUrl,
                URLEncoder.encode(to, StandardCharsets.UTF_8),
                token);

        send(to,
                "Conferma la tua registrazione",
                "mail/registrazione",
                Map.of("nome", nome, "codice", token, "link", link),
                "Attiva il tuo conto Nexa Bank",
                """
                Ciao %s, benvenuta in Nexa Bank.

                Per attivare il conto apri questo link (valido 24 ore):
                %s

                Oppure inserisci questo codice nella pagina di verifica:
                %s
                """.formatted(nome, link, token));
    }

    public void sendLoginCodeEmail(String to, String code) {
        send(to,
                "Codice di accesso",
                "mail/codice-accesso",
                Map.of("codice", code),
                "Il tuo codice di accesso: " + code,
                """
                Il tuo codice per accedere a Nexa Bank è: %s

                Scade tra 10 minuti e può essere usato una sola volta.
                Se non hai richiesto l'accesso, ignora questo messaggio.
                """.formatted(code));
    }

    public void sendTransactionCodeEmail(String to, String code, String importo, String destinatario) {
        send(to,
                "Conferma il tuo bonifico",
                "mail/codice-bonifico",
                Map.of("codice", code, "importo", importo, "destinatario", destinatario),
                "Codice per confermare il bonifico di " + importo,
                """
                Hai richiesto un bonifico di %s verso %s.

                Codice di conferma: %s

                Scade tra 10 minuti. Non condividerlo con nessuno:
                Nexa Bank non te lo chiederà mai per telefono o via email.
                """.formatted(importo, destinatario, code));
    }

    /**
     * Costruisce e invia il messaggio.
     *
     * Vengono incluse DUE versioni dello stesso contenuto (multipart/alternative):
     * quella HTML e quella di solo testo. I client che non mostrano l'HTML usano
     * la seconda, e avere entrambe riduce le probabilità di finire nello spam.
     */
    private void send(String to, String oggetto, String template,
                      Map<String, Object> variabili, String anteprima, String testoSemplice) {
        try {
            Context context = new Context();
            context.setVariables(variabili);
            context.setVariable("titolo", oggetto);
            context.setVariable("anteprima", anteprima);
            // Il template riempie il frammento "contenuto" dentro il layout comune
            context.setVariable("contenuto", template + " :: contenuto");

            String html = templateEngine.process("mail/layout", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(oggetto);
            helper.setText(testoSemplice, html);

            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new IllegalStateException("Invio email non riuscito: " + e.getMessage(), e);
        }
    }
}
