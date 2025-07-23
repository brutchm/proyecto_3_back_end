package com.project.demo.logic.entity.farm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FarmsTechnicalInformationRepository extends JpaRepository<FarmsTechnicalInformation, Long> {
    Optional<FarmsTechnicalInformation> findByFarmId(Long farmId);
}
