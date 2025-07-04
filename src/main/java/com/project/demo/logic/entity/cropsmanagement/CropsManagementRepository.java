package com.project.demo.logic.entity.cropsmanagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropsManagementRepository extends JpaRepository<CropsManagement, CropsManagementId> {
    List<CropsManagement> findById_FarmId(Long farmId);
    List<CropsManagement> findById_CropId(Long cropId);
}