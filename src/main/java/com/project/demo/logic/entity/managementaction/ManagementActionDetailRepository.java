package com.project.demo.logic.entity.managementaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManagementActionDetailRepository extends JpaRepository<ManagementActionDetail, Long> {
    List<ManagementActionDetail> findByActionId(Long actionId);
}