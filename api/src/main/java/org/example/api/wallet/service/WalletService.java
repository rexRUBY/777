package org.example.api.wallet.service;

import lombok.RequiredArgsConstructor;
import org.example.common.common.dto.AuthUser;
import org.example.common.crypto.entity.Crypto;
import org.example.common.crypto.repository.CryptoRepository;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.common.wallet.dto.request.ChargeRequest;
import org.example.common.wallet.dto.response.WalletHistoryListResponseDto;
import org.example.common.wallet.dto.response.WalletHistoryPageResponseDto;
import org.example.common.wallet.dto.response.WalletResponse;
import org.example.common.wallet.entity.Wallet;
import org.example.common.wallet.entity.WalletHistory;
import org.example.common.wallet.enums.ChargeStatus;
import org.example.common.wallet.repository.WalletHistoryRepository;
import org.example.common.wallet.repository.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final CryptoRepository cryptoRepository;
    private final UserRepository userRepository;
    private final WalletHistoryRepository walletHistoryRepository;

    public List<WalletResponse> getWallets(AuthUser authUser) {
        List<Wallet> wallets = walletRepository.findAllByUserId(authUser.getId());

        if (wallets.isEmpty()) {
            throw new IllegalArgumentException("해당 유저의 지갑을 찾을 수 없습니다.");
        }

        return wallets.stream()
                .map(WalletResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createWallet(User user) {
        List<Crypto> cryptos = cryptoRepository.findAll();  // 데이터베이스에서 모든 코인을 조회

        // 각 코인에 대한 지갑 생성
        for (Crypto crypto : cryptos) {
            Wallet wallet = new Wallet(user, 0.0, crypto.getSymbol(), 0L, 1000000000L);
            walletRepository.save(wallet);
        }
    }

    @Transactional
    public String chargeCash(AuthUser authUser, ChargeRequest request) {
        User user = userRepository.findById(authUser.getId()).orElseThrow();

        List<Wallet> wallets = walletRepository.findAllByUser(user);

        for(Wallet wallet : wallets) {
            wallet.chargeCash(request.getChargeAmount());
        }

        WalletHistory walletHistory = new WalletHistory(user, request.getChargeAmount(), ChargeStatus.CHARGE);

        walletHistoryRepository.save(walletHistory);

        return "successfully charged!";
    }

    public WalletHistoryListResponseDto getWalletHistoryPage(
            AuthUser authUser,
            int page,
            int size,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<WalletHistory> walletHistoryPage = walletHistoryRepository.findByUserIdWithDate(
                authUser.getId(),
                startDate,
                endDate,
                pageable
        );

        List<WalletHistoryPageResponseDto> walletHistoryRes = walletHistoryPage.stream()
                .map(WalletHistoryPageResponseDto::new).toList();

        return new WalletHistoryListResponseDto(
                walletHistoryRes,
                walletHistoryPage.getTotalPages(),
                walletHistoryPage.getTotalElements());
    }
}
