package com.project.demo.logic.dashboard;

import java.time.LocalDate;

/**
 * Proyección de interfaz para los resultados de la consulta de resumen diario.
 * Spring Data JPA implementará automáticamente esta interfaz.
 */
public interface DailySummaryProjection {
    LocalDate getDate();
    Double getTotalIncome();
    Double getTotalExpenses();
}
