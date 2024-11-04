package org.example.common.crypto.dto;

import lombok.Getter;
import org.example.common.crypto.entity.Crypto;

import java.util.List;

@Getter
public class CryptoListResponse {
    private List<Crypto> symbolList;

    public CryptoListResponse(List<Crypto> symbols) {
        this.symbolList = symbols;
    }
}
