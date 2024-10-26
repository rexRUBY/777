package org.example.common.trade.dto.request;

import lombok.Getter;

@Getter
public class TradeRequestDto {
    private Double amount;
    private String tradeType;
    private String tradeFor;
}
