package com.project.demo.logic.entity.plot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FarmPlotRepository extends JpaRepository<FarmPlot, Long> {
    List<FarmPlot> findByFarmId(Long farmId);

    Optional<FarmPlot> findByPlotNameAndFarmId(String plotName, Long farmId);

    Optional<FarmPlot> findByIdAndFarmId(Long id, Long farmId);
}