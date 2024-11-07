package org.example.common.subscriptions.repository;

import org.example.common.subscriptions.entity.Subscriptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SubscriptionsRepository extends JpaRepository<Subscriptions, Long> {
    Page<Subscriptions> findAllByFollowerUserId(Long FollowerUserId, Pageable pageable);

    Page<Subscriptions> findAllByFollowingUserId(Long FollowingUserId, Pageable pageable);

    @Query("SELECT DISTINCT s FROM Subscriptions s WHERE s.subscribe = 'OFF'")
    Page<Subscriptions> findAllBySubscribe(Pageable pageable);

    @Query("SELECT MIN(s.id) FROM Subscriptions s")
    Long findMinId();

    @Query("SELECT MAX(s.id) FROM Subscriptions s")
    Long findMaxId();

    @Query("SELECT s FROM Subscriptions s " +
            "WHERE (s.crypto.symbol = :cryptoSymbol AND s.nowPrice * 1.05 <= :price)" +
            "AND s.subscribe = 'ON' ")
    Page<Subscriptions> findPriceByCrypto(@Param("price") Long price, @Param("cryptoSymbol") String cryptoSymbol,Pageable pageable);

    @Query("SELECT s FROM Subscriptions s " +
            "WHERE (s.crypto.symbol = :cryptoSymbol AND s.createdAt < :time) " +
            "AND s.subscribe = 'ON'")
    Page<Subscriptions> findTime(@Param("time")LocalDateTime time,@Param("cryptoSymbol") String cryptoSymbol,Pageable pageable);
}