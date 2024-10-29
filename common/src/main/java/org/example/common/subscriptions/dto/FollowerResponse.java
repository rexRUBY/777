package org.example.common.subscriptions.dto;

import lombok.Getter;

@Getter
public class FollowerResponse {
    private final String FollowerUserName;
    private final String cryptoSymbol;

    public FollowerResponse(String FollowerUserName, String cryptoSymbol) {
        this.FollowerUserName = FollowerUserName;
        this.cryptoSymbol = cryptoSymbol;
    }
}
