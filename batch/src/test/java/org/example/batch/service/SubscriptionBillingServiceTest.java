package org.example.batch.service;

import org.example.common.crypto.entity.Crypto;
import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.user.entity.User;
import org.example.common.wallet.entity.Wallet;
import org.example.common.webclient.service.CryptoWebService;
import org.example.common.webclient.util.DateTimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.lenient;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SubscriptionBillingServiceTest {

    @InjectMocks
    private SubscriptionBillingService subscriptionBillingService;

    @Mock
    private CryptoWebService cryptoWebService;

    @Mock
    private Wallet wallet;

    private User user1;
    private User user2;
    private Crypto crypto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 사용자 설정
        user1 = User.of("test@example.com", "password1!", "테스트 사용자");
        user2 = User.of("test2@example.com", "password1!", "테스트 사용자2");

        // 암호화폐 설정
        crypto = new Crypto();
        ReflectionTestUtils.setField(crypto, "symbol", "BTC");

        // 지갑 설정
        user1.getWalletList().add(wallet);
        user2.getWalletList().add(new Wallet(user2, 1.0, "BTC", 50000L, 1000L));

        // lenient를 사용하여 불필요한 stubbing을 무시
        lenient().when(wallet.getCryptoSymbol()).thenReturn("BTC");

        // 구독 설정
        Subscriptions subscription2 = Subscriptions.of(user2, user1, crypto, 1.0, 50000L);
        ReflectionTestUtils.setField(subscription2, "createdAt", LocalDateTime.now().minusMonths(1));
        user1.getSubscriptionsBeingFollowed().add(subscription2);

        // 불필요한 stubbing 무시 설정
        lenient().when(cryptoWebService.getCryptoValueAsLong(anyString(), anyString(), anyString())).thenReturn(50000L);
    }

    @Test
    void testBillCheck() {
        // 서비스 객체를 Spy로 설정하여 `processSubscriptions` 호출을 모니터링
        SubscriptionBillingService spyService = spy(subscriptionBillingService);

        // Given
        long currentPrice = 50000L;
        lenient().when(cryptoWebService.getCryptoValueAsLong("BTC", DateTimeUtil.getCurrentDate(), DateTimeUtil.getCurrentTime()))
                .thenReturn(currentPrice);

        // When
//        spyService.billCheck(user1, "BTC");

        // Then
        // billCheck 내부에서 processSubscriptions이 호출되었는지를 간접적으로 검증
        verify(wallet, times(1)).billing(anyDouble()); // wallet.billing이 호출되었는지 확인하여 processSubscriptions 호출 간접 검증
    }
}
