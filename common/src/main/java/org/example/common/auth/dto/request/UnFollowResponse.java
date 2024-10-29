package org.example.common.auth.dto.request;

import lombok.Getter;

@Getter
public class UnFollowResponse {

    private final String followingUserEmail;
    private final String cryptoSymbol;
    private final Double amount;
    private final Long totalPrice;

    public UnFollowResponse(String followingUserEmail, String cryptoSymbol, Double amount, Long totalPrice) {
        this.followingUserEmail = followingUserEmail;
        this.cryptoSymbol = cryptoSymbol;
        this.amount = amount;
        this.totalPrice = totalPrice;
    }

}
