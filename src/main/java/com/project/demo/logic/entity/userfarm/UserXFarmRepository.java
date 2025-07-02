package com.project.demo.logic.entity.userfarm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserXFarmRepository extends JpaRepository<UserXFarm, UserFarmId> {
    List<UserXFarm> findByFarmId(Integer farmId);
    List<UserXFarm> findByUserId(Integer userId);
}