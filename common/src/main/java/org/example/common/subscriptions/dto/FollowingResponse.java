package org.example.common.subscriptions.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FollowingResponse {
    private final String FollowingUserName;
    private final String cryptoSymbol;
    private String FollowingEmail;
    private Double cryptoAmount;
    private LocalDateTime createdAt;

    public FollowingResponse(String FollowingUserName, String cryptoSymbol) {
        this.FollowingUserName = FollowingUserName;
        this.cryptoSymbol = cryptoSymbol;
    }

    public FollowingResponse(String FollowingUserName, String cryptoSymbol, String FollowingEmail, Double cryptoAmount, LocalDateTime createdAt) {
        this.FollowingUserName = FollowingUserName;
        this.cryptoSymbol = cryptoSymbol;
        this.FollowingEmail = FollowingEmail;
        this.cryptoAmount = cryptoAmount;
        this.createdAt = createdAt;
    }
}
