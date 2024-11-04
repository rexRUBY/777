package org.example.common.subscriptions.repository;

import org.example.common.subscriptions.entity.Subscriptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscriptionsRepository extends JpaRepository<Subscriptions, Long> {
    Page<Subscriptions> findAllByFollowerUserId(Long FollowerUserId, Pageable pageable);

    Page<Subscriptions> findAllByFollowingUserId(Long FollowingUserId, Pageable pageable);

    @Query("SELECT DISTINCT s FROM Subscriptions s WHERE s.subscribe = 'OFF'")
    Page<Subscriptions> findAllBySubscribe(Pageable pageable);
}
