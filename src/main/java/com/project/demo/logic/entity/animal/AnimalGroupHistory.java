package com.project.demo.logic.entity.animal;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "animal_groups_history")
public class AnimalGroupHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_group_id", nullable = false)
    private AnimalGroup animalGroup;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "production_value")
    private Double productionValue;

    @Column(name = "production_measure", length = 50)
    private String productionMeasure;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AnimalGroup getAnimalGroup() { return animalGroup; }
    public void setAnimalGroup(AnimalGroup animalGroup) { this.animalGroup = animalGroup; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public Double getProductionValue() { return productionValue; }
    public void setProductionValue(Double productionValue) { this.productionValue = productionValue; }
    public String getProductionMeasure() { return productionMeasure; }
    public void setProductionMeasure(String productionMeasure) { this.productionMeasure = productionMeasure; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}