package com.project.demo.logic.dashboard;

public class MonthlySummary {
    private String month;
    private Double totalIncome;
    private Double totalExpenses;

    public MonthlySummary(String month, Double totalIncome, Double totalExpenses) {
        this.month = month;
        this.totalIncome = totalIncome != null ? totalIncome : 0.0;
        this.totalExpenses = totalExpenses != null ? totalExpenses : 0.0;
    }

    // Getters
    public String getMonth() { return month; }
    public Double getTotalIncome() { return totalIncome; }
    public Double getTotalExpenses() { return totalExpenses; }
}
