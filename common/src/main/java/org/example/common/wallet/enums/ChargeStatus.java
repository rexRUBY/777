package org.example.common.wallet.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.trade.enums.TradeType;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ChargeStatus {
    CONFIRM(ChargeStatus.Authority.CONFIRM),
    REJECT(ChargeStatus.Authority.REJECT);

    private final String chargeStatus;

    public static ChargeStatus of(String status) {
        return Arrays.stream(ChargeStatus.values())
                .filter(r -> r.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("유효하지 않은 chargeStatus"));
    }

    public static class Authority {
        public static final String CONFIRM = "CONFIRM";
        public static final String REJECT = "REJECT";
    }
}
