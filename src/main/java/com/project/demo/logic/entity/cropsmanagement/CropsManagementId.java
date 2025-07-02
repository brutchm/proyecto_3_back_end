package com.project.demo.logic.entity.cropsmanagement;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class CropsManagementId implements Serializable {

    @Column(name = "id_crop")
    private Integer cropId;

    @Column(name = "id_farm")
    private Integer farmId;

    @Column(name = "id_action")
    private Integer actionId;

    @Column(name = "action_date")
    private LocalDateTime actionDate;

    // Constructores, Getters, Setters, hashCode y equals
    public CropsManagementId() {}

    public CropsManagementId(Integer cropId, Integer farmId, Integer actionId, LocalDateTime actionDate) {
        this.cropId = cropId;
        this.farmId = farmId;
        this.actionId = actionId;
        this.actionDate = actionDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CropsManagementId that = (CropsManagementId) o;
        return Objects.equals(cropId, that.cropId) && Objects.equals(farmId, that.farmId) && Objects.equals(actionId, that.actionId) && Objects.equals(actionDate, that.actionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cropId, farmId, actionId, actionDate);
    }
}