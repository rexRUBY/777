package org.example.api.auth;

import org.example.api.auth.service.AuthService;
import org.example.api.user.service.UserService;
import org.example.api.wallet.service.WalletService;
import org.example.common.auth.dto.request.SignupRequest;
import org.example.common.auth.dto.response.SignupResponse;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.common.common.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private WalletService walletService;
    @Mock
    private UserService userService;

    @Spy
    private User newUser = User.of("test@example.com", "encodedPassword", "testUser");

    @Test
    public void 회원가입_성공() {
        // given
        String validPassword = "Valid123@Password";
        SignupRequest request = new SignupRequest("test@example.com", validPassword, "testUser");

        String token = "generatedToken";

        // Stubbing
        when(passwordEncoder.encode(validPassword)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtUtil.createToken(isNull(), anyString())).thenReturn(token);
        doNothing().when(walletService).createWallet(any(User.class));
        doNothing().when(jwtUtil).addJwtToCookie(anyString());

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertNotNull(response, "SignupResponse 객체는 null이 아니어야 합니다.");
        assertEquals(token, response.getBearerToken(), "생성된 토큰이 일치해야 합니다.");

        // verify interactions
        verify(passwordEncoder).encode(validPassword);
        verify(userRepository).save(any(User.class));
        verify(walletService).createWallet(any(User.class));
        verify(jwtUtil).createToken(isNull(), anyString());
        verify(jwtUtil).addJwtToCookie(anyString());

        // validateNewPassword 메서드는 호출 검증하지 않음 (정적 메서드로서 검증 불가)
    }
}






