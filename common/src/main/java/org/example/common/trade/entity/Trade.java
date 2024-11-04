package org.example.common.trade.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.common.common.entity.Timestamped;
import org.example.common.crypto.entity.Crypto;
import org.example.common.trade.enums.TradeFor;
import org.example.common.trade.enums.TradeType;
import org.example.common.user.entity.User;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "trade", indexes = {
        @Index(name = "idx_trade_user", columnList = "user_id"),
        @Index(name = "idx_trade_type", columnList = "trade_type"),
        @Index(name = "idx_trade_for", columnList = "trade_for"),
        @Index(name = "idx_trade_id", columnList = "id")
})
public class Trade extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type")
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_for")
    private TradeFor tradeFor;

    @Column(name = "amount")
    private Double amount;
    @Column(name = "price")
    private Long price;
    @Column(name = "total_price")
    private Long totalPrice;
    @Column(name = "money_from")
    private Long moneyFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "crypto_id")
    private Crypto crypto;

    public Trade(User user, Crypto crypto, String tradeType,String tradeFor,Double amount, Long price,Long totalPrice,Long moneyFrom) {
        this.user=user;
        this.crypto=crypto;
        this.tradeType = TradeType.valueOf(tradeType);
        this.tradeFor = TradeFor.valueOf(tradeFor);
        this.amount=amount;
        this.price=price;
        this.totalPrice=totalPrice;
        this.moneyFrom = moneyFrom;
    }

}
