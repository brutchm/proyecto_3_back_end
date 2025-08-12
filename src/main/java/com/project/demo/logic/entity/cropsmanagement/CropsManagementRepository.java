package com.project.demo.logic.entity.cropsmanagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropsManagementRepository extends JpaRepository<CropsManagement, Long> {
    List<CropsManagement> findByCrop_Id(Long cropId);
    List<CropsManagement> findByFarmPlot_Id(Long farmPlotId);
}