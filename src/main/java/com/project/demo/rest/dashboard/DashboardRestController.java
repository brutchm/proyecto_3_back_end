package com.project.demo.rest.dashboard;

import com.project.demo.logic.dashboard.DashboardService;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.user.User;
import com.project.demo.rest.dashboard.DTO.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardRestController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDashboardSummary(HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DashboardSummaryDTO summary = dashboardService.getSummaryForUser(currentUser.getId());
        return new GlobalResponseHandler().handleResponse("Resumen del dashboard recuperado correctamente", summary, HttpStatus.OK, request);
    }

    @PostMapping("/reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> generateReport(@RequestBody ReportRequestDTO reportRequest, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (reportRequest.getReportType() == null) {
            return new GlobalResponseHandler().handleResponse("El tipo de informe es obligatorio", HttpStatus.BAD_REQUEST, request);
        }

        switch (reportRequest.getReportType()) {
            case INCOME_VS_EXPENSES:
                IncomeVsExpensesDTO incomeVsExpensesData = dashboardService.generateIncomeVsExpensesReport(currentUser.getId(), reportRequest);
                return new GlobalResponseHandler().handleResponse("Informe de ingresos vs gastos generado exitosamente", incomeVsExpensesData, HttpStatus.OK, request);

            case CROP_YIELD:
                List<CropYieldDTO> cropYieldReportData = dashboardService.generateCropYieldReport(currentUser.getId(), reportRequest);
                return new GlobalResponseHandler().handleResponse("Informe de rendimiento de cultivos generado correctamente", cropYieldReportData, HttpStatus.OK, request);

            case PLOT_YIELD:
                try {
                    List<PlotYieldDTO> plotYieldReportData = dashboardService.generatePlotYieldReport(currentUser.getId(), reportRequest);
                    return new GlobalResponseHandler().handleResponse("Informe de rendimiento de la parcela generado correctamente", plotYieldReportData, HttpStatus.OK, request);
                } catch (IllegalArgumentException e) {
                    return new GlobalResponseHandler().handleResponse(e.getMessage(), HttpStatus.BAD_REQUEST, request);
                }

            case CROP_COSTS:
                List<CropCostDTO> cropCostReportData = dashboardService.generateCropCostReport(currentUser.getId(), reportRequest);
                return new GlobalResponseHandler().handleResponse("Informe de costos de cultivo generado exitosamente", cropCostReportData, HttpStatus.OK, request);

            case OPERATIONAL_COSTS:
                List<OperationalCostDTO> operationalCostReportData = dashboardService.generateOperationalCostReport(currentUser.getId(), reportRequest);
                return new GlobalResponseHandler().handleResponse("Informe de costos operativos generado exitosamente", operationalCostReportData, HttpStatus.OK, request);

            case FARM_COSTS: // **NUEVO CASE**
                List<FarmCostDTO> reportData = dashboardService.generateFarmCostReport(currentUser.getId(), reportRequest);
                return new GlobalResponseHandler().handleResponse("Informe de costos de la granja generado exitosamente", reportData, HttpStatus.OK, request);

            default:
                return new GlobalResponseHandler().handleResponse("Tipo de informe no válido o no compatible", HttpStatus.BAD_REQUEST, request);
        }
    }

    @GetMapping("/monthly-chart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDashboardMonthlyChart(HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DoughnutChartDTO chartData = dashboardService.getCurrentMonthChartForUser(currentUser.getId());
        return new GlobalResponseHandler().handleResponse("Datos del gráfico mensual recuperados correctamente", chartData, HttpStatus.OK, request);
    }

    @GetMapping("/crop-yield")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDashboardCropYield(HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<TopCropYieldDTO> cropYieldData = dashboardService.getTopCropYieldsForUser(currentUser.getId());
        return new GlobalResponseHandler().handleResponse("Datos de rendimiento de los top 5 cultivos se recuperaron correctamente", cropYieldData, HttpStatus.OK, request);
    }
}
