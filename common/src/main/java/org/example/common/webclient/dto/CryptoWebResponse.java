package org.example.common.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.common.crypto.entity.Crypto;
@Setter
@Getter
@NoArgsConstructor
public class CryptoWebResponse {
    @JsonProperty("key")
    private String key;// Redis의 key 값 (ex: "crypto:ETH:timestamp:2024-10-25_11:47")

    @JsonProperty("value")
    private String value;       // Redis에서 받아온 시세 값 (ex: "3530197.9594236473")

    // 생성자
    public CryptoWebResponse(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public CryptoWebResponse(Crypto crypto, String key, String value) {
        this.key = key;
        this.value = value;
        // Crypto 엔티티 정보도 필요하다면 아래처럼 추가
        // 예를 들어 id, symbol, description과 같은 필드들이 추가 가능
        // this.id = crypto.getId();
        // this.symbol = crypto.getSymbol();
        // this.description = crypto.getDescription();
    }
}