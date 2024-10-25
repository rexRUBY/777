package org.example.batch.service;

import org.example.common.crypto.entity.Crypto;
import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.trade.enums.TradeFor;
import org.example.common.user.entity.User;
import org.example.common.wallet.entity.Wallet;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class SubscriptionBillingService {

    private final RedisTemplate<String, Long> redisTemplate;

    public SubscriptionBillingService(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Wallet billCheck(User user, String cryptoSymbol) {
        List<Subscriptions> followingList = user.getSubscriptionsIFollow(); // 내가 팔로우 누른 구독증
        List<Subscriptions> followerList = user.getSubscriptionsBeingFollowed(); // 나를 상대로 팔로우 누른 구독증

        Long price = Optional.ofNullable(redisTemplate.opsForValue().get("btc")).orElse(0L);

        Wallet wallet = processSubscriptions(followingList, user, cryptoSymbol, 0.9);
        processSubscriptions(followerList, user, cryptoSymbol, 0.1);

        return wallet; // 사용자가 가진 지갑 반환
    }

    private Wallet processSubscriptions(List<Subscriptions> subscriptionsList, User user, String cryptoSymbol, double percentage) {
        return subscriptionsList.stream()
                .filter(s -> s.getCrypto().getSymbol().equals(cryptoSymbol))
                .map(s -> {
                    Wallet wallet = user.getWalletList().stream()
                            .filter(w -> w.getCryptoSymbol().equals(cryptoSymbol))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("지갑이 없습니다."));

                    Long price = Optional.ofNullable(redisTemplate.opsForValue().get(s.getCrypto().getSymbol())).orElse(0L);
                    Long totalPrice = (long) (s.getCryptoAmount() * price);
                    s.checkout(totalPrice);
                    wallet.billing(totalPrice * percentage);
                    return wallet;
                })
                .findFirst() // 첫 번째 지갑을 반환
                .orElseThrow(() -> new RuntimeException("지갑을 업데이트하지 못했습니다."));
    }
}
