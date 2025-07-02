package com.project.demo.logic.entity.cropsmanagement;

import com.project.demo.logic.entity.crop.Crop;
import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.managementaction.ManagementAction;
import com.project.demo.logic.entity.managementaction.ManagementActionDetail;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crops_management")
public class CropsManagement {

    @EmbeddedId
    private CropsManagementId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cropId")
    @JoinColumn(name = "id_crop")
    private Crop crop;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("farmId")
    @JoinColumn(name = "id_farm")
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("actionId")
    @JoinColumn(name = "id_action")
    private ManagementAction managementAction;

    // Relación opcional a los detalles de la acción
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_details_id")
    private ManagementActionDetail actionDetails;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "isActive", columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isActive;

    // Getters and Setters
    public CropsManagementId getId() { return id; }
    public void setId(CropsManagementId id) { this.id = id; }
    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }
    public Farm getFarm() { return farm; }
    public void setFarm(Farm farm) { this.farm = farm; }
    public ManagementAction getManagementAction() { return managementAction; }
    public void setManagementAction(ManagementAction managementAction) { this.managementAction = managementAction; }
    public ManagementActionDetail getActionDetails() { return actionDetails; }
    public void setActionDetails(ManagementActionDetail actionDetails) { this.actionDetails = actionDetails; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}