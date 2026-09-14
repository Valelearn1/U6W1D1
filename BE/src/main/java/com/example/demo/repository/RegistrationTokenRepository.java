package com.example.demo.repository;

import com.example.demo.entity.RegistrationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RegistrationTokenRepository extends JpaRepository<RegistrationToken, UUID> {

    Optional<RegistrationToken> findByValueAndUser_Id(String value, UUID userId);
}
