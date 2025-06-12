# SPRING ADVANCED

---

```markdown
#  JWT 인증 시스템 (Spring Security 기반)

Spring Security 기반으로 구축된 JWT 인증 시스템입니다. Access/Refresh Token 전략을 사용하며, 블랙리스트/스케줄러 등 보안성과 확장성을 고려한 구조로 설계되었습니다.

##  주요 기능

- Spring Security 기반 인증 처리
- Access Token + Refresh Token 발급 및 재발급
- 토큰 블랙리스트 (로그아웃/탈취/비밀번호 변경 대응)
- 매일 만료된 토큰 정리 스케줄러
- `@AuthenticationPrincipal`을 통한 인증 정보 접근


---

##  인증 흐름

1. 사용자 로그인 시 AccessToken + RefreshToken 발급
2. AccessToken은 매 요청 시 Authorization 헤더로 전송
3. RefreshToken은 `/reissue` 요청 시에만 사용
4. 로그아웃 시 AccessToken → 블랙리스트에 등록
5. 블랙리스트에 포함된 토큰은 모든 요청에서 차단
6. 매일 만료된 리프레시 토큰, 블랙리스트 토큰들은 자동 삭제

---

##  API 예시

###  로그인

```

POST /auth/signin
{
"email": "[test@example.com](mailto:test@example.com)",
"password": "12345678"
}

```

###  재발급

```

POST /reissue?userId=1
Headers: Refresh-Token: {리프레시 토큰}

```

###  로그아웃

```

POST /auth/signout
Headers: Authorization: Bearer {액세스 토큰}

````

---

##  인증 정보 접근

컨트롤러에서 `@AuthenticationPrincipal`을 사용하여 인증된 사용자 정보에 접근할 수 있습니다:

```java
@PostMapping("/todos/{id}/comments")
public ResponseEntity<?> saveComment(
    @AuthenticationPrincipal UserPrincipal principal,
    @PathVariable Long id,
    @RequestBody CommentRequest dto
) {
    return service.save(principal.getId(), id, dto);
}
````

---

##  향후 개선 사항

* 블랙리스트 저장소 Redis로 이전 (트래픽 증가 대비)
* JWT Key Rotation (보안 강화)
* 인증 요청 Prometheus 기반 모니터링


