package com.project.demo.logic.dashboard;

import com.project.demo.logic.entity.transaction.TransactionRepository;
import com.project.demo.rest.dashboard.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummaryForUser(Long userId) {
        Double totalIncome = transactionRepository.sumTotalIncomeByUserId(userId);
        Double totalExpenses = transactionRepository.sumTotalExpensesByUserId(userId);
        Long transactionCount = transactionRepository.countTransactionsByUserId(userId);

        return new DashboardSummaryDTO(totalIncome, totalExpenses, transactionCount);
    }

//    @Transactional(readOnly = true)
//    public IncomeVsExpensesDTO generateIncomeVsExpensesReport(Long userId, ReportRequestDTO request) {
//        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
//        LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);
//
//        List<MonthlySummary> summaries = transactionRepository.getIncomeVsExpensesSummary(
//                userId, startDateTime, endDateTime, request.getFarmId());
//
//        List<String> labels = summaries.stream().map(MonthlySummary::getMonth).collect(Collectors.toList());
//        List<Double> incomeData = summaries.stream().map(MonthlySummary::getTotalIncome).collect(Collectors.toList());
//        List<Double> expensesData = summaries.stream().map(MonthlySummary::getTotalExpenses).collect(Collectors.toList());
//
//        return new IncomeVsExpensesDTO(labels, incomeData, expensesData);
//    }

    @Transactional(readOnly = true)
    public IncomeVsExpensesDTO generateIncomeVsExpensesReport(Long userId, ReportRequestDTO request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);

        // **CORRECCIÓN:** Llamamos al nuevo método del repositorio.
        List<MonthlySummaryProjection> summaries = transactionRepository.getIncomeVsExpensesSummary(
                userId, startDateTime, endDateTime, request.getFarmId());

        // Mapeamos los resultados de la proyección al DTO de respuesta.
        List<String> labels = summaries.stream().map(MonthlySummaryProjection::getMonth).collect(Collectors.toList());
        List<Double> incomeData = summaries.stream().map(MonthlySummaryProjection::getTotalIncome).collect(Collectors.toList());
        List<Double> expensesData = summaries.stream().map(MonthlySummaryProjection::getTotalExpenses).collect(Collectors.toList());

        return new IncomeVsExpensesDTO(labels, incomeData, expensesData);
    }

    /**
     * Genera los datos para el gráfico de ingresos vs. egresos de los últimos 30 días.
     * Rellena los días sin transacciones con ceros para asegurar un eje de tiempo continuo.
     */
    @Transactional(readOnly = true)
    public ChartDataDTO getDailyChartForUser(Long userId) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(29).toLocalDate().atStartOfDay();
        List<DailySummaryProjection> summaries = transactionRepository.getDailyIncomeVsExpensesSummary(userId, startDate);

        Map<LocalDate, DailySummaryProjection> summaryMap = summaries.stream()
                .collect(Collectors.toMap(DailySummaryProjection::getDate, Function.identity()));

        List<String> labels = new ArrayList<>();
        List<Double> incomeData = new ArrayList<>();
        List<Double> expensesData = new ArrayList<>();

        LocalDate currentDate = LocalDate.now().minusDays(29);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for (int i = 0; i < 30; i++) {
            labels.add(currentDate.format(formatter));

            DailySummaryProjection summary = summaryMap.get(currentDate);
            if (summary != null) {
                incomeData.add(summary.getTotalIncome());
                expensesData.add(summary.getTotalExpenses());
            } else {
                incomeData.add(0.0);
                expensesData.add(0.0);
            }
            currentDate = currentDate.plusDays(1);
        }

        return new ChartDataDTO(labels, incomeData, expensesData);
    }

    /**
     * Gera os dados para o gráfico circular de ingressos vs. egressos do mês atual.
     */
    @Transactional(readOnly = true)
    public DoughnutChartDTO getCurrentMonthChartForUser(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);

        Double totalIncome = transactionRepository.sumTotalIncomeByUserIdAndDateRange(userId, startOfMonth, endOfMonth);
        Double totalExpenses = transactionRepository.sumTotalExpensesByUserIdAndDateRange(userId, startOfMonth, endOfMonth);

        List<String> labels = List.of("Ingresos", "Egresos");
        List<Double> data = List.of(totalIncome, totalExpenses);

        return new DoughnutChartDTO(labels, data);
    }

    @Transactional(readOnly = true)
    public List<CropYieldDTO> getTopCropYieldsForUser(Long userId) {
        return transactionRepository.findTop5CropYieldsByUserId(userId);
    }

    /**
     * Genera los datos para el reporte de rendimiento por parcela.
     */
    @Transactional(readOnly = true)
    public List<PlotYieldDTO> generatePlotYieldReport(Long userId, ReportRequestDTO request) {
        if (request.getFarmId() == null) {
            // Este reporte requiere una finca específica para ser significativo.
            throw new IllegalArgumentException("Farm ID is required for Plot Yield report.");
        }

        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);

        return transactionRepository.getPlotYieldSummary(
                userId,
                request.getFarmId(),
                startDateTime,
                endDateTime
        );
    }
}
