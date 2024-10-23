package org.example.common.subscriptions.dto;

import lombok.Getter;

@Getter
public class FollowingResponse {
    private final String FollowingUserName;
    private final String cryptoSymbol;

    public FollowingResponse(String FollowingUserName, String cryptoSymbol) {
        this.FollowingUserName = FollowingUserName;
        this.cryptoSymbol = cryptoSymbol;
    }
}
