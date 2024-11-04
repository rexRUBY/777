package org.example.common.trade.repository;

import org.example.common.crypto.entity.Crypto;
import org.example.common.trade.entity.Trade;
import org.example.common.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade,Long> {
    List<Trade> findAllByCryptoAndUser(Crypto crypto, User user);

    List<Trade> findAllByUser(User user);

    @Query("SELECT t FROM Trade t WHERE t.crypto.id = :cryptoId AND t.user.id = :userId " +
            "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR t.createdAt <= :endDate)")
    Page<Trade> findByCryptoIdAndUserIdWithDate(
            @Param("cryptoId") Long cryptoId,
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

}
