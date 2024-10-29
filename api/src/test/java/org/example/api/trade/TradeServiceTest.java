package org.example.api.trade;

import org.example.api.trade.service.TradeService;
import org.example.common.common.dto.AuthUser;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.crypto.entity.Crypto;
import org.example.common.crypto.repository.CryptoRepository;
import org.example.common.subscriptions.repository.BillingRepository;
import org.example.common.trade.dto.request.TradeRequestDto;
import org.example.common.trade.dto.response.TradeResponseDto;
import org.example.common.trade.enums.TradeFor;
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

    @Test
    public void 성공적으로_코인_구매() {
        // given
        AuthUser authUser = AuthUser.from(1L, "test@example.com");
        long cryptoId = 1L;

        TradeRequestDto tradeRequestDto = new TradeRequestDto();
        ReflectionTestUtils.setField(tradeRequestDto, "amount", 10.0);
        ReflectionTestUtils.setField(tradeRequestDto, "tradeType", "BUY");
        ReflectionTestUtils.setField(tradeRequestDto, "tradeFor", "SELF");

        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", 1L);
        ReflectionTestUtils.setField(mockUser, "name", "testUser");

        Wallet wallet = new Wallet();
        ReflectionTestUtils.setField(wallet, "user", mockUser);
        ReflectionTestUtils.setField(wallet, "cryptoSymbol", "BTC");
        ReflectionTestUtils.setField(wallet, "cash", 100000L);
        ReflectionTestUtils.setField(wallet, "amount", 50.0); // 보유 코인 수량 설정

        Crypto mockCrypto = new Crypto();
        ReflectionTestUtils.setField(mockCrypto, "id", cryptoId);
        ReflectionTestUtils.setField(mockCrypto, "symbol", "BTC");

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(mockUser));
        when(cryptoRepository.findById(cryptoId)).thenReturn(Optional.of(mockCrypto));
        when(walletRepository.findByUserIdAndCryptoSymbol(mockUser.getId(), "BTC")).thenReturn(wallet);

        // when
        TradeResponseDto response = tradeService.postTrade(authUser, cryptoId, tradeRequestDto);

        // then
        assertNotNull(response);
        assertEquals("BTC", response.getCryptoSymbol());
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
        ReflectionTestUtils.setField(tradeRequestDto, "tradeType", "SELL");
        ReflectionTestUtils.setField(tradeRequestDto, "tradeFor", "SELF");

        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        Wallet wallet = new Wallet();
        ReflectionTestUtils.setField(wallet, "user", mockUser);
        ReflectionTestUtils.setField(wallet, "cryptoSymbol", "BTC");
        ReflectionTestUtils.setField(wallet, "cash", 100000L); // cash 값 설정
        ReflectionTestUtils.setField(wallet, "amount", 50.0); // 보유 코인 수량 설정

        Crypto mockCrypto = new Crypto();
        ReflectionTestUtils.setField(mockCrypto, "id", cryptoId);
        ReflectionTestUtils.setField(mockCrypto, "symbol", "BTC");

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(mockUser));
        when(cryptoRepository.findById(cryptoId)).thenReturn(Optional.of(mockCrypto));
        when(walletRepository.findByUserIdAndCryptoSymbol(mockUser.getId(), "BTC")).thenReturn(wallet);

        // when
        TradeResponseDto response = tradeService.postTrade(authUser, cryptoId, tradeRequestDto);

        // then
        assertNotNull(response);
        assertEquals("BTC", response.getCryptoSymbol());
        assertEquals("SELL", response.getBillType());

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
}
