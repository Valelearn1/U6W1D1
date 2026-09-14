package com.example.demo.controller;

import com.example.demo.dto.request.LoginCodeRequestDTO;
import com.example.demo.dto.request.LoginPasswordRequestDTO;
import com.example.demo.dto.request.LoginWithCodeRequestDTO;
import com.example.demo.dto.request.RegisterRequestDTO;
import com.example.demo.dto.request.VerifyRequestDTO;
import com.example.demo.dto.response.AuthResponseDTO;
import com.example.demo.dto.response.MessageResponseDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponseDTO("Registrazione completata. Controlla la tua email per attivare l'account."));
    }

    @PostMapping("/verify")
    public ResponseEntity<MessageResponseDTO> verify(@Valid @RequestBody VerifyRequestDTO dto) {
        userService.verify(dto);
        return ResponseEntity.ok(new MessageResponseDTO("Account attivato con successo."));
    }

    @PostMapping("/login/password")
    public ResponseEntity<AuthResponseDTO> loginWithPassword(@Valid @RequestBody LoginPasswordRequestDTO dto) {
        return ResponseEntity.ok(userService.loginWithPassword(dto));
    }

    @PostMapping("/login/code/request")
    public ResponseEntity<MessageResponseDTO> requestLoginCode(@Valid @RequestBody LoginCodeRequestDTO dto) {
        userService.requestLoginCode(dto);
        return ResponseEntity.ok(new MessageResponseDTO("Codice di accesso inviato via email."));
    }

    @PostMapping("/login/code")
    public ResponseEntity<AuthResponseDTO> loginWithCode(@Valid @RequestBody LoginWithCodeRequestDTO dto) {
        return ResponseEntity.ok(userService.loginWithCode(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getCurrentUser(currentUser));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDTO> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        // Il token viene aggiunto alla lista di revoca: da qui in poi non è più
        // utilizzabile, anche se la sua scadenza naturale è ancora lontana.
        if (authorization != null && authorization.startsWith("Bearer ")) {
            userService.logout(authorization.substring("Bearer ".length()));
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponseDTO("Logout effettuato."));
    }
}
