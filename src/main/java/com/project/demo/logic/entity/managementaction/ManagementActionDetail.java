package com.project.demo.logic.entity.managementaction;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "management_actions_details")
public class ManagementActionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private ManagementAction action;

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
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ManagementAction getAction() { return action; }
    public void setAction(ManagementAction action) { this.action = action; }
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