package com.project.demo.logic.entity.plot;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representa la geometría geoespacial de una parcela, usualmente almacenada como un polígono en formato GeoJSON.
 * Mantiene una relación uno a uno con FarmPlot.
 */
@Entity
@Table(name = "plot_geometry")
public class PlotGeometry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plot_id", nullable = false, unique = true)
    private FarmPlot farmPlot;

    @Lob
    @Column(name = "geometry_polygon", nullable = false, columnDefinition = "LONGTEXT")
    private String geometryPolygon; // Almacena el polígono como un string GeoJSON.

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public FarmPlot getFarmPlot() { return farmPlot; }
    public void setFarmPlot(FarmPlot farmPlot) { this.farmPlot = farmPlot; }
    public String getGeometryPolygon() { return geometryPolygon; }
    public void setGeometryPolygon(String geometryPolygon) { this.geometryPolygon = geometryPolygon; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}