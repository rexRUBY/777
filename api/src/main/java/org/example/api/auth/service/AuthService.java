package org.example.api.auth.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.api.user.service.UserService;
import org.example.api.wallet.service.WalletService;
import org.example.common.auth.dto.request.ResetPasswordRequest;
import org.example.common.auth.dto.request.SigninRequest;
import org.example.common.auth.dto.request.SignupRequest;
import org.example.common.auth.dto.response.SigninResponse;
import org.example.common.auth.dto.response.SignupResponse;
import org.example.common.common.config.JwtUtil;
import org.example.common.common.exception.AuthException;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final WalletService walletService;
    private final UserService userService;
    
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validateEmail(request.getEmail());

        userService.validateNewPassword(request.getPassword());
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = User.of(request.getEmail(), encodedPassword, request.getName());

        User savedUser = userRepository.save(newUser);
        walletService.createWallet(savedUser);
        String token = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail());

        jwtUtil.addJwtToCookie(token);
        return SignupResponse.of(token);
    }

    public SigninResponse signin(SigninRequest request) {
        User user = findUserByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("잘못된 비밀번호입니다.");
        }

        String token = jwtUtil.createToken(user.getId(), user.getEmail());
        jwtUtil.addJwtToCookie(token);

        return SigninResponse.of(token);
    }

    @Transactional
    public void resetPassword(@Valid ResetPasswordRequest request) {
        User user = findUserByEmail(request.getEmail());

        validateResetToken(request.getEmail(), request.getToken());
        userService.validateNewPassword(request.getPassword());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.changePassword(encodedPassword);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidRequestException("가입되지 않은 사용자입니다."));
    }

    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new InvalidRequestException("이미 사용 중인 이메일입니다.");
        }
    }

    private void validateExistingUser(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new InvalidRequestException("가입된 사용자가 아닙니다.");
        }
    }

    private String generateRandomToken() {
        return String.valueOf(new Random().nextInt(100000000));
    }

    private void validateResetToken(String email, String token) {
        String storedToken = redisTemplate.opsForValue().get(email);
        if (!token.equals(storedToken)) {
            throw new InvalidRequestException("인증번호가 유효하지 않습니다.");
        }
    }
}