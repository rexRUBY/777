package org.example.common.subscriptions.dto;

import lombok.Getter;

@Getter
public class FollowingRequest {
    private Long followingUserId;
    private Long cryptoId;
    private Double cryptoAmount;
    private Integer maxPercent;
}
