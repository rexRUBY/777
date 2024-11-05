package org.example.common.subscriptions.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.common.common.entity.Timestamped;
import org.example.common.crypto.entity.Crypto;
import org.example.common.user.entity.User;

@Getter
@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_following_user", columnList = "following_user_id"),
        @Index(name = "idx_follower_user", columnList = "follower_user_id"),
        @Index(name = "idx_crypto", columnList = "crypto_id"),
        @Index(name = "idx_subscribe", columnList = "subscribe"),
        @Index(name = "idx_now_price", columnList = "now_price")

})
@NoArgsConstructor
public class Subscriptions extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscribe")
    private Subscribe subscribe;

    @Column(name="now_price")
    private Long nowPrice; // 구독 신청 할 때의 가격

    @Column(name="final_price")
    private Long finalPrice; //정산 될 때의 총 가격

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_user_id") // 구독할 사람 Id
    private User followingUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_user_id") // 내 Id
    private User followerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crypto_id")
    private Crypto crypto;

    @Column(name = "crypto_amount")
    private Double cryptoAmount;

    public static Subscriptions of(User followingUser, User followerUser, Crypto crypto, Double cryptoAmount,Long nowPrice) {
        Subscriptions subscriptions = new Subscriptions();
        subscriptions.followingUser = followingUser;
        subscriptions.followerUser = followerUser;
        subscriptions.crypto = crypto;
        subscriptions.cryptoAmount = cryptoAmount;
        subscriptions.nowPrice = nowPrice;
        subscriptions.subscribe=Subscribe.ON;
        return subscriptions;
    }

    public void checkout(long price) {
        this.finalPrice= (long)(price*this.cryptoAmount);
        this.subscribe=Subscribe.OFF;
    }
    public void checking(long price){
        this.finalPrice = (long)(price*this.cryptoAmount);
        this.subscribe=Subscribe.PENDING;
    }
}
