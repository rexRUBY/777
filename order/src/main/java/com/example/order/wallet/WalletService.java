package com.example.order.wallet;

import org.example.common.wallet.entity.Wallet;
import org.example.common.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    private final String BUY_ORDER_KEY = "BUY_ORDER";
    private final String SELL_ORDER_KEY = "SELL_ORDER";

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public void updateWallet(Long userId, Long oppositeUserId, double price, double amount, String orderKey, String symbol) {
        Wallet wallet = walletRepository.findByUserIdAndCryptoSymbol(userId, symbol);
        Wallet oppositeWallet = walletRepository.findByUserIdAndCryptoSymbol(oppositeUserId, symbol);

        switch (orderKey) {
            case BUY_ORDER_KEY -> {
                wallet.buyUpdate(price, amount);
                oppositeWallet.sellUpdate(price, amount);
            }
            case SELL_ORDER_KEY -> {
                wallet.sellUpdate(price, amount);
                oppositeWallet.buyUpdate(price, amount);
            }
        }
        System.out.println("지갑 업데이트 완료");
    }
}