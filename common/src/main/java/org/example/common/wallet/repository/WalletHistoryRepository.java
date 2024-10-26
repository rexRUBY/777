package org.example.common.wallet.repository;

import org.example.common.wallet.entity.WalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletHistoryRepository extends JpaRepository<WalletHistory,Long> {
}
