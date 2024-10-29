package org.example.common.auth.dto.request;

import lombok.Getter;

@Getter
public class UnFollowResponse {

    private final String followingUserEmail;
    private final String coinSymbol;
    private final Double amount;
    private final Long totalPrice;

    public UnFollowResponse(String followingUserEmail, String coinSymbol, Double amount, Long totalPrice) {
        this.followingUserEmail = followingUserEmail;
        this.coinSymbol = coinSymbol;
        this.amount = amount;
        this.totalPrice = totalPrice;
    }

}
