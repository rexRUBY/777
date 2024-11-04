package org.example.common.wallet.dto.response;

import lombok.Getter;
import org.example.common.wallet.entity.WalletHistory;
import org.example.common.wallet.enums.ChargeStatus;

import java.time.LocalDateTime;

@Getter
public class WalletHistoryPageResponseDto {
    private Long id;
    private Long cash;
    private LocalDateTime createdAt;
    private ChargeStatus chargeStatus;

    public WalletHistoryPageResponseDto(WalletHistory walletHistory) {
        this.id = walletHistory.getId();
        this.cash = walletHistory.getCash();
        this.createdAt = walletHistory.getCreatedAt();
        this.chargeStatus = walletHistory.getChargeStatus();
    }
}
