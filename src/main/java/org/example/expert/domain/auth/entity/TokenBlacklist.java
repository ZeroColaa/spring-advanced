package org.example.expert.domain.auth.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.auth.enums.BlacklistReason;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "token_blacklist")
public class TokenBlacklist {

    @Id
    private String token;

    private long expiration; // 만료 시각 (timestamp)

    // 추가 정보
    private Long userId; // 어떤 사용자의 토큰인지

    @Enumerated(EnumType.STRING)
    private BlacklistReason reason;    // 블랙리스트 사유

    public TokenBlacklist(String token, long expiration, Long userId, BlacklistReason reason) {
        this.token = token;
        this.expiration = expiration;
        this.userId = userId;
        this.reason = reason;
    }

}
