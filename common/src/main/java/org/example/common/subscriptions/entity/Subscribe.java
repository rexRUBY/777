package org.example.common.subscriptions.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.trade.enums.TradeType;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Subscribe {
    ON(Subscribe.Authority.ON),
    OFF(Subscribe.Authority.OFF),
    PENDING(Subscribe.Authority.PENDING);
    private final String subscribe;

    public static Subscribe of(String role) {
        return Arrays.stream(Subscribe.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("유효하지 않은 Subscribe"));
    }

    public static class Authority {
        public static final String ON = "ON";
        public static final String OFF = "OFF";
        public static final String PENDING = "PENDING";
    }
}
