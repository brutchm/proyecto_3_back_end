package com.project.demo.logic.entity.animal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {
    List<Animal> findByFarmId(Long farmId);

    Optional<Animal> findByIdAndFarmId(Long animalId, Long farmId);
}