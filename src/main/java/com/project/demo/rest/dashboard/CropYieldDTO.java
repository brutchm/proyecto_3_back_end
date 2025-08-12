package com.project.demo.rest.dashboard;

/**
 * DTO para representar el rendimiento de un solo cultivo.
 */
public class CropYieldDTO {
    private String cropName;
    private Double totalQuantity;

    public CropYieldDTO(String cropName, Double totalQuantity) {
        this.cropName = cropName;
        this.totalQuantity = totalQuantity;
    }

    // Getters y Setters
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public Double getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Double totalQuantity) { this.totalQuantity = totalQuantity; }
}
