package org.example.streaming.crypto.dto;

import lombok.Getter;

@Getter
public class CryptoDataDto {
    private String symbol;
    private String price;

    public CryptoDataDto(String symbol, String price) {
        this.symbol = symbol;
        this.price = price;
    }
}
