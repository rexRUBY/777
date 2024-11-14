package org.example.batch.service;

import lombok.RequiredArgsConstructor;
import org.example.common.subscriptions.entity.Subscribe;
import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.common.wallet.entity.Wallet;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionBillingService {

    private final UserRepository userRepository;

    public void billCheck(User user, String cryptoSymbol) {
        Long userId = user.getId();
        User foundUser = userRepository.findUserWithWalletList(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // subscriptionsBeingFollowed 컬렉션 로드
        userRepository.findUserWithSubscriptionsBeingFollowed(userId);

        // subscriptionsIFollow 컬렉션 로드
        userRepository.findUserWithSubscriptionsIFollow(userId);


        List<Subscriptions> followingList = getFilteredSubscriptions(foundUser.getSubscriptionsIFollow());
        List<Subscriptions> followerList = getFilteredSubscriptions(foundUser.getSubscriptionsBeingFollowed());

        // followingList가 비어있지 않은 경우에만 처리
        if (!followingList.isEmpty()) {
            processSubscriptions(followingList, foundUser, cryptoSymbol, 0.9);
        }

        // followerList가 비어있지 않은 경우에만 처리
        if (!followerList.isEmpty()) {
            processSubscriptions(followerList, foundUser, cryptoSymbol, 0.1);
        }
    }

    private List<Subscriptions> getFilteredSubscriptions(List<Subscriptions> subscriptions) {
        return subscriptions.stream()
                .filter(s -> s.getSubscribe().equals(Subscribe.PENDING))
                .toList(); // 필터링된 구독증 목록
    }

    private void processSubscriptions(List<Subscriptions> subscriptionsList, User user, String cryptoSymbol, double percentage) {
        Wallet wallet = user.getWalletList().stream()
                .filter(w -> w.getCryptoSymbol().equals(cryptoSymbol))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("지갑이 없습니다."));

        subscriptionsList.stream()
                .filter(s -> s.getCrypto().getSymbol().equals(cryptoSymbol))
                .forEach(s -> {
                    Long totalPrice = s.getFinalPrice();
                    s.checkout((long)(totalPrice / s.getCryptoAmount()));
                    wallet.billing(totalPrice * percentage);
                });
    }

    public void dateCheck(Subscriptions subscriptions, String cryptoSymbol, Long price){
        if(subscriptions.getCrypto().getSymbol().equals(cryptoSymbol)){
            subscriptions.checking(price);
        }
    }

    public void priceCheck(Subscriptions subscriptions, String cryptoSymbol, Long price){
        if(subscriptions.getCrypto().getSymbol().equals(cryptoSymbol)){
            subscriptions.checking(price);
        }
    }
}
