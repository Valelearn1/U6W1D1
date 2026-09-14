package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renderizza le email su file dentro target/mail-preview/ così da poterle
 * aprire nel browser e controllarle senza inviare niente a nessuno.
 *
 * Utile ogni volta che si modifica un template: si lancia il test e si guarda
 * il risultato, invece di registrarsi di nuovo per vedere la mail.
 */
@SpringBootTest
class MailTemplateRenderTest {

    @Autowired
    private TemplateEngine templateEngine;

    private String render(String template, String oggetto, Map<String, Object> variabili) {
        Context ctx = new Context();
        ctx.setVariables(variabili);
        ctx.setVariable("titolo", oggetto);
        ctx.setVariable("anteprima", oggetto);
        ctx.setVariable("contenuto", template + " :: contenuto");
        return templateEngine.process("mail/layout", ctx);
    }

    private void salva(String nome, String html) throws Exception {
        Path dir = Path.of("target", "mail-preview");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(nome + ".html"), html);
    }

    @Test
    void renderizzaTutteLeEmail() throws Exception {
        String registrazione = render("mail/registrazione", "Conferma la tua registrazione",
                Map.of("nome", "Giulia",
                        "codice", "Zc7Y0XSEoujG55jqmMpz08fiiPZj2qVc",
                        "link", "http://localhost:5173/verifica?email=giulia%40example.com&codice=Zc7Y0X"));

        String accesso = render("mail/codice-accesso", "Codice di accesso",
                Map.of("codice", "482913"));

        String bonifico = render("mail/codice-bonifico", "Conferma il tuo bonifico",
                Map.of("codice", "739204",
                        "importo", "450,00 €",
                        "destinatario", "IT87 9206 0453 4303 3672 7383 9564"));

        salva("registrazione", registrazione);
        salva("codice-accesso", accesso);
        salva("codice-bonifico", bonifico);

        // Le variabili devono essere state sostituite davvero
        assertThat(registrazione).contains("Giulia").doesNotContain("th:text");
        assertThat(accesso).contains("482913");
        assertThat(bonifico).contains("450,00 €").contains("739204");

        // Il layout comune deve essere presente in tutte
        assertThat(registrazione).contains("Nexa Bank").contains("non rispondere");
        assertThat(accesso).contains("Nexa Bank");
        assertThat(bonifico).contains("Nexa Bank");
    }
}
