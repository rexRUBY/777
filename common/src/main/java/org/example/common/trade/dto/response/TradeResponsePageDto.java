package org.example.common.trade.dto.response;

import lombok.Getter;
import org.example.common.crypto.entity.Crypto;
import org.example.common.trade.entity.Trade;
import org.example.common.trade.enums.TradeFor;
import org.example.common.trade.enums.TradeType;
import org.example.common.user.entity.User;

import java.time.LocalDateTime;

@Getter
public class TradeResponsePageDto {
    private TradeType tradeType;
    private TradeFor tradeFor;
    private Double amount;
    private Long price;
    private Long totalPrice;
    private Long moneyFromUserId;
    private String moneyFromUserEmail;
    private String moneyFromUserName;
    private Crypto crypto;
    private LocalDateTime createdAt;

    public TradeResponsePageDto(Trade trade, User moneyFrom) {
        this.tradeFor = trade.getTradeFor();
        this.tradeType = trade.getTradeType();
        this.totalPrice = trade.getTotalPrice();
        this.price = trade.getPrice();
        this.amount = trade.getAmount();
        this.crypto = trade.getCrypto();
        this.createdAt = trade.getCreatedAt();

        if(moneyFrom != null) {
            this.moneyFromUserId = moneyFrom.getId();
            this.moneyFromUserEmail = moneyFrom.getEmail();
            this.moneyFromUserName = moneyFrom.getName();
        }
    }
}
