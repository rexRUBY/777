package org.example.common.trade.dto.response;

import lombok.Getter;

@Getter
public class TradeResponseDto {
    private final String cryptoSymbol;
    private final Double amount;
    private final String billType;
    private final Long price;

    public TradeResponseDto(String cryptoSymbol, Double amount, String billType, Long price) {
        this.cryptoSymbol = cryptoSymbol;
        this.amount = amount;
        this.billType = billType;
        this.price = price;
    }
}
