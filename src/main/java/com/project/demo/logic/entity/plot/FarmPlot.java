
package com.project.demo.logic.entity.plot;

import com.project.demo.logic.entity.cropsmanagement.CropsManagement;
import com.project.demo.logic.entity.farm.Farm;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "farm_plots")
public class FarmPlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false, foreignKey = @ForeignKey(name = "fk_farm"))
    @OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JsonIgnore
    private Farm farm;

    @Column(name = "plot_name", length = 150, nullable = false)
    private String plotName;

    @Lob
    @Column(name = "plot_description", columnDefinition = "TEXT")
    private String plotDescription;

    @Column(name = "plot_type", length = 100, nullable = false)
    private String plotType;


    @Column(name = "current_usage", length = 100)
    private String currentUsage;

    @Lob
    @Column(name = "geometry_polygon", columnDefinition = "LONGTEXT")
    private String geometryPolygon; // Stores the polygon as GeoJSON string

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "isActive", columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isActive;

    @OneToMany(mappedBy = "farmPlot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CropsManagement> cropsManagements = new ArrayList<>();

    public List<CropsManagement> getCropsManagements() {
        return cropsManagements;
    }

    public void setCropsManagements(List<CropsManagement> cropsManagements) {
        this.cropsManagements = cropsManagements;
    }


    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Farm getFarm() { return farm; }
    public void setFarm(Farm farm) { this.farm = farm; }
    public String getPlotName() { return plotName; }
    public void setPlotName(String plotName) { this.plotName = plotName; }
    public String getPlotDescription() { return plotDescription; }
    public void setPlotDescription(String plotDescription) { this.plotDescription = plotDescription; }
    public String getPlotType() { return plotType; }
    public void setPlotType(String plotType) { this.plotType = plotType; }
    public String getCurrentUsage() { return currentUsage; }
    public void setCurrentUsage(String currentUsage) { this.currentUsage = currentUsage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getGeometryPolygon() { return geometryPolygon; }
    public void setGeometryPolygon(String geometryPolygon) { this.geometryPolygon = geometryPolygon; }
}