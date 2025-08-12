package com.project.demo.rest.transaction;

import com.project.demo.logic.entity.transaction.MeasureUnitEnum;
import com.project.demo.logic.entity.transaction.Transaction;
import com.project.demo.logic.entity.transaction.TransactionEnum;

import java.time.LocalDateTime;

/**
 * DTO para la entidad Transaction.
 * Este objeto transfiere los datos de forma segura y eficiente a la API,
 * evitando exponer las entidades de la base de datos directamente.
 */
public class TransactionDTO {

    private Long id;
    private Long farmId;
    private String farmName;
    private Long cropId;
    private String cropName;
    private TransactionEnum transactionType;
    private Double quantity;
    private MeasureUnitEnum measureUnit;
    private Double pricePerUnit;
    private Double totalValue;
    private LocalDateTime transactionDate;

    public TransactionDTO(Transaction entity) {
        this.id = entity.getId();
        if (entity.getFarm() != null) {
            this.farmId = entity.getFarm().getId();
            this.farmName = entity.getFarm().getFarmName();
        }
        if (entity.getCrop() != null) {
            this.cropId = entity.getCrop().getId();
            this.cropName = entity.getCrop().getCropName();
        }
        this.transactionType = entity.getTransactionType();
        this.quantity = entity.getQuantity();
        this.measureUnit = entity.getMeasureUnit();
        this.pricePerUnit = entity.getPricePerUnit();
        this.totalValue = entity.getTotalValue();
        this.transactionDate = entity.getTransactionDate();
    }

    public TransactionDTO() {}

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFarmId() { return farmId; }
    public void setFarmId(Long farmId) { this.farmId = farmId; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public Long getCropId() { return cropId; }
    public void setCropId(Long cropId) { this.cropId = cropId; }
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public TransactionEnum getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionEnum transactionType) { this.transactionType = transactionType; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public MeasureUnitEnum getMeasureUnit() { return measureUnit; }
    public void setMeasureUnit(MeasureUnitEnum measureUnit) { this.measureUnit = measureUnit; }
    public Double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(Double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    public Double getTotalValue() { return totalValue; }
    public void setTotalValue(Double totalValue) { this.totalValue = totalValue; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
}
