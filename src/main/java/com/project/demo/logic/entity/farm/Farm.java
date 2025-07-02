package com.project.demo.logic.entity.farm;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "farms")
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "farm_name", nullable = false)
    private String farmName;

    @Column(name = "farm_country", length = 100)
    private String farmCountry;

    @Column(name = "farm_state_province", length = 100)
    private String farmStateProvince;

    @Lob
    @Column(name = "farm_other_directions", columnDefinition = "TEXT")
    private String farmOtherDirections;

    @Column(name = "farm_location")
    private String farmLocation;

    @Column(name = "farm_size")
    private Double farmSize;

    @Column(name = "farm_measure_unit", length = 50)
    private String farmMeasureUnit;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "isActive", columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isActive;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public String getFarmCountry() { return farmCountry; }
    public void setFarmCountry(String farmCountry) { this.farmCountry = farmCountry; }
    public String getFarmStateProvince() { return farmStateProvince; }
    public void setFarmStateProvince(String farmStateProvince) { this.farmStateProvince = farmStateProvince; }
    public String getFarmOtherDirections() { return farmOtherDirections; }
    public void setFarmOtherDirections(String farmOtherDirections) { this.farmOtherDirections = farmOtherDirections; }
    public String getFarmLocation() { return farmLocation; }
    public void setFarmLocation(String farmLocation) { this.farmLocation = farmLocation; }
    public Double getFarmSize() { return farmSize; }
    public void setFarmSize(Double farmSize) { this.farmSize = farmSize; }
    public String getFarmMeasureUnit() { return farmMeasureUnit; }
    public void setFarmMeasureUnit(String farmMeasureUnit) { this.farmMeasureUnit = farmMeasureUnit; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}