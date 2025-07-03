package com.project.demo.logic.entity.userfarm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserXFarmRepository extends JpaRepository<UserXFarm, UserFarmId> {
    List<UserXFarm> findByFarmId(Long farmId);
    List<UserXFarm> findByUserId(Long userId);

    Optional<UserXFarm> findByFarmIdAndUserId(Long farmId, Long userId);
}