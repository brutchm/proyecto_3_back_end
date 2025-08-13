package com.project.demo.rest.dashboard.DTO;

/**
 * DTO para representar una fila en el reporte de rendimiento por cultivo.
 */
public class CropYieldDTO {
    private String cropName;
    private Double totalQuantitySold;
    private String measureUnit;
    private Double totalIncome;
    private Double totalExpenses;
    private Double netProfit;

    public CropYieldDTO(String cropName, Double totalQuantitySold, String measureUnit, Double totalIncome, Double totalExpenses) {
        this.cropName = cropName;
        this.totalQuantitySold = totalQuantitySold != null ? totalQuantitySold : 0.0;
        this.measureUnit = measureUnit;
        this.totalIncome = totalIncome != null ? totalIncome : 0.0;
        this.totalExpenses = totalExpenses != null ? totalExpenses : 0.0;
        this.netProfit = this.totalIncome - this.totalExpenses;
    }

    public String getCropName() { return cropName; }
    public Double getTotalQuantitySold() { return totalQuantitySold; }
    public String getMeasureUnit() { return measureUnit; }
    public Double getTotalIncome() { return totalIncome; }
    public Double getTotalExpenses() { return totalExpenses; }
    public Double getNetProfit() { return netProfit; }
}