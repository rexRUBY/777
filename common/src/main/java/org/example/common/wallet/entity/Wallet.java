package org.example.common.wallet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.common.common.entity.Timestamped;
import org.example.common.user.entity.User;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "wallets")
public class Wallet extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ammount")
    private Double amount;

    @Column(name = "crypto_symbol")
    private String cryptoSymbol;

    @Column(name = "cash")
    private Long cash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Wallet(User user, Double amount, String cryptoSymbol,Long cash) {
        this.user = user;
        this.amount = amount;
        this.cryptoSymbol = cryptoSymbol;
        this.cash=cash;
    }

    public void update(Double l, long cash) {
        this.amount=l;
        this.cash=cash;
    }

    public void updateCash(double v) {
        this.cash=this.cash+ (long)v;
    }
}
