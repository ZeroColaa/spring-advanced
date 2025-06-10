package org.example.expert.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class RefreshToken {

    @Id
    private Long userId;

    @Column(nullable = false, length = 512)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

//    public RefreshToken(Long userId, String token) {
//        this.userId = userId;
//        this.token = token;
//        this.expiresAt = LocalDateTime.now().plusDays(14);//14일
//    }

    public RefreshToken(Long userId, String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public void update(String newToken, LocalDateTime newExp) {
        this.token = newToken;
        this.expiresAt = newExp;
    }
}
