package com.project.demo.rest.dashboard.DTO;

/**
 * DTO para representar una fila en el reporte de costos operativos.
 */
public class OperationalCostDTO {
    private String month;
    private Double totalCost;

    public OperationalCostDTO(String month, Double totalCost) {
        this.month = month;
        this.totalCost = totalCost != null ? totalCost : 0.0;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }
}