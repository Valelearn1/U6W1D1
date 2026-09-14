package com.example.demo.repository;

import com.example.demo.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByIban(String iban);

    boolean existsByIban(String iban);

    /**
     * Carica l'utente bloccando la riga sul database fino a fine transazione
     * (SELECT ... FOR UPDATE). Serve durante un bonifico: senza lock due
     * richieste in parallelo leggerebbero lo stesso saldo e una delle due
     * scritture andrebbe persa.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.iban = :iban")
    Optional<User> findByIbanForUpdate(@Param("iban") String iban);
}
