package org.example.api.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.api.auth.service.AuthCommandService;
import org.example.api.auth.service.AuthQueryService;
import org.example.common.auth.dto.request.ResetPasswordRequest;
import org.example.common.auth.dto.request.SigninRequest;
import org.example.common.auth.dto.request.SignupRequest;
import org.example.common.auth.dto.response.SigninResponse;
import org.example.common.auth.dto.response.SignupResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthCommandService authCommandService; // 명령 작업용 서비스
    private final AuthQueryService authQueryService;     // 조회 작업용 서비스

    @PostMapping("/auth/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(authCommandService.signup(signupRequest));
    }

    @PostMapping("/auth/signin")
    public ResponseEntity<SigninResponse> signin(@Valid @RequestBody SigninRequest signinRequest) {
        return ResponseEntity.ok(authQueryService.signin(signinRequest));
    }

    @PatchMapping("/auth/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        authCommandService.resetPassword(resetPasswordRequest);
        return ResponseEntity.noContent().build();
    }
}