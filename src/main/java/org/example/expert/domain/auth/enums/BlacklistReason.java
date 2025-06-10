package org.example.expert.domain.auth.enums;


public enum BlacklistReason {
    LOGOUT,            // 사용자가 정상 로그아웃
    COMPROMISED,       // 탈취 의심(관리자 수동 차단)
    PASSWORD_CHANGED,  // 비밀번호 변경으로 기존 토큰 무효화
    DUPLICATE_LOGIN    // 중복 로그인 차단,,,,,,,
}