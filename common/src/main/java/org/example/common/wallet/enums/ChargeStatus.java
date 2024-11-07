package org.example.common.wallet.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.trade.enums.TradeType;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ChargeStatus {
    CHARGE(ChargeStatus.Authority.CHARGE),
    TRANSACTION(ChargeStatus.Authority.TRANSACTION);

    private final String chargeStatus;

    public static ChargeStatus of(String status) {
        return Arrays.stream(ChargeStatus.values())
                .filter(r -> r.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("유효하지 않은 chargeStatus"));
    }

    public static class Authority {
        public static final String CHARGE = "CHARGE";
        public static final String TRANSACTION = "TRANSACTION";
    }
}
