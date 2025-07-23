package com.project.demo.logic.entity.farm;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "farms_technical_information")
public class FarmsTechnicalInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    @JsonBackReference("farm-technicalInfo")
    private Farm farm;

    @Column(name = "soil_ph", length = 50)
    private String soilPh;

    @Column(name = "soil_nutrients", length = 255)
    private String soilNutrients;

    @Column(name = "irrigation_system")
    private Boolean irrigationSystem;

    @Column(name = "irrigation_system_type", length = 100)
    private String irrigationSystemType;

    @Column(name = "water_available")
    private Boolean waterAvailable;

    @Column(name = "water_usage_type", length = 100)
    private String waterUsageType;

    @Column(name = "fertilizer_pesticide_use")
    private Boolean fertilizerPesticideUse;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "isActive")
    private Boolean isActive;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Farm getFarm() { return farm; }
    public void setFarm(Farm farm) { this.farm = farm; }
    public String getSoilPh() { return soilPh; }
    public void setSoilPh(String soilPh) { this.soilPh = soilPh; }
    public String getSoilNutrients() { return soilNutrients; }
    public void setSoilNutrients(String soilNutrients) { this.soilNutrients = soilNutrients; }
    public Boolean getIrrigationSystem() { return irrigationSystem; }
    public void setIrrigationSystem(Boolean irrigationSystem) { this.irrigationSystem = irrigationSystem; }
    public String getIrrigationSystemType() { return irrigationSystemType; }
    public void setIrrigationSystemType(String irrigationSystemType) { this.irrigationSystemType = irrigationSystemType; }
    public Boolean getWaterAvailable() { return waterAvailable; }
    public void setWaterAvailable(Boolean waterAvailable) { this.waterAvailable = waterAvailable; }
    public String getWaterUsageType() { return waterUsageType; }
    public void setWaterUsageType(String waterUsageType) { this.waterUsageType = waterUsageType; }
    public Boolean getFertilizerPesticideUse() { return fertilizerPesticideUse; }
    public void setFertilizerPesticideUse(Boolean fertilizerPesticideUse) { this.fertilizerPesticideUse = fertilizerPesticideUse; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
