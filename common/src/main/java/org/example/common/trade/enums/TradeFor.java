package org.example.common.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.common.common.exception.InvalidRequestException;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TradeFor {
    SELF(Authority.SELF),
    OTHER(Authority.OTHER);

    private final String tradeType;

    public static TradeFor of(String role) {
        return Arrays.stream(TradeFor.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("유효하지 않은 tradeFor"));
    }

    public static class Authority {
        public static final String SELF = "SELF";
        public static final String OTHER = "OTHER";
    }
}
