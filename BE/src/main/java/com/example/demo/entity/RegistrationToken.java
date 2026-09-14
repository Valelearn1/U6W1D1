package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "registration_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 32)
    private String value;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expire_at", nullable = false)
    private Instant expireAt;

    @Column(name = "is_used", nullable = false)
    private boolean used = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (expireAt == null) {
            expireAt = createdAt.plus(24, ChronoUnit.HOURS);
        }
    }
}
