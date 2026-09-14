package com.example.demo.repository;

import com.example.demo.entity.LoginToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface LoginTokenRepository extends JpaRepository<LoginToken, UUID> {

    /** L'ultimo codice emesso e non ancora usato: e' l'unico che puo' essere valido. */
    Optional<LoginToken> findTopByUser_IdAndUsedFalseOrderByCreatedAtDesc(UUID userId);

    /** Quando si chiede un nuovo codice, i precedenti devono smettere di funzionare. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE LoginToken t SET t.used = true WHERE t.user.id = :userId AND t.used = false")
    int invalidateAllForUser(@Param("userId") UUID userId);

    /** Quanti codici sono stati richiesti di recente: serve a limitare gli abusi. */
    @Query("SELECT COUNT(t) FROM LoginToken t WHERE t.user.id = :userId AND t.createdAt > :since")
    long countRecentForUser(@Param("userId") UUID userId, @Param("since") Instant since);
}
