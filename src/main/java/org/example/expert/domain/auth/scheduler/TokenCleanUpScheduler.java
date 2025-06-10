package org.example.expert.domain.auth.scheduler;


import lombok.RequiredArgsConstructor;
import org.example.expert.domain.auth.repository.RefreshTokenRepository;
import org.example.expert.domain.auth.repository.TokenBlacklistRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TokenCleanUpScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;


    // 매일 새벽 3시에 실행
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteAllByExpiresAtBefore(now);
        tokenBlacklistRepository.deleteAllByExpirationBefore(System.currentTimeMillis());
    }
}
