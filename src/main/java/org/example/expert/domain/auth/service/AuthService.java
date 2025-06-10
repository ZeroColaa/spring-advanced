package org.example.expert.domain.auth.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.entity.RefreshToken;
import org.example.expert.domain.auth.entity.TokenBlacklist;
import org.example.expert.domain.auth.enums.BlacklistReason;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.auth.repository.RefreshTokenRepository;
import org.example.expert.domain.auth.repository.TokenBlacklistRepository;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.InvalidTokenException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.security.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.expert.domain.auth.enums.BlacklistReason;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new InvalidRequestException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        User newUser = new User(
                signupRequest.getEmail(),
                encodedPassword,
                userRole
        );
        User savedUser = userRepository.save(newUser);

        String accessToken = jwtUtil.createAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole().name());
        String refreshToken = jwtUtil.createRefreshToken();

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenTtl());
        refreshTokenRepository.save(new RefreshToken(savedUser.getId(), refreshToken, expiresAt));

        return new SignupResponse(accessToken, refreshToken);
    }

    @Transactional
    public SigninResponse signin(SigninRequest signinRequest) {

        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
                () -> new InvalidRequestException("가입되지 않은 유저입니다."));

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new AuthException("잘못된 비밀번호입니다.");
        }

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getUserRole().name());
        String refreshToken = jwtUtil.createRefreshToken();
        LocalDateTime newExpiresAt = LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenTtl());

        refreshTokenRepository.findById(user.getId())
                .ifPresentOrElse(
                        token -> token.update(refreshToken, newExpiresAt),
                        () -> refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken, newExpiresAt))
                );

        return new SigninResponse(accessToken, refreshToken);
    }

    public void signout(Long userId, String bearerToken) {
        String accessToken = jwtUtil.extractToken(bearerToken);
        Long tokenUserId = jwtUtil.extractUserId(accessToken);
        long expiration = jwtUtil.getExpiration(accessToken);


        // userId와 토큰 안의 userId 비교
        if (!userId.equals(tokenUserId)) {
            throw new InvalidTokenException("로그아웃 권한이 없습니다.");
        }

        tokenBlacklistRepository.save(new TokenBlacklist(accessToken, expiration, userId, BlacklistReason.LOGOUT));
        refreshTokenRepository.deleteById(userId);
    }

    //토큰 재발급
    public SigninResponse reissue(Long userId, String requestRefreshToken) {

        // 서명 + 만료 검증
        try {
            jwtUtil.validateToken(requestRefreshToken);
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Refresh Token 만료");
        } catch (JwtException e) {
            throw new InvalidTokenException("Refresh Token 위조 또는 잘못된 형식");
        }

        // DB 토큰 일치 여부
        RefreshToken savedToken = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("저장된 리프레시 토큰 없음"));

        if (!savedToken.getToken().equals(requestRefreshToken)) {
            throw new InvalidTokenException("Refresh Token 불일치");
        }
        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh Token 만료");
        }


        // Sliding Expiration: 새로 발급할 때마다 Refresh Token도 갱신
        // Fixed Expiration: Access Token만 새로 발급, Refresh Token은 유지

        // 새 토큰 발급 (Sliding Expiration)
        long refreshTtlSec = jwtUtil.getRefreshTokenTtl();
        String newRefresh = jwtUtil.createRefreshToken();
        LocalDateTime newExp = LocalDateTime.now().plusSeconds(refreshTtlSec);

        savedToken.update(newRefresh, newExp);
        refreshTokenRepository.save(savedToken);

        User user = userRepository.findById(userId).orElseThrow();
        String newAccess = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getUserRole().name());

        return new SigninResponse(newAccess, newRefresh);

    }


    //탈취 의심 토큰을 강제 차단하는 메서드(관리자용)
    public void blacklistCompromisedToken(String token, Long userId) {
        long expiration = jwtUtil.getExpiration(token);
        tokenBlacklistRepository.save(
                new TokenBlacklist(token, expiration, userId, BlacklistReason.COMPROMISED)
        );
    }


    public void invalidateUserTokens(Long userId, String accessToken) {
        refreshTokenRepository.deleteById(userId);
        long expiration = jwtUtil.getExpiration(accessToken);
        tokenBlacklistRepository.save(new TokenBlacklist(accessToken, expiration, userId, BlacklistReason.PASSWORD_CHANGED));
    }








//    private String getCurrentRequestAccessToken() {
//        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        String bearer = request.getHeader("Authorization");
//        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
//            return bearer.substring(7);
//        }
//        return null;
//    }
//
//    public void invalidateUserTokens(Long userId) {
//        // 리프레시 토큰 먼저 무효화
//        refreshTokenRepository.deleteById(userId);
//
//        // 액세스 토큰은 현재 SecurityContext에 있을 수도 있고 없을 수도 있음
//        // 이 메서드를 안전하게 사용하려면 직접 액세스 토큰을 인자로 넘기거나,
//        // 현재 사용자의 요청 헤더에서 추출해야 함
//
//
//        // 현재 인증된 사용자의 토큰을 블랙리스트에 넣기
//        String accessToken = getCurrentRequestAccessToken(); // 아래에서 구현
//        if (accessToken != null) {
//            long expiration = jwtUtil.getExpiration(accessToken);
//            tokenBlacklistRepository.save(
//                    new TokenBlacklist(accessToken, expiration, userId, BlacklistReason.PASSWORD_CHANGED)
//            );
//        }
//    }
}