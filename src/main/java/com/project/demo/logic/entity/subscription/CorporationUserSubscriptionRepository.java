package com.project.demo.logic.entity.subscription;

import com.project.demo.logic.entity.userfarm.UserXFarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorporationUserSubscriptionRepository extends JpaRepository<CorporationUserSubscription, CorporationUserSubscriptionId> {
    List<UserXFarm> findByCorporationId(Long corporationId); // Asumiendo que el UserXFarm se reutiliza o se crea un DTO. Para este caso, usaremos el UserXFarm.
    List<UserXFarm> findByIndividualUserId(Long individualUserId);
    Optional<UserXFarm> findByCorporationIdAndIndividualUserId(Long corporationId, Long individualUserId);
}