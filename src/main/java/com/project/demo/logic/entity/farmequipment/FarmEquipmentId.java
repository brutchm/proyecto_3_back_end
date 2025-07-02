package com.project.demo.logic.entity.farmequipment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class FarmEquipmentId implements Serializable {

    @Column(name = "equipment_id")
    private Integer equipmentId;

    @Column(name = "farm_id")
    private Integer farmId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructores, Getters, Setters, hashCode y equals
    public FarmEquipmentId() {}

    public FarmEquipmentId(Integer equipmentId, Integer farmId, LocalDateTime createdAt) {
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
}