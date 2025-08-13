package com.project.demo.rest.dashboard.DTO;

import java.util.List;

/**
 * DTO para representar los datos de un gráfico de tipo Doughnut.
 */
public class DoughnutChartDTO {
    private List<String> labels;
    private List<Double> data;

    public DoughnutChartDTO(List<String> labels, List<Double> data) {
        this.labels = labels;
        this.data = data;
    }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
}
