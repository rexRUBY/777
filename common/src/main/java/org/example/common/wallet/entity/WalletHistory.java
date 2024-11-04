package org.example.common.wallet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.common.common.entity.Timestamped;
import org.example.common.user.entity.User;
import org.example.common.wallet.enums.ChargeStatus;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "wallethistory")
public class WalletHistory extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "crypto_symbol")
    private String cryptoSymbol;

    @Column(name = "crypto_price")
    private Long cryptoPrice;

    @Column(name = "cash")
    private Long cash;

    @Column(name = "charge_status")
    @Enumerated(EnumType.STRING)
    private ChargeStatus chargeStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public WalletHistory(Wallet wallet) {
        this.user = wallet.getUser();
        this.amount = wallet.getAmount();
        this.cryptoSymbol = wallet.getCryptoSymbol();
        this.cash= wallet.getCash();
        this.cryptoPrice = wallet.getCryptoPrice();
    }

    public WalletHistory(User user, String chargeAmount, ChargeStatus chargeStatus) {
        this.user = user;
        this.chargeStatus = chargeStatus;
        this.cash = Long.valueOf(chargeAmount);
    }
}
