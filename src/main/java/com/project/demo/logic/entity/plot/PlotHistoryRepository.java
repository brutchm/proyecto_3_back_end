package com.project.demo.logic.entity.plot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlotHistoryRepository extends JpaRepository<PlotHistory, Integer> {
    List<PlotHistory> findByFarmPlotId(Integer farmPlotId);
}