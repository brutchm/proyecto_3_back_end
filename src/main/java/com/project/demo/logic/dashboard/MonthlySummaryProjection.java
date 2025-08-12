package com.project.demo.logic.dashboard;

/**
 * Proyección de interfaz para los resultados de la consulta de resumen mensual.
 * Spring Data JPA implementará automáticamente esta interfaz.
 */
public interface MonthlySummaryProjection {
    String getMonth();
    Double getTotalIncome();
    Double getTotalExpenses();
}
