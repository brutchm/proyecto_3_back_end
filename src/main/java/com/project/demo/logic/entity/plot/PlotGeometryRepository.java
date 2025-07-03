package com.project.demo.logic.entity.plot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlotGeometryRepository extends JpaRepository<PlotGeometry, Long> {
    Optional<PlotGeometry> findByFarmPlot_Id(Long plotId);
}