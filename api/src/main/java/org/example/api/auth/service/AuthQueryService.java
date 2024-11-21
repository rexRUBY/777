package org.example.api.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.common.auth.dto.request.SigninRequest;
import org.example.common.auth.dto.response.SigninResponse;
import org.example.common.common.config.JwtUtil;
import org.example.common.common.exception.AuthException;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.user.entity.UserDocument; // MongoDB 전용 UserDocument
import org.example.common.user.mongo.UserQueryRepository; // MongoDB 리포지토리
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthQueryService {

    private final UserQueryRepository userQueryRepository; // MongoDB 전용 리포지토리
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public SigninResponse signin(SigninRequest request) {
        // 사용자 조회
        UserDocument userDocument = findUserByEmail(request.getEmail());

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), userDocument.getPassword())) {
            throw new AuthException("잘못된 비밀번호입니다.");
        }

        // 로그인 토큰 생성
        String token = jwtUtil.createToken(Long.parseLong(userDocument.getId()), userDocument.getEmail());

        return SigninResponse.of(token);
    }

    private UserDocument findUserByEmail(String email) {
        return userQueryRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidRequestException("가입되지 않은 사용자입니다."));
    }
}