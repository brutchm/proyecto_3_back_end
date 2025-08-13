package com.project.demo.logic.dashboard;

/**
 * Proyección de interfaz para los resultados de la consulta de rendimiento por cultivo.
 * Los nombres de los métodos deben coincidir con los alias de la consulta SQL.
 */
public interface ICropYield {
    String getCropName();
    Double getTotalQuantitySold();
    String getMeasureUnit();
    Double getTotalIncome();
    Double getTotalExpenses();
}