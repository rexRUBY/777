package org.example.common.crypto.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CryptoLatestPriceResponseDto {
    private String symbol;
    private List<String> prices;

    public CryptoLatestPriceResponseDto(String symbol, List<String> prices) {
        this.symbol = symbol;
        this.prices = prices;
    }
}
