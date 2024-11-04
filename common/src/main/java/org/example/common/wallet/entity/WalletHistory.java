package org.example.common.wallet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.common.common.entity.Timestamped;
import org.example.common.user.entity.User;
@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "wallethistory", indexes = {
        @Index(name = "idx_wallet_history_user", columnList = "user_id"),
        @Index(name = "idx_wallet_history_crypto", columnList = "crypto_symbol"),
        @Index(name = "idx_wallet_history_id", columnList = "id")

})
public class WalletHistory extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount")
    private Double amount=0.0;

    @Column(name = "crypto_symbol")
    private String cryptoSymbol;

    @Column(name = "crypto_price")
    private Long cryptoPrice;

    @Column(name = "cash")
    private Long cash;

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

    public WalletHistory(User user, double v, String btc, long l, long i) {
        this.user=user;
        this.amount=v;
        this.cryptoSymbol=btc;
        this.cryptoPrice=l;
        this.cash=i;
    }
}
