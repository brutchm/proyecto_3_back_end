package com.project.demo.logic.entity.farmequipment;

import com.project.demo.logic.entity.equipment.EquipmentMachinery;
import com.project.demo.logic.entity.farm.Farm;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "farm_equipment")
public class FarmEquipment {

    @EmbeddedId
    private FarmEquipmentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("equipmentId")
    @JoinColumn(name = "equipment_id")
    private EquipmentMachinery equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("farmId")
    @JoinColumn(name = "farm_id")
    private Farm farm;

    @Column(name = "maintenance_type")
    private String maintenanceType;

    @Lob
    @Column(name = "maintenance_log", columnDefinition = "TEXT")
    private String maintenanceLog;

    @Column(name = "maintenance_cost")
    private Double maintenanceCost;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "isActive", columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isActive;

    // Getters and Setters
    public FarmEquipmentId getId() { return id; }
    public void setId(FarmEquipmentId id) { this.id = id; }
    public EquipmentMachinery getEquipment() { return equipment; }
    public void setEquipment(EquipmentMachinery equipment) { this.equipment = equipment; }
    public Farm getFarm() { return farm; }
    public void setFarm(Farm farm) { this.farm = farm; }
    public String getMaintenanceType() { return maintenanceType; }
    public void setMaintenanceType(String maintenanceType) { this.maintenanceType = maintenanceType; }
    public String getMaintenanceLog() { return maintenanceLog; }
    public void setMaintenanceLog(String maintenanceLog) { this.maintenanceLog = maintenanceLog; }
    public Double getMaintenanceCost() { return maintenanceCost; }
    public void setMaintenanceCost(Double maintenanceCost) { this.maintenanceCost = maintenanceCost; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}