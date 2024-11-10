package com.example.order.wallet;

import org.example.common.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public void updateWallet(Long userId, Long oppositeUserId, double price, double requestAmount, String orderKey) {

    }
}
