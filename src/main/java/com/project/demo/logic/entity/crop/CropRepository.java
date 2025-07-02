package com.project.demo.logic.entity.crop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CropRepository extends JpaRepository<Crop, Integer> {
}