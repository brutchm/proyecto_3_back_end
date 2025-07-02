package com.project.demo.logic.entity.marketprice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorporationMarketPriceRepository extends JpaRepository<CorporationMarketPrice, Integer> {
    List<CorporationMarketPrice> findByCorporationId(Integer corporationId);
    List<CorporationMarketPrice> findByCropId(Integer cropId);
}