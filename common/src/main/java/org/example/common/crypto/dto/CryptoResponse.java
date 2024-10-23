package org.example.common.crypto.dto;

import lombok.Getter;
import org.example.common.crypto.entity.Crypto;

@Getter
public class CryptoResponse {
    private Long id;
    private String symbol;
    private String description;

    public CryptoResponse(Crypto crypto) {
        this.id = crypto.getId();
        this.symbol = crypto.getSymbol();
        this.description = crypto.getDescription();
    }
}
