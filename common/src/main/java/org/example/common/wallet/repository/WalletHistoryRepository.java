package org.example.common.wallet.repository;

import org.example.common.wallet.entity.WalletHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface WalletHistoryRepository extends JpaRepository<WalletHistory,Long> {
    @Query("SELECT w FROM WalletHistory w WHERE w.user.id = :userId " +
            "AND (:startDate IS NULL OR w.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR w.createdAt <= :endDate)")
    Page<WalletHistory> findByUserIdWithDate(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
}
