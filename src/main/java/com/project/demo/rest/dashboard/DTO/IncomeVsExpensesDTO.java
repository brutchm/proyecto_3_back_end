package com.project.demo.rest.dashboard.DTO;

import java.util.List;

/**
 * DTO para la respuesta del reporte de Ingresos vs. Egresos.
 * Diseñado para ser consumido directamente por una librería de gráficos en el frontend.
 */
public class IncomeVsExpensesDTO {

    private List<String> labels;
    private List<Double> incomeData;
    private List<Double> expensesData;

    public IncomeVsExpensesDTO(List<String> labels, List<Double> incomeData, List<Double> expensesData) {
        this.labels = labels;
        this.incomeData = incomeData;
        this.expensesData = expensesData;
    }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public List<Double> getIncomeData() { return incomeData; }
    public void setIncomeData(List<Double> incomeData) { this.incomeData = incomeData; }
    public List<Double> getExpensesData() { return expensesData; }
    public void setExpensesData(List<Double> expensesData) { this.expensesData = expensesData; }
}
