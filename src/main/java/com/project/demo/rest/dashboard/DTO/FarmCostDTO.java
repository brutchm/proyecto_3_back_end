package com.project.demo.rest.dashboard.DTO;

/**
 * DTO para representar una fila en el reporte de costos por finca.
 */
public class FarmCostDTO {
    private String farmName;
    private Double totalCost;

    public FarmCostDTO(String farmName, Double totalCost) {
        this.farmName = farmName;
        this.totalCost = totalCost != null ? totalCost : 0.0;
    }

    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }
}