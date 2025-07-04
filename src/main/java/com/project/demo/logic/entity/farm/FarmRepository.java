package com.project.demo.logic.entity.farm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {
    Optional<Farm> findByFarmName(String farmName);

    @Query("SELECT f FROM Farm f JOIN UserXFarm uxf ON f.id = uxf.farm.id WHERE uxf.user.id = :userId")
    List<Farm> findFarmsByUserId(@Param("userId") Long userId);
}