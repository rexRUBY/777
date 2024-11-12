package org.example.common.dataTest;

import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.subscriptions.repository.SubscriptionsRepository;
import org.example.common.user.entity.User;
import org.example.common.crypto.entity.Crypto;
import org.example.common.subscriptions.repository.SubscriptionsRepository;
import org.example.common.user.repository.UserRepository;
import org.example.common.crypto.repository.CryptoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class JdbcBulkInsert {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionsRepository subscriptionRepository;

    @Autowired
    private CryptoRepository cryptoRepository;

    private static final int TOTAL_SUBSCRIPTIONS = 1000000; // 생성할 더미 데이터 개수

    @Transactional
    public void createDummySubscriptions() {
        List<User> users = userRepository.findAll();
        List<Crypto> cryptos = cryptoRepository.findAll();
        Random random = new Random();

        for (int i = 0; i < TOTAL_SUBSCRIPTIONS; i++) {
            // 서로 다른 followingUser와 followerUser를 선택
            User followingUser;
            User followerUser;
            do {
                followingUser = users.get(random.nextInt(users.size()));
                followerUser = users.get(random.nextInt(users.size()));
            } while (followingUser.equals(followerUser));

            // 랜덤으로 Crypto 선택
            Crypto crypto = cryptos.get(random.nextInt(cryptos.size()));

            // 랜덤 가격 설정
            Long nowPrice = (long) (random.nextDouble() * 100000); // 예시로 0 ~ 100000 범위의 랜덤 가격 설정
            Double cryptoAmount = random.nextDouble() * 3.0; // 0 ~ 3 범위의 랜덤 cryptoAmount

            // Subscription 엔티티 생성
            Subscriptions subscription = Subscriptions.of(followingUser, followerUser, crypto, cryptoAmount, nowPrice);

            // 양방향 연관관계 설정
            followingUser.getSubscriptionsBeingFollowed().add(subscription);
            followerUser.getSubscriptionsIFollow().add(subscription);

            // Subscription 저장
            subscriptionRepository.save(subscription);

            // 배치 크기에 도달할 때마다 flush하여 메모리 사용을 최적화
            if (i % 1000 == 0) {
                subscriptionRepository.flush();
                userRepository.flush();
            }
        }
    }
}

