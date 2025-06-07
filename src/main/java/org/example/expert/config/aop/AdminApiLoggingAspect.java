package org.example.expert.config.aop;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@Aspect
@Component
@Slf4j
public class AdminApiLoggingAspect {


    @Around("execution(* org.example.expert.domain.comment.controller.CommentAdminController.*(..)) || " +
            "execution(* org.example.expert.domain.user.controller.UserAdminController.*(..))")
    public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        long requestedTime = System.currentTimeMillis();

        Object userId = request.getAttribute("userId");
        String url = request.getRequestURI();
        String method = request.getMethod();
        String requestBody = new ObjectMapper().writeValueAsString(joinPoint.getArgs());


        Object result = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            log.error("Admin API 호출 중 예외 발생", ex);
            throw ex;
        } finally {
            String responseBody = new ObjectMapper().writeValueAsString(result);

            log.info("[ADMIN API] userId={} method={} uri={} 요청시각={} ", userId, method, url, Instant.ofEpochMilli(requestedTime));
            log.info("️ 요청 본문: {}", requestBody);
            log.info(" 응답 본문: {}", responseBody);

        }
    }
}
