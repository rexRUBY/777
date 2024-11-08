package org.example.common.subscriptions.repository;

import org.example.common.subscriptions.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRepository extends JpaRepository<Billing,Long> {
}
