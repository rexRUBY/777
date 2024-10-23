package org.example.common.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.common.common.exception.InvalidRequestException;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TradeType {
    SELL(Authority.SELL),
    BUY(Authority.BUY);

    private final String tradeType;

    public static TradeType of(String role) {
        return Arrays.stream(TradeType.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("유효하지 않은 tradeType"));
    }

    public static class Authority {
        public static final String SELL = "SELL";
        public static final String BUY = "BUY";
    }
}
