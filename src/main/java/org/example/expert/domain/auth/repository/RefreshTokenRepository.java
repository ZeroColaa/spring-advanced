package org.example.expert.domain.auth.repository;

import org.example.expert.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    void deleteAllByExpiresAtBefore(LocalDateTime now);
}