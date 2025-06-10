package org.example.expert.security.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.example.expert.domain.common.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;


@Component
public class JwtUtil {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_TIME = 15 * 60 * 1000L; // 15분
    private static final long REFRESH_TOKEN_TIME = 14L * 24 * 60 * 60 * 1000; // 14일

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String createAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        return BEARER_PREFIX + Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("userRole", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("토큰이 만료되었습니다.");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }
    }

    public String extractToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        throw new InvalidTokenException("잘못된 토큰 형식입니다.");
    }

    public long getExpiration(String token) {
        Date expiration = extractClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token); // 서명, 만료 포함 검증
        } catch (ExpiredJwtException e) {
            throw e; // 상위로 던짐
        } catch (JwtException | IllegalArgumentException e) {
            throw e; // 상위로 던짐
        }
    }

    public long getRefreshTokenTtl() {
        // 예: 14일을 초 단위로 반환 (14일 * 24시간 * 60분 * 60초)
        return 14 * 24 * 60 * 60L;
    }

    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        return Long.parseLong(claims.getSubject());
    }





//
//    public Pair<String, LocalDateTime> createRefreshTokenWithExpiration() {
//        String token = createRefreshToken();
//        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(getRefreshTokenTtl());
//        return Pair.of(token, expiresAt);
//    }
}