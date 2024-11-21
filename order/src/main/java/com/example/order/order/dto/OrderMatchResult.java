package com.example.order.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderMatchResult {
    double remainingAmount;
    boolean isTradeStatus = false;

    public OrderMatchResult(double amount, boolean isTradeStatus) {
        this.remainingAmount = amount;
        this.isTradeStatus = isTradeStatus;
    }

    public OrderMatchResult() {

    }

    public boolean getIsTradeStatus() {
        return this.isTradeStatus;
    }
}
