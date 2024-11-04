package org.example.common.wallet.repository;


import org.example.common.user.entity.User;
import org.example.common.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findAllByUserId(Long userId);

    Wallet findByUserIdAndCryptoSymbol(Long id, String symbol);

    List<Wallet> findAllByUser(User user);
}
