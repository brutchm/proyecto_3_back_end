package com.project.demo.logic.entity.plot;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plot_history")
public class PlotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plot_id", nullable = false)
    private FarmPlot farmPlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private RecordTypeEnum recordType;

    @Column(name = "record_name", length = 100, nullable = false)
    private String recordName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

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
    public FarmPlot getFarmPlot() { return farmPlot; }
    public void setFarmPlot(FarmPlot farmPlot) { this.farmPlot = farmPlot; }
    public RecordTypeEnum getRecordType() { return recordType; }
    public void setRecordType(RecordTypeEnum recordType) { this.recordType = recordType; }
    public String getRecordName() { return recordName; }
    public void setRecordName(String recordName) { this.recordName = recordName; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}