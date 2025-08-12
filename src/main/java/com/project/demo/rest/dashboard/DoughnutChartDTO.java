package com.project.demo.rest.dashboard;

import java.util.List;

/**
 * DTO para a resposta do gráfico circular (doughnut) do dashboard.
 */
public class DoughnutChartDTO {
    private List<String> labels; // Ex: ["Ingresos", "Egresos"]
    private List<Double> data;   // Ex: [5000.0, 2500.0]

    public DoughnutChartDTO(List<String> labels, List<Double> data) {
        this.labels = labels;
        this.data = data;
    }

    // Getters e Setters
    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
}
