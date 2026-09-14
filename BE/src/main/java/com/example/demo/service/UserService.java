package com.example.demo.service;

import com.example.demo.dto.request.LoginCodeRequestDTO;
import com.example.demo.dto.request.LoginPasswordRequestDTO;
import com.example.demo.dto.request.LoginWithCodeRequestDTO;
import com.example.demo.dto.request.RegisterRequestDTO;
import com.example.demo.dto.request.VerifyRequestDTO;
import com.example.demo.dto.response.AuthResponseDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.entity.LoginToken;
import com.example.demo.entity.RegistrationToken;
import com.example.demo.entity.RevokedToken;
import com.example.demo.entity.User;
import com.example.demo.event.MailEvents;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.InvalidTokenException;
import com.example.demo.exception.TokenExpiredException;
import com.example.demo.exception.TooManyAttemptsException;
import com.example.demo.exception.UserNotActiveException;
import com.example.demo.repository.LoginTokenRepository;
import com.example.demo.repository.RegistrationTokenRepository;
import com.example.demo.repository.RevokedTokenRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int LOGIN_CODE_DIGITS = 6;
    private static final int REGISTRATION_TOKEN_LENGTH = 32;
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_CODE_REQUESTS_PER_HOUR = 5;

    private final UserRepository userRepository;
    private final RegistrationTokenRepository registrationTokenRepository;
    private final LoginTokenRepository loginTokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final IbanGeneratorService ibanGeneratorService;
    private final JwtService jwtService;
    private final ApplicationEventPublisher events;

    @Transactional
    public void register(RegisterRequestDTO dto) {
        String email = normalizeEmail(dto.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email già registrata: " + email);
        }

        User user = new User();
        user.setNome(dto.nome().trim());
        user.setCognome(dto.cognome().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setIban(ibanGeneratorService.generate());
        user.setSaldo(BigDecimal.ZERO);
        user.setActive(false);
        user = userRepository.save(user);

        String value = TokenGeneratorUtil.randomAlphanumeric(REGISTRATION_TOKEN_LENGTH);
        RegistrationToken token = new RegistrationToken();
        token.setValue(value);
        token.setUser(user);
        token.setUsed(false);
        registrationTokenRepository.save(token);

        // La mail parte dopo il commit: vedi MailEventListener
        events.publishEvent(new MailEvents.RegistrationRequested(user.getEmail(), user.getNome(), value));
    }

    @Transactional
    public void verify(VerifyRequestDTO dto) {
        User user = userRepository.findByEmail(normalizeEmail(dto.email()))
                .orElseThrow(() -> new InvalidTokenException("Email o codice non validi"));

        RegistrationToken token = registrationTokenRepository
                .findByValueAndUser_Id(dto.codice().trim(), user.getId())
                .orElseThrow(() -> new InvalidTokenException("Email o codice non validi"));

        if (token.isUsed()) {
            throw new InvalidTokenException("Codice già utilizzato");
        }
        if (token.getExpireAt().isBefore(Instant.now())) {
            throw new TokenExpiredException("Codice scaduto");
        }

        token.setUsed(true);
        registrationTokenRepository.save(token);

        user.setActive(true);
        userRepository.save(user);
    }

    public AuthResponseDTO loginWithPassword(LoginPasswordRequestDTO dto) {
        // Messaggio identico per email inesistente e password sbagliata:
        // non deve essere possibile dedurre quali indirizzi sono registrati.
        User user = userRepository.findByEmail(normalizeEmail(dto.email()))
                .orElseThrow(() -> new InvalidCredentialsException("Credenziali non valide"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Credenziali non valide");
        }
        if (!user.isActive()) {
            throw new UserNotActiveException("Account non attivo: verifica la tua email prima di accedere");
        }

        return buildAuthResponse(user);
    }

    /**
     * Non rivela mai se l'indirizzo esiste: la risposta al chiamante e' sempre
     * la stessa, la mail parte solo se l'account c'e' ed e' attivo.
     */
    @Transactional
    public void requestLoginCode(LoginCodeRequestDTO dto) {
        Optional<User> found = userRepository.findByEmail(normalizeEmail(dto.email()))
                .filter(User::isActive);

        if (found.isEmpty()) {
            return;
        }
        User user = found.get();

        long recenti = loginTokenRepository.countRecentForUser(
                user.getId(), Instant.now().minus(1, ChronoUnit.HOURS));
        if (recenti >= MAX_CODE_REQUESTS_PER_HOUR) {
            throw new TooManyAttemptsException("Troppe richieste: riprova tra un'ora");
        }

        // I codici richiesti in precedenza smettono di essere validi
        loginTokenRepository.invalidateAllForUser(user.getId());

        BigDecimal code = TokenGeneratorUtil.randomNumericCode(LOGIN_CODE_DIGITS);
        LoginToken token = new LoginToken();
        token.setValue(code);
        token.setUser(user);
        token.setUsed(false);
        loginTokenRepository.save(token);

        events.publishEvent(new MailEvents.LoginCodeRequested(
                user.getEmail(), TokenGeneratorUtil.formatNumericCode(code, LOGIN_CODE_DIGITS)));
    }

    @Transactional
    public AuthResponseDTO loginWithCode(LoginWithCodeRequestDTO dto) {
        User user = userRepository.findByEmail(normalizeEmail(dto.email()))
                .orElseThrow(() -> new InvalidCredentialsException("Credenziali non valide"));

        if (!user.isActive()) {
            throw new UserNotActiveException("Account non attivo: verifica la tua email prima di accedere");
        }

        LoginToken token = loginTokenRepository
                .findTopByUser_IdAndUsedFalseOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new InvalidTokenException("Nessun codice valido: richiedine uno nuovo"));

        if (token.getExpireAt().isBefore(Instant.now())) {
            throw new TokenExpiredException("Codice scaduto: richiedine uno nuovo");
        }

        if (token.getAttempts() >= MAX_ATTEMPTS) {
            token.setUsed(true);
            loginTokenRepository.save(token);
            throw new TooManyAttemptsException("Troppi tentativi errati: richiedi un nuovo codice");
        }

        if (token.getValue().compareTo(new BigDecimal(dto.codice())) != 0) {
            token.setAttempts(token.getAttempts() + 1);
            loginTokenRepository.save(token);
            throw new InvalidTokenException("Codice non valido");
        }

        token.setUsed(true);
        loginTokenRepository.save(token);

        return buildAuthResponse(user);
    }

    /** Il token viene messo in lista di revoca: da qui in poi non vale piu'. */
    @Transactional
    public void logout(String jwt) {
        if (jwt == null || !jwtService.isValid(jwt)) {
            return;
        }
        String jti = jwtService.extractJti(jwt);
        if (jti != null && !revokedTokenRepository.existsByJti(jti)) {
            RevokedToken revoked = new RevokedToken();
            revoked.setJti(jti);
            revoked.setExpiresAt(jwtService.extractExpiration(jwt));
            revokedTokenRepository.save(revoked);
        }
    }

    public UserResponseDTO getCurrentUser(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getNome(),
                user.getCognome(),
                user.getEmail(),
                user.getIban(),
                user.getSaldo(),
                user.getCreatedAt(),
                user.isActive()
        );
    }

    private AuthResponseDTO buildAuthResponse(User user) {
        String jwt = jwtService.generateToken(user);
        return new AuthResponseDTO(jwt, "Bearer", jwtService.extractExpiration(jwt));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
