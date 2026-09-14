package com.example.demo.service;

import com.example.demo.dto.request.DepositRequestDTO;
import com.example.demo.dto.request.ExecuteTransactionRequestDTO;
import com.example.demo.dto.request.NewTransactionRequestDTO;
import com.example.demo.dto.response.TransactionResponseDTO;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.TransactionCategory;
import com.example.demo.entity.TransactionResult;
import com.example.demo.entity.TransactionToken;
import com.example.demo.entity.TransactionType;
import com.example.demo.entity.User;
import com.example.demo.event.MailEvents;
import com.example.demo.exception.InsufficientFundsException;
import com.example.demo.exception.InvalidTokenException;
import com.example.demo.exception.InvalidTransactionStateException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.TokenExpiredException;
import com.example.demo.exception.TooManyAttemptsException;
import com.example.demo.exception.UserNotActiveException;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.TransactionTokenRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final int TRANSACTION_CODE_DIGITS = 6;
    private static final int MAX_ATTEMPTS = 5;

    private final TransactionRepository transactionRepository;
    private final TransactionTokenRepository transactionTokenRepository;
    private final UserRepository userRepository;
    private final TransactionStateService transactionStateService;
    private final ApplicationEventPublisher events;

    /**
     * L'IBAN del mittente viene dall'utente autenticato, mai dalla richiesta:
     * e' cosi' che si impedisce di disporre bonifici dal conto di qualcun altro.
     */
    @Transactional
    public TransactionResponseDTO newTransaction(NewTransactionRequestDTO dto, User mittente) {
        String ibanDestinatario = dto.ibanDestinatario().replaceAll("\\s+", "").toUpperCase();

        if (mittente.getIban().equals(ibanDestinatario)) {
            throw new IllegalArgumentException("Non puoi inviare denaro sul tuo stesso conto");
        }

        User destinatario = userRepository.findByIban(ibanDestinatario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Questo IBAN non appartiene a nessun conto Nexa Bank: "
                                + "in questa applicazione i bonifici sono possibili solo tra conti interni"));

        if (!destinatario.isActive()) {
            throw new UserNotActiveException("Il conto del destinatario non è attivo");
        }

        // Controllo preliminare: quello vincolante viene rifatto in fase di esecuzione
        if (mittente.getSaldo().compareTo(dto.importo()) < 0) {
            throw new InsufficientFundsException("Saldo insufficiente");
        }

        Transaction transaction = new Transaction();
        transaction.setImporto(dto.importo());
        transaction.setIbanMittente(mittente.getIban());
        transaction.setIbanDestinatario(ibanDestinatario);
        transaction.setResult(TransactionResult.WAITING);
        transaction.setTipo(TransactionType.TRANSFER);
        transaction.setCategoria(dto.categoria() != null ? dto.categoria() : TransactionCategory.ALTRO);
        transaction.setDescrizione(dto.descrizione() != null ? dto.descrizione().trim() : null);
        transaction = transactionRepository.save(transaction);

        BigDecimal code = TokenGeneratorUtil.randomNumericCode(TRANSACTION_CODE_DIGITS);
        TransactionToken token = new TransactionToken();
        token.setValue(code);
        token.setTransaction(transaction);
        token.setUsed(false);
        transactionTokenRepository.save(token);

        events.publishEvent(new MailEvents.TransactionCodeRequested(
                mittente.getEmail(),
                TokenGeneratorUtil.formatNumericCode(code, TRANSACTION_CODE_DIGITS),
                NumberFormat.getCurrencyInstance(Locale.ITALY).format(dto.importo()),
                ibanDestinatario));

        return toDTO(transaction, mittente.getIban());
    }

    @Transactional
    public TransactionResponseDTO executeTransaction(ExecuteTransactionRequestDTO dto, User utente) {
        Transaction transaction = transactionRepository.findById(dto.transactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Transazione non trovata"));

        // Solo il titolare del conto addebitato puo' confermare il bonifico
        if (!transaction.getIbanMittente().equals(utente.getIban())) {
            throw new AccessDeniedException("La transazione non appartiene a questo conto");
        }

        if (transaction.getResult() != TransactionResult.WAITING) {
            throw new InvalidTransactionStateException("La transazione è già stata processata");
        }

        TransactionToken token = transactionTokenRepository
                .findTopByTransaction_IdAndUsedFalseOrderByCreatedAtDesc(transaction.getId())
                .orElseThrow(() -> new InvalidTokenException("Nessun codice valido per questa transazione"));

        if (token.getExpireAt().isBefore(Instant.now())) {
            // Salvato in una transazione separata: altrimenti l'eccezione qui sotto
            // farebbe rollback e lo stato REJECT non verrebbe mai scritto.
            transactionStateService.markRejected(transaction.getId());
            throw new TokenExpiredException("Codice scaduto: transazione rifiutata");
        }

        if (token.getAttempts() >= MAX_ATTEMPTS) {
            transactionStateService.markRejected(transaction.getId());
            throw new TooManyAttemptsException("Troppi tentativi errati: transazione rifiutata");
        }

        if (token.getValue().compareTo(new BigDecimal(dto.transactionToken())) != 0) {
            transactionStateService.registerFailedAttempt(token.getId());
            throw new InvalidTokenException("Codice non valido");
        }

        // Blocco le righe dei due conti sempre in ordine alfabetico di IBAN:
        // se due bonifici incrociati partissero insieme senza un ordine comune,
        // ognuno aspetterebbe il lock dell'altro (deadlock).
        Map<String, User> conti = Stream.of(transaction.getIbanMittente(), transaction.getIbanDestinatario())
                .sorted()
                .collect(Collectors.toMap(
                        iban -> iban,
                        iban -> userRepository.findByIbanForUpdate(iban)
                                .orElseThrow(() -> new ResourceNotFoundException("Conto non trovato: " + iban)),
                        (a, b) -> a,
                        LinkedHashMap::new));

        User mittente = conti.get(transaction.getIbanMittente());
        User destinatario = conti.get(transaction.getIbanDestinatario());

        if (!mittente.isActive() || !destinatario.isActive()) {
            throw new UserNotActiveException("Uno dei due conti non è attivo");
        }

        // Ricontrollo vincolante: il saldo puo' essere cambiato dopo la creazione
        if (mittente.getSaldo().compareTo(transaction.getImporto()) < 0) {
            transactionStateService.markRejected(transaction.getId());
            throw new InsufficientFundsException("Saldo insufficiente: transazione rifiutata");
        }

        mittente.setSaldo(mittente.getSaldo().subtract(transaction.getImporto()));
        destinatario.setSaldo(destinatario.getSaldo().add(transaction.getImporto()));
        userRepository.save(mittente);
        userRepository.save(destinatario);

        token.setUsed(true);
        transactionTokenRepository.save(token);

        transaction.setResult(TransactionResult.SUCCESS);
        transactionRepository.save(transaction);

        return toDTO(transaction, utente.getIban());
    }

    /**
     * Ricarica simulata del proprio conto: il denaro arriva dall'esterno,
     * quindi non c'e' un IBAN mittente e non serve un codice di conferma.
     */
    @Transactional
    public TransactionResponseDTO deposit(DepositRequestDTO dto, User utente) {
        User conto = userRepository.findByIbanForUpdate(utente.getIban())
                .orElseThrow(() -> new ResourceNotFoundException("Conto non trovato"));

        if (!conto.isActive()) {
            throw new UserNotActiveException("Il conto non è attivo");
        }

        conto.setSaldo(conto.getSaldo().add(dto.importo()));
        userRepository.save(conto);

        Transaction transaction = new Transaction();
        transaction.setImporto(dto.importo());
        transaction.setIbanMittente(null);
        transaction.setIbanDestinatario(conto.getIban());
        transaction.setResult(TransactionResult.SUCCESS);
        transaction.setTipo(TransactionType.DEPOSIT);
        transaction.setCategoria(TransactionCategory.RICARICA);
        transaction.setDescrizione("Ricarica conto");
        transaction = transactionRepository.save(transaction);

        return toDTO(transaction, conto.getIban());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> history(User utente) {
        return transactionRepository
                .findByIbanMittenteOrIbanDestinatarioOrderByCreatedAtDesc(utente.getIban(), utente.getIban())
                .stream()
                .map(tx -> toDTO(tx, utente.getIban()))
                .toList();
    }

    private TransactionResponseDTO toDTO(Transaction transaction, String ibanUtente) {
        // Le ricariche non hanno mittente: sono sempre entrate
        boolean inUscita = ibanUtente.equals(transaction.getIbanMittente());
        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getImporto(),
                transaction.getIbanMittente(),
                transaction.getIbanDestinatario(),
                transaction.getResult().name(),
                transaction.getTipo().name(),
                transaction.getCategoria().name(),
                transaction.getDescrizione(),
                transaction.getCreatedAt(),
                inUscita ? "OUT" : "IN"
        );
    }
}
