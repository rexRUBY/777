package org.example.common.user.repository;

import org.example.common.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.tradeList  " + // User와 Trade 조인
            "LEFT JOIN  u.walletHistoryList ") // User와 WalletHistory 조인
    Page<User> findAllJoinTradeJoinWallet(Pageable pageable);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.subscriptionsIFollow  " + // User와 Trade 조인
            "LEFT JOIN u.walletList " +
            "LEFT JOIN u.subscriptionsBeingFollowed ") // User와 WalletHistory 조인
    Page<User> findAllJoinSubscriptsJoinWallet(Pageable pageable);
}
