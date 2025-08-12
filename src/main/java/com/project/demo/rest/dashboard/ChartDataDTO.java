package com.project.demo.rest.dashboard;

import java.util.List;

/**
 * DTO para la respuesta del gráfico del dashboard.
 * Contiene las etiquetas y los conjuntos de datos para el gráfico.
 */
public class ChartDataDTO {
    private List<String> labels;
    private List<Double> incomeData;
    private List<Double> expensesData;

    public ChartDataDTO(List<String> labels, List<Double> incomeData, List<Double> expensesData) {
        this.labels = labels;
        this.incomeData = incomeData;
        this.expensesData = expensesData;
    }

    // Getters y Setters
    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public List<Double> getIncomeData() { return incomeData; }
    public void setIncomeData(List<Double> incomeData) { this.incomeData = incomeData; }
    public List<Double> getExpensesData() { return expensesData; }
    public void setExpensesData(List<Double> expensesData) { this.expensesData = expensesData; }
}
