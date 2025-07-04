package com.project.demo.logic.entity.farmequipment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class FarmEquipmentId implements Serializable {

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "farm_id")
    private Long farmId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructores, Getters, Setters, hashCode y equals
    public FarmEquipmentId() {}

    public FarmEquipmentId(Long equipmentId, Long farmId, LocalDateTime createdAt) {
        this.equipmentId = equipmentId;
        this.farmId = farmId;
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FarmEquipmentId that = (FarmEquipmentId) o;
        return Objects.equals(equipmentId, that.equipmentId) && Objects.equals(farmId, that.farmId) && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipmentId, farmId, createdAt);
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Long getFarmId() {
        return farmId;
    }

    public void setFarmId(Long farmId) {
        this.farmId = farmId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}