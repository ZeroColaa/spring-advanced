package org.example.expert.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.auth.exception.AuthException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Slf4j
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {

        // JwtFilter가 먼저 실행되어 request에 사용자 정보가 담겨 있음
        Object role = request.getAttribute("userRole");
        Object userId = request.getAttribute("userId");


        // 관리자가 아니면 401
        if (!"ADMIN".equals(role)) {
            throw new AuthException("관리자만 접근 가능합니다.");
        }

        // 인증 성공 로그
        log.info("[ADMIN AUTH] userId={} url={} authSuccessAt={}", userId, request.getRequestURI(), System.currentTimeMillis());


        return true;
    }
}
