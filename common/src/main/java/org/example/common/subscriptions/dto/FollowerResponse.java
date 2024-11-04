package org.example.common.subscriptions.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FollowerResponse {
    private final String FollowerUserName;
    private final String cryptoSymbol;
    private final String FollowingEmail;
    private final Double cryptoAmount;
    private final LocalDateTime createdAt;


    public FollowerResponse(String FollowerUserName, String cryptoSymbol, String FollowingEmail, Double cryptoAmount, LocalDateTime createdAt) {
        this.FollowerUserName = FollowerUserName;
        this.cryptoSymbol = cryptoSymbol;
        this.FollowingEmail = FollowingEmail;
        this.cryptoAmount = cryptoAmount;
        this.createdAt = createdAt;
    }
}
