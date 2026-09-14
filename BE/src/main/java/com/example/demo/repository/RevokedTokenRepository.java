package com.example.demo.repository;

import com.example.demo.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, UUID> {

    boolean existsByJti(String jti);

    /** Pulizia periodica: i token gia' scaduti non serve piu' tenerli in lista. */
    @Modifying
    @Query("DELETE FROM RevokedToken r WHERE r.expiresAt < :now")
    int deleteExpired(@Param("now") Instant now);
}
