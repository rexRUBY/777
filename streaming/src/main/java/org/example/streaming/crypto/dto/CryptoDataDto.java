package org.example.streaming.crypto.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CryptoDataDto {
    private String symbol;
    private String price;

    public CryptoDataDto(String symbol, String price) {
        this.symbol = symbol;
        this.price = price;
    }
}
