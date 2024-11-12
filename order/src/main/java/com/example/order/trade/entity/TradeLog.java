package com.example.order.trade.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "trade_logs")
public class TradeLog {

    @Id
    private String id;
    private String orderId;
    private Long userId;
    private Long oppositeUserId;
    private double price;
    private double amount;
    private String orderType;
    private String symbol;
    private Number timestamp;
    private String tradeStatus;

    public TradeLog(String orderId, Long userId, Long oppositeUserId, double price, double amount,
                    String orderType, String symbol, Number timestamp, String tradeStatus) {
        this.orderId = orderId;
        this.userId = userId;
        this.oppositeUserId = oppositeUserId;
        this.price = price;
        this.amount = amount;
        this.orderType = orderType;
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.tradeStatus = tradeStatus;
    }

}
