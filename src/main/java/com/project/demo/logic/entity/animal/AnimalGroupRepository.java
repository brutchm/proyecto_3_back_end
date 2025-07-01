package com.project.demo.logic.entity.animal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnimalGroupRepository extends JpaRepository<AnimalGroup, Integer> {
    List<AnimalGroup> findByFarmId(Integer farmId);
}