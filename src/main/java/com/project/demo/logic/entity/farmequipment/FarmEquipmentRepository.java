package com.project.demo.logic.entity.farmequipment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmEquipmentRepository extends JpaRepository<FarmEquipment, FarmEquipmentId> {
    List<FarmEquipment> findById_FarmId(Long farmId);

    List<FarmEquipment> findById_EquipmentId(Long equipmentId);
}