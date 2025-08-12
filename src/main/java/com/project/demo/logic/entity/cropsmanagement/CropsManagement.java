package com.project.demo.logic.entity.cropsmanagement;

import com.project.demo.logic.entity.crop.Crop;
import com.project.demo.logic.entity.plot.FarmPlot;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;

import java.time.LocalDateTime;

@Entity
@Table(name = "crops_management")
public class CropsManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_crop")
    private Crop crop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_farm_plot", foreignKey = @ForeignKey(name = "fk_farm_plot"))
    @OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JsonIgnore
    private FarmPlot farmPlot;

    @Column(name = "id_farm", nullable = false)
    private Long farmId;

    @Column(name = "action_name", nullable = false)
    private String actionName;

    @Column(name = "action_picture_url")
    private String actionPictureUrl;

    @Column(name = "measure_unit", length = 50)
    private String measureUnit;

    @Column(name = "measure_value")
    private Double measureValue;

    @Column(name = "value_spent")
    private Double valueSpent;

    @Column(name = "action_date", nullable = false)
    private LocalDateTime actionDate;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "isActive", columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isActive;

    // Getters and Setters
    public Long getCropId() {
    return crop != null ? crop.getId() : null;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }
    public FarmPlot getFarmPlot() { return farmPlot; }
    public void setFarmPlot(FarmPlot farmPlot) { this.farmPlot = farmPlot; }
    public Long getFarmId() { return farmId; }
    public void setFarmId(Long farmId) { this.farmId = farmId; }
    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }
    public String getActionPictureUrl() { return actionPictureUrl; }
    public void setActionPictureUrl(String actionPictureUrl) { this.actionPictureUrl = actionPictureUrl; }
    public String getMeasureUnit() { return measureUnit; }
    public void setMeasureUnit(String measureUnit) { this.measureUnit = measureUnit; }
    public Double getMeasureValue() { return measureValue; }
    public void setMeasureValue(Double measureValue) { this.measureValue = measureValue; }
    public Double getValueSpent() { return valueSpent; }
    public void setValueSpent(Double valueSpent) { this.valueSpent = valueSpent; }
    public LocalDateTime getActionDate() { return actionDate; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}