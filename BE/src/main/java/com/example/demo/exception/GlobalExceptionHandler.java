package com.example.demo.exception;

import com.example.demo.dto.response.MessageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<MessageResponseDTO> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<MessageResponseDTO> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<MessageResponseDTO> handleUserNotActive(UserNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<MessageResponseDTO> handleInvalidToken(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<MessageResponseDTO> handleTokenExpired(TokenExpiredException ex) {
        return ResponseEntity.status(HttpStatus.GONE).body(new MessageResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResponseDTO> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(InvalidTransactionStateException.class)
    public ResponseEntity<MessageResponseDTO> handleInvalidTransactionState(InvalidTransactionStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<MessageResponseDTO> handleTooManyAttempts(TooManyAttemptsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new MessageResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<MessageResponseDTO> handleInsufficientFunds(InsufficientFundsException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new MessageResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResponseDTO> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponseDTO("Operazione non consentita su questo conto"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Dati non validi");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(message));
    }
}
