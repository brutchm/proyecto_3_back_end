package com.project.demo.logic.entity.animal;

import com.project.demo.logic.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {
    List<Animal> findByFarmId(Long farmId);

    List<Animal> findByFarmIdAndAnimalGroupId(Long farmId, Long groupId);

    Page<Animal> findByFarmIdAndAnimalGroupId(Long farmId, Long groupId, Pageable pageable);

    Optional<Animal> findByIdAndFarmId(Long animalId, Long farmId);
}