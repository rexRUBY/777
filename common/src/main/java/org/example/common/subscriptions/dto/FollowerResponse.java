package org.example.common.subscriptions.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FollowerResponse {
    private final String FollowingUserName;
    private final String cryptoSymbol;
    private final String FollowingEmail;
    private final Double cryptoAmount;
    private final LocalDateTime createdAt;

    public FollowerResponse(String FollowingUserName, String cryptoSymbol, String FollowingEmail, Double cryptoAmount, LocalDateTime createdAt) {
        this.FollowingUserName = FollowingUserName;
        this.cryptoSymbol = cryptoSymbol;
        this.FollowingEmail = FollowingEmail;
        this.cryptoAmount = cryptoAmount;
        this.createdAt = createdAt;
    }
}
