package com.project.demo.logic.entity.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorporationUserSubscriptionRepository extends JpaRepository<CorporationUserSubscription, CorporationUserSubscriptionId> {
}