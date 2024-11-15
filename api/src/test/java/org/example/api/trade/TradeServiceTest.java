package org.example.api.trade;

import org.example.api.trade.service.TradeService;
import org.example.common.common.dto.AuthUser;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.crypto.entity.Crypto;
import org.example.common.crypto.repository.CryptoRepository;
import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.subscriptions.repository.BillingRepository;
import org.example.common.subscriptions.repository.SubscriptionsRepository;
import org.example.common.trade.dto.request.TradeRequestDto;
import org.example.common.trade.dto.response.TradeResponseDto;
import org.example.common.trade.entity.Trade;
import org.example.common.trade.enums.TradeType;
import org.example.common.trade.repository.TradeRepository;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.common.wallet.entity.Wallet;
import org.example.common.wallet.repository.WalletHistoryRepository;
import org.example.common.wallet.repository.WalletRepository;
import org.example.common.webclient.service.CryptoWebService;
import org.example.common.webclient.util.DateTimeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TradeServiceTest {

    @InjectMocks
    private TradeService tradeService;
    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CryptoRepository cryptoRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private BillingRepository billingRepository;
    @Mock
    private WalletHistoryRepository walletHistoryRepository;
    @Mock
    private CryptoWebService cryptoWebService;
    @Mock
    private SubscriptionsRepository subscriptionsRepository;
    @Mock
    private RestTemplate restTemplate;

    @Test
    public void 성공적으로_코인_구매() {
        // given
        AuthUser authUser = AuthUser.from(1L, "test@example.com");
        long cryptoId = 1L;

        TradeRequestDto tradeRequestDto = new TradeRequestDto();
        ReflectionTestUtils.setField(tradeRequestDto, "amount", 10.0);
        ReflectionTestUtils.setField(tradeRequestDto, "tradeType", TradeType.Authority.BUY);
        ReflectionTestUtils.setField(tradeRequestDto, "tradeFor", "SELF");

        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        Wallet wallet = new Wallet();
        ReflectionTestUtils.setField(wallet, "user", mockUser);
        ReflectionTestUtils.setField(wallet, "cryptoSymbol", "BTC");
        ReflectionTestUtils.setField(wallet, "cash", 200000L); // 구매 조건 충족
        ReflectionTestUtils.setField(wallet, "amount", 50.0);

        Crypto mockCrypto = new Crypto();
        ReflectionTestUtils.setField(mockCrypto, "id", cryptoId);
        ReflectionTestUtils.setField(mockCrypto, "symbol", "BTC");

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(mockUser));
        when(cryptoRepository.findById(cryptoId)).thenReturn(Optional.of(mockCrypto));
        when(walletRepository.findByUserIdAndCryptoSymbol(mockUser.getId(), "BTC")).thenReturn(wallet);
        when(cryptoWebService.getCryptoValueAsLong("BTC", DateTimeUtil.getCurrentDate(), DateTimeUtil.getCurrentTime()))
                .thenReturn(10000L);

        // when
        TradeResponseDto response = tradeService.postTrade(authUser, cryptoId, tradeRequestDto);

        // then
        assertNotNull(response);
        assertEquals("BTC", response.getCryptoSymbol());
        assertEquals(10.0, response.getAmount());
        assertEquals(100000L, response.getPrice()); // price * amount 계산값
    }

    @Test
    public void 코인_구매시_사용자가_존재하지_않음() {
        // given
        AuthUser authUser = AuthUser.from(1L, "test@example.com");
        long cryptoId = 1L;

        TradeRequestDto tradeRequestDto = new TradeRequestDto();
        ReflectionTestUtils.setField(tradeRequestDto, "amount", 10.0);
        ReflectionTestUtils.setField(tradeRequestDto, "tradeType", "BUY");
        ReflectionTestUtils.setField(tradeRequestDto, "tradeFor", "SELF");

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.empty());

        // when & then
        Exception exception = assertThrows(InvalidRequestException.class, () ->
                tradeService.postTrade(authUser, cryptoId, tradeRequestDto)
        );

        assertEquals("no such user", exception.getMessage());
    }

    @Test
    public void 성공적으로_코인_판매하기() {
        // given
        AuthUser authUser = AuthUser.from(1L, "test@example.com");
        long cryptoId = 1L;

        TradeRequestDto tradeRequestDto = new TradeRequestDto();
        ReflectionTestUtils.setField(tradeRequestDto, "amount", 5.0);
        ReflectionTestUtils.setField(tradeRequestDto, "tradeType", TradeType.Authority.SELL);
        ReflectionTestUtils.setField(tradeRequestDto, "tradeFor", "SELF");

        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        Wallet wallet = new Wallet();
        ReflectionTestUtils.setField(wallet, "user", mockUser);
        ReflectionTestUtils.setField(wallet, "cryptoSymbol", "BTC");
        ReflectionTestUtils.setField(wallet, "cash", 100000L);
        ReflectionTestUtils.setField(wallet, "amount", 50.0);

        Crypto mockCrypto = new Crypto();
        ReflectionTestUtils.setField(mockCrypto, "id", cryptoId);
        ReflectionTestUtils.setField(mockCrypto, "symbol", "BTC");

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(mockUser));
        when(cryptoRepository.findById(cryptoId)).thenReturn(Optional.of(mockCrypto));
        when(walletRepository.findByUserIdAndCryptoSymbol(mockUser.getId(), "BTC")).thenReturn(wallet);
        when(cryptoWebService.getCryptoValueAsLong("BTC", DateTimeUtil.getCurrentDate(), DateTimeUtil.getCurrentTime()))
                .thenReturn(10000L);

        // when
        TradeResponseDto response = tradeService.postTrade(authUser, cryptoId, tradeRequestDto);

        // then
        assertNotNull(response);
        assertEquals("BTC", response.getCryptoSymbol());
        assertEquals(5.0, response.getAmount());
        assertEquals(50000L, response.getPrice()); // price * amount 계산값
    }

    @Test
    public void 잔액_부족으로_구매_실패() {
        // given
        AuthUser authUser = AuthUser.from(1L, "test@example.com");
        long cryptoId = 1L;

        TradeRequestDto tradeRequestDto = new TradeRequestDto();
        ReflectionTestUtils.setField(tradeRequestDto, "amount", 10.0); // 구매할 수량 설정
        ReflectionTestUtils.setField(tradeRequestDto, "tradeType", "BUY");
        ReflectionTestUtils.setField(tradeRequestDto, "tradeFor", "SELF");

        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", 1L);
        ReflectionTestUtils.setField(mockUser, "name", "testUser");

        Crypto mockCrypto = new Crypto();
        ReflectionTestUtils.setField(mockCrypto, "id", cryptoId);
        ReflectionTestUtils.setField(mockCrypto, "symbol", "BTC");

        Wallet wallet = new Wallet();
        ReflectionTestUtils.setField(wallet, "user", mockUser);
        ReflectionTestUtils.setField(wallet, "cryptoSymbol", "BTC");
        ReflectionTestUtils.setField(wallet, "cash", 1000L); // 잔액을 1000으로 설정
        ReflectionTestUtils.setField(wallet, "amount", 5.0);

        // 구매하려는 코인의 가격 설정 (예: 개당 500L)
        Long price = 500L;
        when(cryptoWebService.getCryptoValueAsLong(mockCrypto.getSymbol(), DateTimeUtil.getCurrentDate(), DateTimeUtil.getCurrentTime()))
                .thenReturn(price);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(mockUser));
        when(cryptoRepository.findById(cryptoId)).thenReturn(Optional.of(mockCrypto));
        when(walletRepository.findByUserIdAndCryptoSymbol(mockUser.getId(), "BTC")).thenReturn(wallet);

        // when & then
        assertThrows(InvalidRequestException.class, () -> tradeService.postTrade(authUser, cryptoId, tradeRequestDto));
    }

    @Test
    public void 존재하지_않는_구독으로_거래_시_예외_발생() {
        // given
        AuthUser authUser = AuthUser.from(1L, "test@example.com");
        long cryptoId = 1L;
        long invalidSubscriptionId = 999L; // 존재하지 않는 구독 ID

        TradeRequestDto tradeRequestDto = new TradeRequestDto();
        ReflectionTestUtils.setField(tradeRequestDto, "amount", 5.0);
        ReflectionTestUtils.setField(tradeRequestDto, "tradeType", "SELL");
        ReflectionTestUtils.setField(tradeRequestDto, "tradeFor", "OTHER");

        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", 1L);
        ReflectionTestUtils.setField(mockUser, "name", "testUser");

        Crypto mockCrypto = new Crypto();
        ReflectionTestUtils.setField(mockCrypto, "id", cryptoId);
        ReflectionTestUtils.setField(mockCrypto, "symbol", "BTC");

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(mockUser));
        when(cryptoRepository.findById(cryptoId)).thenReturn(Optional.of(mockCrypto));
        when(subscriptionsRepository.findById(invalidSubscriptionId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(InvalidRequestException.class, () -> tradeService.postSubscriptionsTrade(authUser, cryptoId, invalidSubscriptionId, tradeRequestDto));
    }

    @Test
    public void 구독자와_다른_사용자_거래_시_예외_발생() {
        // given
        AuthUser authUser = AuthUser.from(1L, "test@example.com");
        long cryptoId = 1L;
        long subscriptionId = 2L;

        TradeRequestDto tradeRequestDto = new TradeRequestDto();
        ReflectionTestUtils.setField(tradeRequestDto, "amount", 5.0);
        ReflectionTestUtils.setField(tradeRequestDto, "tradeType", "SELL");
        ReflectionTestUtils.setField(tradeRequestDto, "tradeFor", "OTHER");

        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", 1L);
        ReflectionTestUtils.setField(mockUser, "name", "testUser");

        User differentUser = new User();
        ReflectionTestUtils.setField(differentUser, "id", 3L);

        Crypto mockCrypto = new Crypto();
        ReflectionTestUtils.setField(mockCrypto, "id", cryptoId);
        ReflectionTestUtils.setField(mockCrypto, "symbol", "BTC");

        Subscriptions mockSubscription = new Subscriptions();
        ReflectionTestUtils.setField(mockSubscription, "id", subscriptionId);
        ReflectionTestUtils.setField(mockSubscription, "followingUser", differentUser); // 다른 사용자 설정
        ReflectionTestUtils.setField(mockSubscription, "crypto", mockCrypto);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(mockUser));
        when(cryptoRepository.findById(cryptoId)).thenReturn(Optional.of(mockCrypto));
        when(subscriptionsRepository.findById(subscriptionId)).thenReturn(Optional.of(mockSubscription));

        // when & then
        assertThrows(InvalidRequestException.class, () -> tradeService.postSubscriptionsTrade(authUser, cryptoId, subscriptionId, tradeRequestDto));
    }

    @Test
    public void 성공적으로_특정_코인_거래_내역_조회() {
        // given
        AuthUser authUser = AuthUser.from(1L, "test@example.com");
        long cryptoId = 1L;

        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", 1L);
        ReflectionTestUtils.setField(mockUser, "name", "testUser");

        Crypto mockCrypto = new Crypto();
        ReflectionTestUtils.setField(mockCrypto, "id", cryptoId);
        ReflectionTestUtils.setField(mockCrypto, "symbol", "BTC");

        Trade trade1 = new Trade();
        ReflectionTestUtils.setField(trade1, "crypto", mockCrypto);
        ReflectionTestUtils.setField(trade1, "amount", 5.0);
        ReflectionTestUtils.setField(trade1, "tradeType", TradeType.BUY);
        ReflectionTestUtils.setField(trade1, "price", 500L);

        Trade trade2 = new Trade();
        ReflectionTestUtils.setField(trade2, "crypto", mockCrypto);
        ReflectionTestUtils.setField(trade2, "amount", 3.0);
        ReflectionTestUtils.setField(trade2, "tradeType", TradeType.SELL);
        ReflectionTestUtils.setField(trade2, "price", 300L);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(mockUser));
        when(cryptoRepository.findById(cryptoId)).thenReturn(Optional.of(mockCrypto));
        when(tradeRepository.findAllByCryptoAndUser(mockCrypto, mockUser)).thenReturn(List.of(trade1, trade2));

        // when
        List<TradeResponseDto> responseList = tradeService.getTradeList(authUser, cryptoId);

        // then
        assertEquals(2, responseList.size());
        assertEquals("BTC", responseList.get(0).getCryptoSymbol());
        assertEquals(5.0, responseList.get(0).getAmount());
        assertEquals("BUY", responseList.get(0).getBillType());

        assertEquals("BTC", responseList.get(1).getCryptoSymbol());
        assertEquals(3.0, responseList.get(1).getAmount());
        assertEquals("SELL", responseList.get(1).getBillType());
    }
}
