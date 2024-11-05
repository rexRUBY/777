package org.example.common.subscriptions.repository;

import org.example.common.subscriptions.entity.Subscriptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
            "WHERE (s.crypto.id = 1 AND s.nowPrice * 1.05 <= :btcPrice)" +
            " OR (s.crypto.id = 2 AND s.nowPrice * 1.05 <= :ethPrice)" +
            "AND s.subscribe = 'ON' ")
    Page<Subscriptions> findAllPrice(@Param("btcPrice") Long btcPrice, @Param("ethPrice") Long ethPrice,Pageable pageable);

    @Query("SELECT s FROM Subscriptions s " +
            "WHERE s.createdAt < :time " +
            "AND s.subscribe = 'ON'")
    Page<Subscriptions> findAllPrice(@Param("time")LocalDateTime time,Pageable pageable);
}
