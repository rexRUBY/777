package org.example.common.wallet.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.common.wallet.entity.Wallet;

@Getter
@RequiredArgsConstructor
public class WalletResponse {

    private final Long id;
    private final Double amount;
    private final String cryptoSymbol;
    private final String userEmail;
    private final Long cash;

    public WalletResponse(Wallet wallet) {
        this.id = wallet.getId();
        this.amount = wallet.getAmount();
        this.cryptoSymbol = wallet.getCryptoSymbol();
        this.userEmail = wallet.getUser().getEmail();
        this.cash = wallet.getCash();
    }
}
