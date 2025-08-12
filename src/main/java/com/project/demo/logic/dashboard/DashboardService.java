package com.project.demo.logic.dashboard;

import com.project.demo.logic.entity.transaction.TransactionRepository;
import com.project.demo.rest.dashboard.DTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Obtiene un resumen del dashboard para el usuario especificado.
     * Incluye ingresos totales, gastos totales y número de transacciones.
     */
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummaryForUser(Long userId) {
        Double totalIncome = transactionRepository.sumTotalIncomeByUserId(userId);
        Double totalExpenses = transactionRepository.sumTotalExpensesByUserId(userId);
        Long transactionCount = transactionRepository.countTransactionsByUserId(userId);

        return new DashboardSummaryDTO(totalIncome, totalExpenses, transactionCount);
    }

    /**
     * Genera los datos para el gráfico de ingresos vs. gastos basado en el rango de fechas y la finca proporcionados.
     */
    @Transactional(readOnly = true)
    public IncomeVsExpensesDTO generateIncomeVsExpensesReport(Long userId, ReportRequestDTO request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);

        List<IMonthlySummary> summaries = transactionRepository.getIncomeVsExpensesSummary(
                userId, startDateTime, endDateTime, request.getFarmId());

        List<String> labels = summaries.stream().map(IMonthlySummary::getMonth).collect(Collectors.toList());
        List<Double> incomeData = summaries.stream().map(IMonthlySummary::getTotalIncome).collect(Collectors.toList());
        List<Double> expensesData = summaries.stream().map(IMonthlySummary::getTotalExpenses).collect(Collectors.toList());

        return new IncomeVsExpensesDTO(labels, incomeData, expensesData);
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
    public List<TopCropYieldDTO> getTopCropYieldsForUser(Long userId) {
        return transactionRepository.findTop5CropYieldsByUserId(userId);
    }

    /**
     * Genera los datos para el reporte de rendimiento por parcela.
     */
    @Transactional(readOnly = true)
    public List<PlotYieldDTO> generatePlotYieldReport(Long userId, ReportRequestDTO request) {
        if (request.getFarmId() == null) {
            throw new IllegalArgumentException("Farm ID is required for Plot Yield report.");
        }

        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);

        List<IPlotYield> projections = transactionRepository.getPlotYieldSummary(
                userId,
                request.getFarmId(),
                startDateTime,
                endDateTime
        );

        return projections.stream()
                .map(p -> new PlotYieldDTO(
                        p.getPlotName(),
                        p.getCropName(),
                        p.getTotalQuantitySold(),
                        p.getMeasureUnit()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Genera los datos para el reporte de rendimiento por cultivo.
     */
    @Transactional(readOnly = true)
    public List<CropYieldDTO> generateCropYieldReport(Long userId, ReportRequestDTO request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);

        List<ICropYield> projections = transactionRepository.getCropYieldSummary(
                userId,
                startDateTime,
                endDateTime,
                request.getFarmId(),
                request.getCropId()
        );

        return projections.stream()
                .map(p -> new CropYieldDTO(
                        p.getCropName(),
                        p.getTotalQuantitySold(),
                        p.getMeasureUnit(),
                        p.getTotalIncome(),
                        p.getTotalExpenses()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Genera los datos para el reporte de costos por cultivo.
     */
    @Transactional(readOnly = true)
    public List<CropCostDTO> generateCropCostReport(Long userId, ReportRequestDTO request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);

        List<ICropCost> projections = transactionRepository.getCropCostSummary(
                userId,
                startDateTime,
                endDateTime,
                request.getFarmId()
        );

        return projections.stream()
                .map(p -> new CropCostDTO(
                        p.getCropName(),
                        p.getTotalCost()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Genera los datos para el reporte de costos operativos.
     */
    @Transactional(readOnly = true)
    public List<OperationalCostDTO> generateOperationalCostReport(Long userId, ReportRequestDTO request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);

        List<IOperationalCost> projections = transactionRepository.getOperationalCostSummary(
                userId,
                startDateTime,
                endDateTime,
                request.getFarmId()
        );

        return projections.stream()
                .map(p -> new OperationalCostDTO(
                        p.getMonth(),
                        p.getTotalCost()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Genera los datos para el reporte de costos por finca.
     */
    @Transactional(readOnly = true)
    public List<FarmCostDTO> generateFarmCostReport(Long userId, ReportRequestDTO request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);

        List<IFarmCost> projections = transactionRepository.getFarmCostSummary(
                userId,
                startDateTime,
                endDateTime
        );

        return projections.stream()
                .map(p -> new FarmCostDTO(
                        p.getFarmName(),
                        p.getTotalCost()
                ))
                .collect(Collectors.toList());
    }
}
