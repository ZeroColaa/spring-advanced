package org.example.expert.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.security.userdetails.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/signup")
    public SignupResponse signup(@Valid @RequestBody SignupRequest signupRequest) {
        return authService.signup(signupRequest);
    }

    @PostMapping("/auth/signin")
    public SigninResponse login(@Valid @RequestBody SigninRequest signinRequest) {
        return authService.signin(signinRequest);
    }

    @Transactional
    @PostMapping("/auth/signout")
    public void logout(@AuthenticationPrincipal UserPrincipal principal, @RequestHeader("Authorization") String token) {
        authService.signout(principal.getId(), token);
    }

    @PostMapping("/reissue")
    public ResponseEntity<SigninResponse> reissue(
            @RequestParam Long userId,
            @RequestHeader("Refresh-Token") String refreshToken
    ) {
        SigninResponse response = authService.reissue(userId, refreshToken);
        return ResponseEntity.ok(response);
    }
}


