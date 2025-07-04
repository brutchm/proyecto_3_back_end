package com.project.demo.logic.entity.animal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnimalGroupRepository extends JpaRepository<AnimalGroup, Long> {
    List<AnimalGroup> findByFarmId(Long farmId);

    Optional<AnimalGroup> findByIdAndFarmId(Long groupId, Long farmId);
}