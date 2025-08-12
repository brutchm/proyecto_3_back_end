package com.project.demo.logic.dashboard;

/**
 * Proyección de interfaz para los resultados de la consulta de rendimiento por parcela.
 * Spring Data JPA implementará automáticamente esta interfaz.
 * Los nombres de los métodos (getPlotName, getCropName, etc.) deben coincidir
 * con los alias (AS plotName, AS cropName) de la consulta SQL nativa.
 */
public interface IPlotYield {
    String getPlotName();
    String getCropName();
    Double getTotalQuantitySold();
    String getMeasureUnit();
}