package org.example.common.crypto.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class CryptoListResponse {
    private List<String> symbolList;

    public CryptoListResponse(List<String> symbols) {
        this.symbolList = symbols;
    }
}
