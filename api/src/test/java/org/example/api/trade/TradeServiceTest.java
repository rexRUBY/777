package org.example.api.trade;

import org.example.api.trade.service.TradeService;
import org.example.common.common.dto.AuthUser;
import org.example.common.crypto.entity.Crypto;
import org.example.common.crypto.repository.CryptoRepository;
import org.example.common.subscriptions.repository.BillingRepository;
import org.example.common.trade.dto.request.TradeRequestDto;
import org.example.common.trade.dto.response.TradeResponseDto;
import org.example.common.trade.repository.TradeRepository;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.common.wallet.entity.Wallet;
import org.example.common.wallet.repository.WalletHistoryRepository;
import org.example.common.wallet.repository.WalletRepository;
import org.example.common.webclient.service.CryptoWebService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
