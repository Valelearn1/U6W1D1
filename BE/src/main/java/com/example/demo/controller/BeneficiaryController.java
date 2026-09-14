package com.example.demo.controller;

import com.example.demo.dto.request.BeneficiaryRequestDTO;
import com.example.demo.dto.response.BeneficiaryResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @GetMapping
    public ResponseEntity<List<BeneficiaryResponseDTO>> list(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(beneficiaryService.list(currentUser));
    }

    @PostMapping
    public ResponseEntity<BeneficiaryResponseDTO> create(
            @Valid @RequestBody BeneficiaryRequestDTO dto,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(beneficiaryService.create(dto, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal User currentUser) {
        beneficiaryService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
