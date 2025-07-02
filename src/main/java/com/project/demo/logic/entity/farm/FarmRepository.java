package com.project.demo.logic.entity.farm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Integer> {
    Optional<Farm> findByFarmName(String farmName);
}