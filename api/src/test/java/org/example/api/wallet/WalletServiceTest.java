package org.example.api.wallet;

import org.example.api.wallet.service.WalletService;
import org.example.common.common.dto.AuthUser;
import org.example.common.crypto.entity.Crypto;
import org.example.common.crypto.repository.CryptoRepository;
import org.example.common.user.entity.User;
import org.example.common.wallet.dto.response.WalletResponse;
import org.example.common.wallet.entity.Wallet;
import org.example.common.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @InjectMocks
    private WalletService walletService;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private CryptoRepository cryptoRepository;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.of("test@example.com", "encodedPassword", "testUser");
    }

    @Test
    public void 유저_지갑_조회_성공() {
        // given
        List<Wallet> wallets = new ArrayList<>();
        wallets.add(new Wallet(mockUser, 10.0, "BTC", 50000L, 1000000L));
        wallets.add(new Wallet(mockUser, 5.0, "ETH", 25000L, 500000L));

        when(walletRepository.findAllByUserId(mockUser.getId())).thenReturn(wallets);

        // when
        List<WalletResponse> walletResponses = walletService.getWallets(new AuthUser(mockUser.getId(), mockUser.getEmail()));

        // then
        assertNotNull(walletResponses);
        assertEquals(2, walletResponses.size());
        assertEquals("BTC", walletResponses.get(0).getCryptoSymbol());
        assertEquals("ETH", walletResponses.get(1).getCryptoSymbol());

        verify(walletRepository, times(1)).findAllByUserId(mockUser.getId());
    }

    @Test
    public void 지갑_생성_성공() {
        // given
        List<Crypto> cryptos = Arrays.asList(
                mock(Crypto.class), // Mock 객체로 리스트 생성
                mock(Crypto.class)
        );

        when(cryptoRepository.findAll()).thenReturn(cryptos);

        // when
        walletService.createWallet(mockUser);

        // then
        verify(walletRepository, times(cryptos.size())).save(any(Wallet.class));
    }

    @Test
    public void 지갑_조회_실패_유저_지갑_없음() {
        // given
        when(walletRepository.findAllByUserId(mockUser.getId())).thenReturn(new ArrayList<>());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            walletService.getWallets(new AuthUser(mockUser.getId(), mockUser.getEmail()));
        });

        assertEquals("해당 유저의 지갑을 찾을 수 없습니다.", exception.getMessage());
        verify(walletRepository, times(1)).findAllByUserId(mockUser.getId());
    }
}


