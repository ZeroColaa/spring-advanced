package org.example.expert.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.auth.repository.TokenBlacklistRepository;
import org.example.expert.security.userdetails.UserPrincipal;
import org.example.expert.security.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String bearer = request.getHeader("Authorization");
        if (!StringUtils.hasText(bearer) || !bearer.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = bearer.substring(7);
        try{

            if (tokenBlacklistRepository.existsByToken(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "이미 로그아웃된 토큰입니다.");
                return;
            }

            // 서명 및 만료 검증
            jwtUtil.validateToken(token); // 내부에서 ExpiredJwtException 또는 JwtException 발생 가능

            // 클레임 추출
            Claims claims = jwtUtil.extractClaims(token);

            UserPrincipal principal = UserPrincipal.of(
                    Long.parseLong(claims.getSubject()),
                    claims.get("email", String.class),
                    claims.get("userRole", String.class)
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
    }
        catch (ExpiredJwtException ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "ACCESS_EXPIRED");
            return;
        } catch (JwtException ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN");
            return;
        }catch (Exception ex){
            log.debug("JWT 처리 실패: {}", ex.getMessage());
            //SecurityContext 미설정 -> 이후 entry point에서 401 응답
        }
        filterChain.doFilter(request, response);
    }

}
