package com.example.demo.service;

import com.example.demo.dto.request.BeneficiaryRequestDTO;
import com.example.demo.dto.response.BeneficiaryResponseDTO;
import com.example.demo.entity.Beneficiary;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BeneficiaryRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BeneficiaryResponseDTO> list(User user) {
        return beneficiaryRepository.findByUser_IdOrderByNomeAsc(user.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public BeneficiaryResponseDTO create(BeneficiaryRequestDTO dto, User user) {
        String iban = dto.iban().replaceAll("\\s+", "").toUpperCase();

        if (iban.equals(user.getIban())) {
            throw new IllegalArgumentException("Non puoi salvare il tuo stesso conto in rubrica");
        }
        if (beneficiaryRepository.existsByUser_IdAndIban(user.getId(), iban)) {
            throw new IllegalArgumentException("Questo IBAN è già in rubrica");
        }
        // Si possono salvare solo conti che esistono davvero
        if (userRepository.findByIban(iban).isEmpty()) {
            throw new ResourceNotFoundException("Nessun conto trovato con questo IBAN");
        }

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setNome(dto.nome().trim());
        beneficiary.setIban(iban);
        beneficiary.setUser(user);
        return toDTO(beneficiaryRepository.save(beneficiary));
    }

    @Transactional
    public void delete(UUID id, User user) {
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiario non trovato"));
        beneficiaryRepository.delete(beneficiary);
    }

    private BeneficiaryResponseDTO toDTO(Beneficiary b) {
        return new BeneficiaryResponseDTO(b.getId(), b.getNome(), b.getIban(), b.getCreatedAt());
    }
}
