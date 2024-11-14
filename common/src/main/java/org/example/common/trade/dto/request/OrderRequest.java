package org.example.common.trade.dto.request;

import lombok.Getter;

@Getter
public class OrderRequest {
    private Double price;
    private Double amount;
    private String tradeType;
    private String symbol;
    private Long userId;

}
