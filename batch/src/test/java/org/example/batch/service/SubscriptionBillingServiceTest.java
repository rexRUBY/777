/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionBillingServiceTest {

    @InjectMocks
    private SubscriptionBillingService subscriptionBillingService;

    @Mock
    private CryptoWebService cryptoWebService;

    @Mock
    private Wallet wallet; // Wallet을 mock으로 선언합니다.

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
        ReflectionTestUtils.setField(crypto, "symbol", "BTC"); // 기호 설정

        // 지갑 설정
        user1.getWalletList().add(wallet); // mock wallet을 사용합니다.
        user2.getWalletList().add(new Wallet(user2, 1.0, "BTC", 50000L, 1000L));
        given(wallet.getCryptoSymbol()).willReturn("BTC"); // getCryptoSymbol이 "BTC"를 반환하도록 설정합니다.
        doNothing().when(wallet).billing(eq(5000.0d)); // billing 메서드는 특별한 결과 없이 호출되므로 doNothing() 사용
        doNothing().when(wallet).billing(eq(45000.0d)); // billing 메서드는 특별한 결과 없이 호출되므로 doNothing() 사용
        doNothing().when(wallet).billing(0.0d); // 0에 대한 stubbing 추가

        mockStatic(DateTimeUtil.class);
        given(DateTimeUtil.getCurrentDate()).willReturn("2024-10-29");
        given(DateTimeUtil.getCurrentTime()).willReturn("11:11");

        // 구독 설정
        Subscriptions subscription2 = Subscriptions.of(user2, user1, crypto, 1.0, 50000L);
        ReflectionTestUtils.setField(subscription2, "createdAt", LocalDateTime.now().minusMonths(1));
        user1.getSubscriptionsBeingFollowed().add(subscription2);
    }

    @Test
    void testBillCheck() {
        // 주어진 값
        long currentPrice = 50000L; // 현재 가격 모킹
        given(cryptoWebService.getCryptoValueAsLong(eq("BTC"), eq("2024-10-29"), eq("11:11"))).willReturn(currentPrice);

        // 실행
        subscriptionBillingService.billCheck(user1, "BTC");

        // wallet.billing이 호출되었는지 검증
        verify(wallet,times(1)).billing(eq(0.0d)); // 0.9 비율로 호출되었는지 검증

        // 지갑의 현금이 업데이트되었는지 검증
        assertEquals(0, user1.getWalletList().get(0).getCash());
    }
}


*/
