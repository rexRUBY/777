package org.example.api.auth.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.api.user.event.producer.UserEventPublisher;
import org.example.api.wallet.service.WalletService;
import org.example.common.auth.dto.request.ResetPasswordRequest;
import org.example.common.auth.dto.request.SignupRequest;
import org.example.common.auth.dto.response.SignupResponse;
import org.example.common.common.config.JwtUtil;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserCommandRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandService {

    private final UserCommandRepository userCommandRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final WalletService walletService;
    private final UserEventPublisher userEventPublisher; // Kafka 이벤트 퍼블리셔 주입

    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 여부 확인
        validateEmail(request.getEmail());

        // 비밀번호 암호화 및 사용자 생성
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = User.of(request.getEmail(), encodedPassword, request.getName());

        // 사용자 저장
        User savedUser = userCommandRepository.save(newUser);

        // 사용자 지갑 생성
        walletService.createWallet(savedUser);

        // Kafka 이벤트 발행
        userEventPublisher.publishUserCreatedEvent(savedUser);

        // JWT 토큰 생성 후 반환
        String token = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail());
        return SignupResponse.of(token);
    }

    public void resetPassword(@Valid ResetPasswordRequest request) {
        // 이메일로 사용자 조회
        User user = findUserByEmail(request.getEmail());

        // 인증 토큰 검증
        validateResetToken(request.getEmail(), request.getToken());

        // 비밀번호 암호화 후 변경
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.changePassword(encodedPassword);

        // 변경된 사용자 저장
        userCommandRepository.save(user);

        // Kafka 이벤트 발행
        userEventPublisher.publishUserUpdatedEvent(user);
    }

    private User findUserByEmail(String email) {
        return userCommandRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidRequestException("가입되지 않은 사용자입니다."));
    }

    private void validateEmail(String email) {
        if (userCommandRepository.existsByEmail(email)) {
            throw new InvalidRequestException("이미 사용 중인 이메일입니다.");
        }
    }

    private void validateResetToken(String email, String token) {
        String storedToken = redisTemplate.opsForValue().get(email);
        if (!token.equals(storedToken)) {
            throw new InvalidRequestException("인증번호가 유효하지 않습니다.");
        }
    }
}
