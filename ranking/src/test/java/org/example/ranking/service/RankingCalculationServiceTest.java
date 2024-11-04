package org.example.ranking.service;

// ... (other imports)
import org.example.common.crypto.entity.Crypto;
import org.example.common.trade.entity.Trade;
import org.example.common.trade.enums.TradeFor;
import org.example.common.user.entity.User;
import org.example.common.wallet.entity.Wallet;
import org.example.common.wallet.entity.WalletHistory;
import org.example.ranking.config.CountConfig;
import org.example.ranking.entity.Ranked;
import org.example.ranking.entity.Ranking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RankingCalculationServiceTest {

    @InjectMocks
    private RankingCalculationService rankingCalculationService;

    @Mock
    private User user;

    @Mock
    private Wallet wallet;

    private List<WalletHistory> walletHistoryList;
    private List<Trade> tradeList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        walletHistoryList = new ArrayList<>();
        tradeList = new ArrayList<>();
    }

    @Test
    void testCalculateYield() {
        // Given
        String cryptoSymbol = "BTC";
        Crypto crypto = new Crypto();
        ReflectionTestUtils.setField(crypto, "symbol", "BTC");

        // Mock WalletHistory for one month ago
        WalletHistory lastMonthWallet = new WalletHistory(wallet);
        ReflectionTestUtils.setField(lastMonthWallet, "cash", 1000L);
        ReflectionTestUtils.setField(lastMonthWallet, "amount", 2.0);
        ReflectionTestUtils.setField(lastMonthWallet, "cryptoPrice", 40000L);
        ReflectionTestUtils.setField(lastMonthWallet, "modifiedAt", LocalDate.now().minusMonths(1).atStartOfDay());
        ReflectionTestUtils.setField(lastMonthWallet, "cryptoSymbol", "BTC");

        // Mock WalletHistory for now
        WalletHistory nowWallet = new WalletHistory(wallet);
        ReflectionTestUtils.setField(nowWallet, "cash", 1500L);
        ReflectionTestUtils.setField(nowWallet, "amount", 3.0);
        ReflectionTestUtils.setField(nowWallet, "cryptoPrice", 45000L);
        ReflectionTestUtils.setField(nowWallet, "modifiedAt", LocalDate.now().atStartOfDay());
        ReflectionTestUtils.setField(nowWallet, "cryptoSymbol", "BTC");

        walletHistoryList.add(lastMonthWallet);
        walletHistoryList.add(nowWallet);

        // Mock User
        when(user.getWalletHistoryList()).thenReturn(walletHistoryList);

        // Mock Trade
        Trade trade = new Trade();
        ReflectionTestUtils.setField(trade, "totalPrice", 5000L);
        ReflectionTestUtils.setField(trade, "tradeFor", TradeFor.OTHER);
        ReflectionTestUtils.setField(trade, "crypto", crypto);
        ReflectionTestUtils.setField(trade, "modifiedAt", LocalDate.now().minusDays(10).atStartOfDay());
        tradeList.add(trade);

        // Mock Trade List
        when(user.getTradeList()).thenReturn(tradeList);

        // When
        double yield = rankingCalculationService.calculateYield(user, cryptoSymbol);

        // Then
        assertEquals(0.0, yield); // Expected yield calculation based on provided data
    }

//    @Test
//    void testSetRank() {
//        // Given
//        Ranking ranking = new Ranking();
//        ReflectionTestUtils.setField(ranking, "cryptoSymbol", "BTC");
//        ReflectionTestUtils.setField(ranking, "ranked", Ranked.ON);
//
//        // When
//        rankingCalculationService.setRank(ranking, "BTC");
//
//        // Then
//        assertEquals(1, CountConfig.count); // Assuming CountConfig.count starts from 0
//    }

//    @Test
//    void testCalculateOtherPrice() {
//        // Given
//        String cryptoSymbol = "BTC";
//        Trade trade = new Trade();
//        ReflectionTestUtils.setField(trade, "totalPrice", 1000.0);
//        ReflectionTestUtils.setField(trade, "tradeFor", TradeFor.OTHER);
//        ReflectionTestUtils.setField(trade, "modifiedAt", LocalDate.now().atStartOfDay()); // Today's date
//
//        tradeList.add(trade);
//        when(user.getTradeList()).thenReturn(tradeList);
//
//        // When
//        // calculateYield 메서드를 호출하여 calculateOtherPrice를 간접적으로 테스트
//        rankingCalculationService.calculateYield(user, cryptoSymbol);
//
//        // Then
//        // verify the calculation indirectly (you can also check other results if necessary)
//        // 여기서 실제 기대 결과를 검증할 수 있습니다.
//        // 예를 들어, 다른 메서드 호출의 결과 등을 확인하세요.
//    }
}
