package org.example.batch.service;

import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.common.wallet.entity.Wallet;
import org.example.common.webclient.service.CryptoWebService;
import org.example.common.webclient.util.DateTimeUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class SubscriptionBillingService {

    private final CryptoWebService cryptoWebService;
    private final UserRepository userRepository;

    public SubscriptionBillingService(CryptoWebService cryptoWebService, UserRepository userRepository) {
        this.cryptoWebService = cryptoWebService;
        this.userRepository = userRepository;
    }

    @Transactional
    public void billCheck(User user, String cryptoSymbol) {
        Long userId = user.getId();
        User foundUser = userRepository.findUserWithWalletList(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // subscriptionsBeingFollowed 컬렉션 로드
        userRepository.findUserWithSubscriptionsBeingFollowed(userId);

        // subscriptionsIFollow 컬렉션 로드
        userRepository.findUserWithSubscriptionsIFollow(userId);

        Long price = cryptoWebService.getCryptoValueAsLong(cryptoSymbol, DateTimeUtil.getCurrentDate(), DateTimeUtil.getCurrentTime());

        List<Subscriptions> followingList = getFilteredSubscriptions(foundUser.getSubscriptionsIFollow(), price);
        List<Subscriptions> followerList = getFilteredSubscriptions(foundUser.getSubscriptionsBeingFollowed(), price);

        // followingList가 비어있지 않은 경우에만 처리
        if (!followingList.isEmpty()) {
            processSubscriptions(followingList, foundUser, cryptoSymbol, 0.9, price);
        }

        // followerList가 비어있지 않은 경우에만 처리
        if (!followerList.isEmpty()) {
            processSubscriptions(followerList, foundUser, cryptoSymbol, 0.1, price);
        }
    }

    private List<Subscriptions> getFilteredSubscriptions(List<Subscriptions> subscriptions, Long price) {
        return subscriptions.stream()
                .filter(s -> {
                    LocalDate createdAt = s.getCreatedAt().toLocalDate();
                    return (createdAt.isEqual(LocalDate.now().minusMonths(1))) || // 현재로부터 한 달 전과 동일
                            (createdAt.isAfter(LocalDate.now().minusMonths(1)) && // 한 달 이후이고
                                    (s.getNowPrice() * 1.05) <= price); // 가격 조건
                })
                .toList(); // 필터링된 구독증 목록
    }

    private void processSubscriptions(List<Subscriptions> subscriptionsList, User user, String cryptoSymbol, double percentage, long price) {
        Wallet wallet = user.getWalletList().stream()
                .filter(w -> w.getCryptoSymbol().equals(cryptoSymbol))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("지갑이 없습니다."));

        subscriptionsList.stream()
                .filter(s -> s.getCrypto().getSymbol().equals(cryptoSymbol))
                .forEach(s -> {
                    Long totalPrice = (long) (s.getCryptoAmount() * price);
                    s.checkout(price);
                    wallet.billing(totalPrice * percentage);
                });
    }
}
