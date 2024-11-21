package org.example.common.user.repository;

import org.example.common.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCommandRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.tradeList " +
            "LEFT JOIN u.walletHistoryList " +
            "WHERE u.processed = false")
    Page<User> findAllByProcessedFalseJoinTradeJoinWallet(Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.subscriptionsIFollow " +
            "LEFT JOIN u.walletList " +
            "LEFT JOIN u.subscriptionsBeingFollowed")
    Page<User> findAllJoinSubscriptsJoinWallet(Pageable pageable);

    @Query("SELECT MIN(u.id) FROM User u")
    Long findMinId();

    @Query("SELECT MAX(u.id) FROM User u")
    Long findMaxId();

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.tradeList " +
            "LEFT JOIN u.walletHistoryList " +
            "WHERE u.id BETWEEN :start AND :end")
    Page<User> findAllByIdBetween(@Param("start") Long start, @Param("end") Long end, Pageable pageable);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.walletList WHERE u.id = :id")
    Optional<User> findUserWithWalletList(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.subscriptionsBeingFollowed WHERE u.id = :id")
    Optional<User> findUserWithSubscriptionsBeingFollowed(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.subscriptionsIFollow WHERE u.id = :id")
    Optional<User> findUserWithSubscriptionsIFollow(@Param("id") Long id);
}