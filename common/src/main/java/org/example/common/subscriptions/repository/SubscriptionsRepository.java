package org.example.common.subscriptions.repository;

import org.example.common.subscriptions.entity.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionsRepository extends JpaRepository<Subscriptions, Long> {
    List<Subscriptions> findAllByFollowerUserId(Long FollowerUserId);
    List<Subscriptions> findAllByFollowingUserId(Long FollowingUserId);

}
