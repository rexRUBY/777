package org.example.api.auth;

import org.example.api.auth.service.AuthService;
import org.example.api.user.service.UserService;
import org.example.api.wallet.service.WalletService;
import org.example.common.auth.dto.request.ResetPasswordRequest;
import org.example.common.auth.dto.request.SigninRequest;
import org.example.common.auth.dto.request.SignupRequest;
import org.example.common.auth.dto.response.SigninResponse;
import org.example.common.auth.dto.response.SignupResponse;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.common.common.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
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

        verify(passwordEncoder).encode(validPassword);
        verify(userRepository).save(any(User.class));
        verify(walletService).createWallet(any(User.class));
        verify(jwtUtil).createToken(isNull(), anyString());
        verify(jwtUtil).addJwtToCookie(anyString());

        // validateNewPassword 메서드는 호출 검증하지 않음 (정적 메서드로서 검증 불가)
    }

    @Test
    public void 로그인_성공() {
        // given
        String email = "test@example.com";
        String password = "Valid123@Password";
        SigninRequest request = new SigninRequest(email, password);
        String token = "generatedToken";

        // Stubbing
        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(newUser));
        when(passwordEncoder.matches(password, newUser.getPassword())).thenReturn(true);
        when(jwtUtil.createToken(isNull(), anyString())).thenReturn(token); // isNull()과 anyString() 사용
        doNothing().when(jwtUtil).addJwtToCookie(anyString());

        // when
        SigninResponse response = authService.signin(request);

        // then
        assertNotNull(response, "SigninResponse 객체는 null이 아니어야 한다.");
        assertEquals(token, response.getBearerToken(), "생성된 토큰이 일치해야 한다.");

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, newUser.getPassword());
        verify(jwtUtil).createToken(isNull(), anyString());
        verify(jwtUtil).addJwtToCookie(anyString());
    }

    @Test
    public void 비밀번호_재설정_성공() {
        // given
        String email = "test@example.com";
        String token = "resetToken";
        String newPassword = "NewValid123@Password";
        ResetPasswordRequest request = new ResetPasswordRequest(email, token, newPassword);

        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(email)).thenReturn(token); // 토큰 반환 설정

        // Stubbing
        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(newUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // when
        assertDoesNotThrow(() -> authService.resetPassword(request));

        verify(userRepository).findByEmail(email);
        verify(redisTemplate.opsForValue()).get(email);
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    public void 비밀번호_재설정_토큰_불일치() {
        // given
        String email = "test@example.com";
        String token = "wrongToken";
        ResetPasswordRequest request = new ResetPasswordRequest(email, token, "NewValid123@Password");

        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(email)).thenReturn("actualToken"); // 실제 토큰 설정 (다른 값 반환)

        // Stubbing
        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(newUser));

        // when & then
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> authService.resetPassword(request),
                "인증번호가 유효하지 않을 경우 예외가 발생해야 한다."
        );
        assertEquals("인증번호가 유효하지 않습니다.", exception.getMessage());
    }
}







