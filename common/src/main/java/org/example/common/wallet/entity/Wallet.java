package org.example.common.wallet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.common.common.entity.Timestamped;
import org.example.common.user.entity.User;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "wallets", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
public class Wallet extends Timestamped {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Wallet(User user, Double amount, String cryptoSymbol,Long cryptoPrice,Long cash) {
        this.user = user;
        this.amount = amount;
        this.cryptoSymbol = cryptoSymbol;
        this.cryptoPrice = cryptoPrice;
        this.cash=cash;
    }

    public void update(Double l, long cash,Long cryptoPrice) {
        this.amount=l;
        this.cash=cash;
        this.cryptoPrice =cryptoPrice;
    }

    public void updateCash(double v,Long cryptoPrice) {
        this.cash=this.cash+ (long)v;
        this.cryptoPrice =cryptoPrice;
    }
    public void billing(double v) {
        this.cash=this.cash+ (long)v;
    }

    public void minusCoin(Double cryptoAmount) {
        this.amount=amount-cryptoAmount;
    }

}
