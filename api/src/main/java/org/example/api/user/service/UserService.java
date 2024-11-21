package org.example.api.user.service;

import lombok.RequiredArgsConstructor;
import org.example.common.common.dto.AuthUser;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.user.dto.request.UserChangePasswordRequest;
import org.example.common.user.dto.request.UserWithdrawRequest;
import org.example.common.user.dto.response.UserResponse;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUser(long userId) {
        User user = findValidUser(userId);
        return UserResponse.entityToDto(user);
    }

    @Transactional
    public void withdrawUser(AuthUser authUser, UserWithdrawRequest request) {
        User user = findValidUser(authUser.getId());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("비밀번호가 틀렸습니다.");
        }

        user.withdrawUser(); // 유저 탈퇴 처리
    }

    @Transactional
    public void changePassword(AuthUser authUser, UserChangePasswordRequest request) {
        User user = findValidUser(authUser.getId());

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    private User findValidUser(long userId) {
        return userRepository.findById(userId)
                .filter(User::isUserStatus)
                .orElseThrow(() -> new InvalidRequestException("유효하지 않은 사용자입니다."));
    }

    public UserResponse getMyInfo(AuthUser authUser) {
        return getUser(authUser.getId());
    }
}
