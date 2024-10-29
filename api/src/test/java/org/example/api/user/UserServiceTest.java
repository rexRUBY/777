package org.example.api.user;

import org.example.api.user.service.UserService;
import org.example.common.common.dto.AuthUser;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.user.dto.request.UserChangePasswordRequest;
import org.example.common.user.dto.request.UserWithdrawRequest;
import org.example.common.user.dto.response.UserResponse;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void 유저_조회_성공() {
        // given
        long userId = 1L;

        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", userId);
        ReflectionTestUtils.setField(mockUser, "userStatus", true); // 활성화된 사용자 설정

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // when
        UserResponse response = userService.getUser(userId);

        // then
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
    }

    @Test
    public void 유저_탈퇴_성공() {
        // given
        AuthUser authUser = AuthUser.from(1L, "test@example.com");
        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", authUser.getId());
        ReflectionTestUtils.setField(mockUser, "password", "encodedPassword");
        ReflectionTestUtils.setField(mockUser, "userStatus", true); // 활성화된 사용자 설정

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("correctPassword", mockUser.getPassword())).thenReturn(true);

        // when
        UserWithdrawRequest withdrawRequest = new UserWithdrawRequest("correctPassword");
        userService.withdrawUser(authUser, withdrawRequest);

        // then
        assertFalse(mockUser.isUserStatus());
    }

    @Test
    public void 비밀번호가_틀려서_탈퇴_실패() {
        // given
        AuthUser authUser = AuthUser.from(1L, "test@example.com");
        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", authUser.getId());
        ReflectionTestUtils.setField(mockUser, "password", "encodedPassword");

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPassword", mockUser.getPassword())).thenReturn(false);

        // when & then
        UserWithdrawRequest withdrawRequest = new UserWithdrawRequest("wrongPassword");
        Exception exception = assertThrows(InvalidRequestException.class, () -> {
            userService.withdrawUser(authUser, withdrawRequest);
        });

        assertEquals("비밀번호가 틀렸습니다.", exception.getMessage());
    }

    @Test
    public void 비밀번호_변경_성공() {
        // given
        AuthUser authUser = AuthUser.from(1L, "test@example.com");
        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", authUser.getId());
        ReflectionTestUtils.setField(mockUser, "password", "oldEncodedPassword");

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(mockUser));

        // 설정된 passwordEncoder.matches() 호출에 따른 예외 방지
        doReturn(true).when(passwordEncoder).matches("oldPassword", "oldEncodedPassword"); // 이전 비밀번호 일치 확인
        doReturn(false).when(passwordEncoder).matches("NewPassword1", "oldEncodedPassword"); // 새 비밀번호가 다름 확인
        when(passwordEncoder.encode("NewPassword1")).thenReturn("newEncodedPassword");

        // when
        UserChangePasswordRequest passwordRequest = new UserChangePasswordRequest("oldPassword", "NewPassword1");
        userService.changePassword(authUser, passwordRequest);

        // then
        assertEquals("newEncodedPassword", mockUser.getPassword());
    }

    @Test
    public void 기존과_같은_비밀번호로_변경_실패() {
        // given
        AuthUser authUser = AuthUser.from(1L, "test@example.com");
        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", authUser.getId());
        ReflectionTestUtils.setField(mockUser, "password", "oldEncodedPassword");

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("newPassword", mockUser.getPassword())).thenReturn(true);

        // when & then
        UserChangePasswordRequest passwordRequest = new UserChangePasswordRequest("oldPassword", "newPassword");
        Exception exception = assertThrows(InvalidRequestException.class, () -> {
            userService.changePassword(authUser, passwordRequest);
        });

        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
    }
}