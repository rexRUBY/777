package org.example.common.trade.repository;

import org.example.common.crypto.entity.Crypto;
import org.example.common.trade.entity.Trade;
import org.example.common.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade,Long> {
    List<Trade> findAllByCryptoAndUser(Crypto crypto, User user);

    List<Trade> findAllByUser(User user);
}
