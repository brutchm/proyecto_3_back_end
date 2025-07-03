package com.project.demo.logic.entity.animal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnimalGroupHistoryRepository extends JpaRepository<AnimalGroupHistory, Long> {
    List<AnimalGroupHistory> findByAnimalGroupId(Long animalGroupId);
}