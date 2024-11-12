package org.example.api.subscriptions.service;

import org.example.common.auth.dto.request.UnFollowResponse;
import org.example.common.common.dto.AuthUser;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.crypto.entity.Crypto;
import org.example.common.crypto.repository.CryptoRepository;
import org.example.common.subscriptions.dto.FollowerListResponse;
import org.example.common.subscriptions.dto.FollowingListResponse;
import org.example.common.subscriptions.dto.FollowingRequest;
import org.example.common.subscriptions.dto.FollowingResponse;
import org.example.common.subscriptions.entity.Billing;
import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.subscriptions.repository.BillingRepository;
import org.example.common.subscriptions.repository.SubscriptionsRepository;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.common.wallet.entity.Wallet;
import org.example.common.wallet.repository.WalletRepository;
import org.example.common.webclient.service.CryptoWebService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubscriptionsServiceTest {
    @Mock
    private SubscriptionsRepository subscriptionsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CryptoRepository cryptoRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private BillingRepository billingRepository;
    @Mock
    private CryptoWebService cryptoWebService;
    @InjectMocks
    private SubscriptionsService subscriptionsService;

    private User testUser1;
    private User testUser2;
    private AuthUser testAuthUser;
    private Crypto testCrypto;
    private Wallet testWallet1;
    private Wallet testWallet2;
    private FollowingRequest testFollowingRequest;
    private Subscriptions testSubscription;

    @BeforeEach
    public void setup() {
        testUser1 = User.of("test1@email.com", "@Abc1234", "test1");
        ReflectionTestUtils.setField(testUser1, "id", 1L);
        testUser2 = User.of("test2@email.com", "@Abc1234", "test2");
        ReflectionTestUtils.setField(testUser2, "id", 2L);
        testAuthUser = AuthUser.from(1L, "test1@email.com");
        testCrypto = new Crypto();
        ReflectionTestUtils.setField(testCrypto, "id", 1L);
        ReflectionTestUtils.setField(testCrypto, "symbol", "BTC");
        ReflectionTestUtils.setField(testCrypto, "description", "비트코인");
        testWallet1 = new Wallet(testUser1, 1.0, "BTC", 3000L, 1000000L);
        testWallet2 = new Wallet(testUser2, 0.1, "BTC", 3000L, 1000000L);
        testFollowingRequest = new FollowingRequest();
        ReflectionTestUtils.setField(testFollowingRequest, "followingUserId", 2L);
        ReflectionTestUtils.setField(testFollowingRequest, "cryptoId", 1L);
        ReflectionTestUtils.setField(testFollowingRequest, "cryptoAmount", 1.0);
        testSubscription = Subscriptions.of(testUser2, testUser1, testCrypto, 1.0, 100L);
        ReflectionTestUtils.setField(testSubscription, "id", 1L);
    }

    @Test
    @DisplayName("구독 생성 DB에 저장 성공")
    void subscribe() {
        // given
        given(cryptoRepository.findById(anyLong())).willReturn(Optional.of(testCrypto));
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser1));
        given(walletRepository.findByUserIdAndCryptoSymbol(anyLong(), any())).willReturn(testWallet1);
        given(cryptoWebService.getCryptoValueAsLong(any(), any(), any())).willReturn(100L);
        // when
        FollowingResponse response = subscriptionsService.subscribe(testAuthUser, testFollowingRequest);
        // then
        assertNotNull(response);
        assertEquals("test1", response.getFollowingUserName());
        assertEquals("BTC", response.getCryptoSymbol());
        assertEquals(0.0, testWallet1.getAmount());
        verify(subscriptionsRepository).save(any(Subscriptions.class));
    }

    @Test
    @DisplayName("구독 생성 본인 구독 예외")
    void subscribe_throw1() {
        // given
        given(cryptoRepository.findById(anyLong())).willReturn(Optional.of(testCrypto));
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser2));
        // when - then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> subscriptionsService.subscribe(testAuthUser, testFollowingRequest));
        assertEquals("you can't subscribe yourself", exception.getMessage());
    }

    @Test
    @DisplayName("구독 생성 보유 코인 보다 많이 구독 예외")
    void subscribe_throw2() {
        // given
        given(cryptoRepository.findById(anyLong())).willReturn(Optional.of(testCrypto));
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser1));
        given(walletRepository.findByUserIdAndCryptoSymbol(anyLong(), any())).willReturn(testWallet2);
        // when - then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> subscriptionsService.subscribe(testAuthUser, testFollowingRequest));
        assertEquals("you don't have such amount of coin", exception.getMessage());
    }
//
//    @Test
//    @DisplayName("사용자의 구독 목록을 가져옵니다.")
//    void getFollowing() {
//        // given
//        given(subscriptionsRepository.findAllByFollowerUserId(anyLong()))
//                .willReturn(List.of(testSubscription));
//
//        // when
//        FollowingListResponse response = subscriptionsService.getFollowing(testAuthUser);
//
//        // then
//        assertNotNull(response);
//        assertEquals(1, response.getSubscriptions().size());
//        assertEquals("test2", response.getSubscriptions().get(0).getFollowingUserName());
//        assertEquals("BTC", response.getSubscriptions().get(0).getCryptoSymbol());
//    }

//    @Test
//    @DisplayName("사용자의 팔로워 목록을 가져옵니다.")
//    void getFollower() {
//        // given
//        given(subscriptionsRepository.findAllByFollowingUserId(anyLong()))
//                .willReturn(List.of(testSubscription));
//        // when
//        FollowerListResponse response = subscriptionsService.getFollower(testAuthUser);
//        // then
//        assertNotNull(response);
//        assertEquals(1, response.getSubscriptions().size());
//        assertEquals("test1", response.getSubscriptions().get(0).getFollowerUserName());
//        assertEquals("BTC", response.getSubscriptions().get(0).getCryptoSymbol());
//    }

    @Test
    @DisplayName("구독 해제 및 거래 기록 저장 성공")
    void unFollowing() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser1));
        given(subscriptionsRepository.findById(anyLong())).willReturn(Optional.of(testSubscription));
        given(walletRepository.findByUserIdAndCryptoSymbol(anyLong(), any())).willReturn(testWallet1);
        given(walletRepository.findByUserIdAndCryptoSymbol(anyLong(), any())).willReturn(testWallet2);
        given(cryptoWebService.getCryptoValueAsLong(any(), any(), any())).willReturn(100L);
        // when
        UnFollowResponse response = subscriptionsService.unFollowing(testAuthUser, testSubscription.getId());
        // then
        assertNotNull(response);
        assertEquals("BTC", response.getCryptoSymbol());
        assertEquals(1.0, response.getAmount());
        verify(subscriptionsRepository).delete(testSubscription);
        verify(billingRepository).save(any(Billing.class));
    }

    @Test
    @DisplayName("구독 해제 내것이 아닌 구독 예외")
    void unFollowing_구독_내것이_아님() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser2));
        given(subscriptionsRepository.findById(anyLong())).willReturn(Optional.of(testSubscription));
        // when - then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> subscriptionsService.unFollowing(testAuthUser, testSubscription.getId()));
        assertEquals("that is not you're subscriptions", exception.getMessage());
    }

}