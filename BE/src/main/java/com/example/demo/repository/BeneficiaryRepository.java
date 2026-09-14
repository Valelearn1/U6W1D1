package com.example.demo.repository;

import com.example.demo.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {

    List<Beneficiary> findByUser_IdOrderByNomeAsc(UUID userId);

    Optional<Beneficiary> findByIdAndUser_Id(UUID id, UUID userId);

    boolean existsByUser_IdAndIban(UUID userId, String iban);
}
