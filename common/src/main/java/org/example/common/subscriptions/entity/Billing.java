package org.example.common.subscriptions.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.common.common.entity.Timestamped;
import org.example.common.crypto.entity.Crypto;
import org.example.common.user.entity.User;

@Getter
@Entity
@Table(name = "billing")
@NoArgsConstructor
public class Billing extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscribe")
    private Subscribe subscribe;

    @Column(name="final_price")
    private Long finalPrice;

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

    public static Billing of(Subscriptions subscriptions) {
        Billing billing = new Billing();
        billing.followingUser = subscriptions.getFollowingUser();
        billing.followerUser = subscriptions.getFollowerUser();
        billing.crypto = subscriptions.getCrypto();
        billing.cryptoAmount = subscriptions.getCryptoAmount();
        billing.finalPrice= subscriptions.getFinalPrice();
        billing.subscribe=Subscribe.OFF;
        return billing;
    }

}
