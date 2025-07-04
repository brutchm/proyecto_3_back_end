package com.project.demo.logic.entity.marketprice;

import com.project.demo.logic.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorporationMarketPriceRepository extends JpaRepository<CorporationMarketPrice, Long> {
    List<CorporationMarketPrice> findByCorporationId(Long corporationId);
    List<CorporationMarketPrice> findByCropId(Long cropId);

    Optional<CorporationMarketPrice> findByCropIdAndCorporationId(Long id, Long userId);
}