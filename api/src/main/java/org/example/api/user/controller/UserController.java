package org.example.api.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.api.user.service.UserService;
import org.example.common.common.dto.AuthUser;
import org.example.common.user.dto.request.UserChangePasswordRequest;
import org.example.common.user.dto.request.UserWithdrawRequest;
import org.example.common.user.dto.response.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @DeleteMapping("/users")
    public void withdrawUser(@AuthenticationPrincipal AuthUser authUser,
                             @Valid @RequestBody UserWithdrawRequest userWithdrawRequest
    ) {
        userService.withdrawUser(authUser, userWithdrawRequest);
    }

    @PatchMapping("/users")
    public void changePassword(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UserChangePasswordRequest userChangePasswordRequest
    ) {
        userService.changePassword(authUser, userChangePasswordRequest);
    }

    @GetMapping("/users")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(userService.getMyInfo(authUser));
    }
}
