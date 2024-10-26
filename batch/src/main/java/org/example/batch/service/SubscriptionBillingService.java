package org.example.batch.service;

import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.user.entity.User;
import org.example.common.wallet.entity.Wallet;
import org.example.common.webclient.Util.DateTimeUtil;
import org.example.common.webclient.service.CryptoWebService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubscriptionBillingService {

    private final CryptoWebService cryptoWebService;

    public SubscriptionBillingService(CryptoWebService cryptoWebService) {
        this.cryptoWebService = cryptoWebService;
    }

    public void billCheck(User user, String cryptoSymbol) {
        List<Subscriptions> followingList = user.getSubscriptionsIFollow(); // 내가 팔로우 누른 구독증
        List<Subscriptions> followerList = user.getSubscriptionsBeingFollowed(); // 나를 상대로 팔로우 누른 구독증

        Long price = cryptoWebService.getCryptoValueAsLong(cryptoSymbol, DateTimeUtil.getCurrentDate(),DateTimeUtil.getCurrentTime());

        // followingList가 null이 아니고 비어있지 않은 경우에만 처리
        if (followingList != null && !followingList.isEmpty()) {
            processSubscriptions(followingList, user, cryptoSymbol, 0.9);
        }

        // followerList가 null이 아니고 비어있지 않은 경우에만 처리
        if (followerList != null && !followerList.isEmpty()) {
            processSubscriptions(followerList, user, cryptoSymbol, 0.1);
        }
    }

    private void processSubscriptions(List<Subscriptions> subscriptionsList, User user, String cryptoSymbol, double percentage) {
        Wallet wallet = user.getWalletList().stream()
                .filter(w -> w.getCryptoSymbol().equals(cryptoSymbol))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("지갑이 없습니다."));

        subscriptionsList.stream()
                .filter(s -> s.getCrypto().getSymbol().equals(cryptoSymbol))
                .forEach(s -> {
                    Long price = cryptoWebService.getCryptoValueAsLong(cryptoSymbol, DateTimeUtil.getCurrentDate(), DateTimeUtil.getCurrentTime());
                    Long totalPrice = (long) (s.getCryptoAmount() * price);
                    s.checkout(totalPrice);
                    wallet.billing(totalPrice * percentage);
                });
    }

}
