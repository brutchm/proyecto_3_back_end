package com.project.demo.rest.dashboard.DTO;

/**
 * DTO para representar una fila en el reporte de rendimiento por parcela.
 */
public class PlotYieldDTO {
    private String plotName;
    private String cropName;
    private Double totalQuantitySold;
    private String measureUnit;

    public PlotYieldDTO(String plotName, String cropName, Double totalQuantitySold, String measureUnit) {
        this.plotName = plotName;
        this.cropName = cropName;
        this.totalQuantitySold = totalQuantitySold != null ? totalQuantitySold : 0.0;
        this.measureUnit = measureUnit;
    }

    public String getPlotName() { return plotName; }
    public void setPlotName(String plotName) { this.plotName = plotName; }
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public Double getTotalQuantitySold() { return totalQuantitySold; }
    public void setTotalQuantitySold(Double totalQuantitySold) { this.totalQuantitySold = totalQuantitySold; }
    public String getMeasureUnit() { return measureUnit; }
    public void setMeasureUnit(String measureUnit) { this.measureUnit = measureUnit; }
}
