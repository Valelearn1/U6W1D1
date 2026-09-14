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

        // La rubrica e' un semplice elenco di contatti: accetta qualsiasi IBAN
        // ben formato. Che il conto esista davvero viene verificato al momento
        // del bonifico, non del salvataggio.
        if (!iban.matches("IT\\d{26}")) {
            throw new IllegalArgumentException("IBAN non valido: servono IT seguito da 26 cifre");
        }
        if (iban.equals(user.getIban())) {
            throw new IllegalArgumentException("Non puoi salvare il tuo stesso conto in rubrica");
        }
        if (beneficiaryRepository.existsByUser_IdAndIban(user.getId(), iban)) {
            throw new IllegalArgumentException("Questo IBAN è già in rubrica");
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
        boolean interno = userRepository.findByIban(b.getIban()).isPresent();
        return new BeneficiaryResponseDTO(b.getId(), b.getNome(), b.getIban(), interno, b.getCreatedAt());
    }
}
