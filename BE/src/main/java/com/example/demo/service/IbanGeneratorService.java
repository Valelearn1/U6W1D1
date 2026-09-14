package com.example.demo.service;

import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class IbanGeneratorService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DIGITS = 26;

    private final UserRepository userRepository;

    public String generate() {
        String iban;
        do {
            StringBuilder sb = new StringBuilder("IT");
            for (int i = 0; i < DIGITS; i++) {
                sb.append(RANDOM.nextInt(10));
            }
            iban = sb.toString();
        } while (userRepository.existsByIban(iban));
        return iban;
    }
}
