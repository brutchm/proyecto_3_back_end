package com.project.demo.rest.dashboard;

import java.time.LocalDate;

/**
 * DTO para recibir los parámetros de una solicitud de reporte desde el frontend.
 */
public class ReportRequestDTO {

    private ReportTypeEnum reportTypeEnum; // Ej: "INCOME_VS_EXPENSES"
    private LocalDate startDate;
    private LocalDate endDate;
    private Long farmId; // Opcional

    // Getters y Setters
    public ReportTypeEnum getReportType() { return reportTypeEnum; }
    public void setReportType(ReportTypeEnum reportTypeEnum) { this.reportTypeEnum = reportTypeEnum; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Long getFarmId() { return farmId; }
    public void setFarmId(Long farmId) { this.farmId = farmId; }
}
