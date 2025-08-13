package com.project.demo.logic.entity.marketprice;

import com.project.demo.logic.entity.crop.Crop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorporationMarketPriceRepository extends JpaRepository<CorporationMarketPrice, Long> {
    List<CorporationMarketPrice> findByCorporationId(Long corporationId);
//Ordenamiento descendente
    @Query("select cp  from CorporationMarketPrice cp where cp.corporation.id=:userId order by cp.updatedAt desc")
    Page<CorporationMarketPrice> findByCorporationId(@Param("userId")Long corporationId, Pageable pageable);

    List<CorporationMarketPrice> findByCropId(Long cropId);

    Optional<CorporationMarketPrice> findByCropIdAndCorporationId(Long id, Long userId);
}