package com.project.demo.logic.entity.managementaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagementActionRepository extends JpaRepository<ManagementAction, Long> {
}