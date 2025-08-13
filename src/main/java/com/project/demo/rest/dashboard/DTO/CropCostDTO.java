package com.project.demo.rest.dashboard.DTO;

/**
 * DTO para representar una fila en el reporte de costos por cultivo.
 */
public class CropCostDTO {
    private String cropName;
    private Double totalCost;

    public CropCostDTO(String cropName, Double totalCost) {
        this.cropName = cropName;
        this.totalCost = totalCost != null ? totalCost : 0.0;
    }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }
}