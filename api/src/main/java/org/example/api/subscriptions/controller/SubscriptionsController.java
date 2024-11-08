package org.example.api.subscriptions.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.subscriptions.service.SubscriptionsService;
import org.example.common.auth.dto.request.UnFollowResponse;
import org.example.common.common.dto.AuthUser;
import org.example.common.subscriptions.dto.FollowerListResponse;
import org.example.common.subscriptions.dto.FollowingListResponse;
import org.example.common.subscriptions.dto.FollowingRequest;
import org.example.common.subscriptions.dto.FollowingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionsController {

    private final SubscriptionsService subscriptionsService;

    @PostMapping
    public ResponseEntity<FollowingResponse> subscribe(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody FollowingRequest followingRequest) {
        return ResponseEntity.ok(subscriptionsService.subscribe(authUser, followingRequest));
    }

    @GetMapping("/following/{page}/{size}")
    public ResponseEntity<FollowingListResponse> getFollowing(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable int page,
            @PathVariable int size
    ) {
        return ResponseEntity.ok(subscriptionsService.getFollowing(authUser, page, size));
    }

    @GetMapping("/follower/{page}/{size}")
    public ResponseEntity<FollowerListResponse> getFollower(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable int page,
            @PathVariable int size
    ) {
        return ResponseEntity.ok(subscriptionsService.getFollower(authUser, page, size));
    }

    @DeleteMapping("/{subscriptionsId}")
    public ResponseEntity<UnFollowResponse> unFollowing(@AuthenticationPrincipal AuthUser authUser,
                                                        @PathVariable long subscriptionsId){
        return ResponseEntity.ok(subscriptionsService.unFollowing(authUser,subscriptionsId));
    }
}
