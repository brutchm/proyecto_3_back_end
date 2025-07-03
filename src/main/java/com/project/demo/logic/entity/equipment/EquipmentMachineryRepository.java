package com.project.demo.logic.entity.equipment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentMachineryRepository extends JpaRepository<EquipmentMachinery, Long> {
}