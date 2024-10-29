package org.example.api.user;

import org.example.api.user.service.UserService;
import org.example.common.common.dto.AuthUser;
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
}