package com.project.demo.rest.dashboard;

import com.project.demo.logic.dashboard.DashboardService;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.user.User;
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
        return new GlobalResponseHandler().handleResponse("Dashboard summary retrieved successfully", summary, HttpStatus.OK, request);
    }

    @GetMapping("/daily-chart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDashboardDailyChart(HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ChartDataDTO chartData = dashboardService.getDailyChartForUser(currentUser.getId());
        return new GlobalResponseHandler().handleResponse("Daily chart data retrieved successfully", chartData, HttpStatus.OK, request);
    }

    @PostMapping("/reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> generateReport(@RequestBody ReportRequestDTO reportRequest, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (reportRequest.getReportType() == null) {
            return new GlobalResponseHandler().handleResponse("Report type is required", HttpStatus.BAD_REQUEST, request);
        }

        switch (reportRequest.getReportType()) {
            case INCOME_VS_EXPENSES:
                // La lógica para reportes tabulares se implementará aquí en el futuro
                // Por ahora, devolvemos un placeholder o un error.
                return new GlobalResponseHandler().handleResponse("Tabular report not yet implemented", HttpStatus.NOT_IMPLEMENTED, request);
            case PLOT_YIELD: // **NUEVO CASE**
                try {
                    List<PlotYieldDTO> reportData = dashboardService.generatePlotYieldReport(currentUser.getId(), reportRequest);
                    return new GlobalResponseHandler().handleResponse("Plot Yield report generated successfully", reportData, HttpStatus.OK, request);
                } catch (IllegalArgumentException e) {
                    return new GlobalResponseHandler().handleResponse(e.getMessage(), HttpStatus.BAD_REQUEST, request);
                }
            default:
                return new GlobalResponseHandler().handleResponse("Invalid or unsupported report type", HttpStatus.BAD_REQUEST, request);
        }
    }

    @GetMapping("/monthly-chart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDashboardMonthlyChart(HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DoughnutChartDTO chartData = dashboardService.getCurrentMonthChartForUser(currentUser.getId());
        return new GlobalResponseHandler().handleResponse("Monthly chart data retrieved successfully", chartData, HttpStatus.OK, request);
    }

    @GetMapping("/crop-yield")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDashboardCropYield(HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<CropYieldDTO> cropYieldData = dashboardService.getTopCropYieldsForUser(currentUser.getId());
        return new GlobalResponseHandler().handleResponse("Top 5 crop yield data retrieved successfully", cropYieldData, HttpStatus.OK, request);
    }
}
