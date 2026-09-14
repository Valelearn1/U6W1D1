package com.example.demo.service;

import com.example.demo.entity.TransactionResult;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.TransactionTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Scritture che devono sopravvivere al rollback del chiamante.
 *
 * Sta in una classe separata di proposito: se questi metodi fossero dentro
 * TransactionService, chiamarli dall'interno della stessa classe salterebbe
 * il proxy di Spring e REQUIRES_NEW verrebbe ignorato.
 */
@Service
@RequiredArgsConstructor
public class TransactionStateService {

    private final TransactionRepository transactionRepository;
    private final TransactionTokenRepository transactionTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRejected(UUID transactionId) {
        transactionRepository.findById(transactionId).ifPresent(tx -> {
            tx.setResult(TransactionResult.REJECT);
            transactionRepository.save(tx);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailedAttempt(UUID tokenId) {
        transactionTokenRepository.findById(tokenId).ifPresent(t -> {
            t.setAttempts(t.getAttempts() + 1);
            transactionTokenRepository.save(t);
        });
    }
}
