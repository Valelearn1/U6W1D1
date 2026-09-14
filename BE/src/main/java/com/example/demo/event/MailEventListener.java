package com.example.demo.event;

import com.example.demo.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailEventListener {

    private final MailService mailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRegistration(MailEvents.RegistrationRequested event) {
        send(() -> mailService.sendRegistrationEmail(event.email(), event.nome(), event.token()), event.email());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLoginCode(MailEvents.LoginCodeRequested event) {
        send(() -> mailService.sendLoginCodeEmail(event.email(), event.code()), event.email());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCode(MailEvents.TransactionCodeRequested event) {
        send(() -> mailService.sendTransactionCodeEmail(
                event.email(), event.code(), event.importo(), event.destinatario()), event.email());
    }

    /**
     * Se l'invio fallisce lo registriamo nei log senza propagare l'errore:
     * il dato e' gia' stato salvato e non ha senso annullarlo.
     */
    private void send(Runnable action, String destinatario) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("Invio email a {} fallito: {}", destinatario, e.getMessage());
        }
    }
}
