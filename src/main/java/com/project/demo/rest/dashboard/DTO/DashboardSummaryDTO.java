package com.project.demo.rest.dashboard.DTO;

public class DashboardSummaryDTO {
    private double totalIncome;
    private double totalExpenses;
    private double netBalance;
    private long transactionCount;

    public DashboardSummaryDTO(double totalIncome, double totalExpenses, long transactionCount) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.netBalance = totalIncome - totalExpenses;
        this.transactionCount = transactionCount;
    }

    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }
    public double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(double totalExpenses) { this.totalExpenses = totalExpenses; }
    public double getNetBalance() { return netBalance; }
    public void setNetBalance(double netBalance) { this.netBalance = netBalance; }
    public long getTransactionCount() { return transactionCount; }
    public void setTransactionCount(long transactionCount) { this.transactionCount = transactionCount; }
}
