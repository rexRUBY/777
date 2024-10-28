package org.example.common.subscriptions.repository;

import org.example.common.subscriptions.entity.Subscriptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscriptionsRepository extends JpaRepository<Subscriptions, Long> {
    List<Subscriptions> findAllByFollowerUserId(Long FollowerUserId);
    List<Subscriptions> findAllByFollowingUserId(Long FollowingUserId);

    @Query("SELECT DISTINCT s FROM Subscriptions s WHERE s.subscribe = 'OFF'")
    Page<Subscriptions> findAllBySubscribe(Pageable pageable);
}
