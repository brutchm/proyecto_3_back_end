package com.project.demo.logic.entity.scenario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimulatorScenarioRepository extends JpaRepository<SimulatorScenario, Long> {
    Page<SimulatorScenario> findByUserId(Long userId, Pageable pageable);
    Optional<SimulatorScenario> findByIdAndUserId(Long id, Long userId);
}