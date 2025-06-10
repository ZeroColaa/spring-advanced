package org.example.expert.domain.auth.repository;

import org.example.expert.domain.auth.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, String> {
    boolean existsByToken(String token);


    void deleteAllByExpirationBefore(Long timestamp);
}