package com.project.demo.logic.entity.suggestion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AiSuggestionRepository extends JpaRepository<AiSuggestion, Long> {
    Page<AiSuggestion> findByUserId(Long userId, Pageable pageable);
    Optional<AiSuggestion> findByIdAndUserId(Long id, Long userId);
}