package com.project.demo.logic.entity.cropsmanagement;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class CropsManagementId implements Serializable {

    @Column(name = "id_crop")
    private Long cropId;

    @Column(name = "id_farm")
    private Long farmId;

    @Column(name = "id_action")
    private Long actionId;

    @Column(name = "action_date")
    private LocalDateTime actionDate;

    // Constructores, Getters, Setters, hashCode y equals
    public CropsManagementId() {}

    public CropsManagementId(Long cropId, Long farmId, Long actionId, LocalDateTime actionDate) {
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

    public Long getCropId() {
        return cropId;
    }

    public void setCropId(Long cropId) {
        this.cropId = cropId;
    }

    public Long getFarmId() {
        return farmId;
    }

    public void setFarmId(Long farmId) {
        this.farmId = farmId;
    }

    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    public LocalDateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDateTime actionDate) {
        this.actionDate = actionDate;
    }
}