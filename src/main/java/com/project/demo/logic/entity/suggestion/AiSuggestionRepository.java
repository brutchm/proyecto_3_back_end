package com.project.demo.logic.entity.suggestion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AiSuggestionRepository extends JpaRepository<AiSuggestion, Integer> {
    List<AiSuggestion> findByUserId(Integer userId);
    List<AiSuggestion> findByRelatedFarmId(Integer farmId);
}