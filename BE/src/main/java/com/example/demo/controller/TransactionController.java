package com.example.demo.controller;

import com.example.demo.dto.request.DepositRequestDTO;
import com.example.demo.dto.request.ExecuteTransactionRequestDTO;
import com.example.demo.dto.request.NewTransactionRequestDTO;
import com.example.demo.dto.response.TransactionResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> newTransaction(
            @Valid @RequestBody NewTransactionRequestDTO dto,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.newTransaction(dto, currentUser));
    }

    @PatchMapping("/execute")
    public ResponseEntity<TransactionResponseDTO> executeTransaction(
            @Valid @RequestBody ExecuteTransactionRequestDTO dto,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(transactionService.executeTransaction(dto, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> history(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(transactionService.history(currentUser));
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDTO> deposit(
            @Valid @RequestBody DepositRequestDTO dto,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.deposit(dto, currentUser));
    }
}
