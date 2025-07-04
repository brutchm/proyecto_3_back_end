package com.project.demo.rest.plot;

import com.project.demo.logic.entity.plot.FarmPlot;
import com.project.demo.logic.entity.plot.FarmPlotRepository;
import com.project.demo.logic.entity.plot.PlotHistory;
import com.project.demo.logic.entity.plot.PlotHistoryRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.userfarm.UserFarmId;
import com.project.demo.logic.entity.userfarm.UserXFarmRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador para gestionar el historial de una parcela.
 * La operación DELETE se omite deliberadamente para preservar la integridad de los datos históricos.
 */
@RestController
@RequestMapping("/plots/{plotId}/history")
public class PlotHistoryRestController {

    @Autowired
    private PlotHistoryRepository plotHistoryRepository;

    @Autowired
    private FarmPlotRepository farmPlotRepository;

    @Autowired
    private UserXFarmRepository userXFarmRepository;

    /**
     * Añade un nuevo registro al historial de la parcela, validando el acceso.
     * @param plotId El ID de la parcela padre.
     * @param historyEntry El registro de historial a crear.
     * @return El registro creado.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addHistory(@PathVariable Long plotId, @RequestBody PlotHistory historyEntry, HttpServletRequest request) {
        Optional<FarmPlot> optionalPlot = farmPlotRepository.findById(plotId);
        if (optionalPlot.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Plot with id " + plotId + " not found", HttpStatus.NOT_FOUND, request);
        }

        if (!hasAccessToFarm(optionalPlot.get().getFarm().getId())) {
            return new GlobalResponseHandler().handleResponse("Access Denied", HttpStatus.FORBIDDEN, request);
        }

        historyEntry.setFarmPlot(optionalPlot.get());
        PlotHistory savedHistory = plotHistoryRepository.save(historyEntry);
        return new GlobalResponseHandler().handleResponse("History entry created", savedHistory, HttpStatus.CREATED, request);
    }

    /**
     * Obtiene todo el historial de una parcela, validando el acceso.
     * @param plotId El ID de la parcela.
     * @return La lista de registros históricos.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getHistory(@PathVariable Long plotId, HttpServletRequest request) {
        Optional<FarmPlot> optionalPlot = farmPlotRepository.findById(plotId);
        if (optionalPlot.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Plot with id " + plotId + " not found", HttpStatus.NOT_FOUND, request);
        }

        if (!hasAccessToFarm(optionalPlot.get().getFarm().getId())) {
            return new GlobalResponseHandler().handleResponse("Access Denied", HttpStatus.FORBIDDEN, request);
        }

        List<PlotHistory> history = plotHistoryRepository.findByFarmPlotId(plotId);
        return new GlobalResponseHandler().handleResponse("History retrieved", history, HttpStatus.OK, request);
    }

    /**
     * Actualiza una entrada de historial existente (para correcciones), validando el acceso.
     * @param plotId El ID de la parcela padre.
     * @param historyId El ID del registro de historial.
     * @param historyDetails Los nuevos datos.
     * @return El registro actualizado.
     */
    @PutMapping("/{historyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateHistory(@PathVariable Long plotId, @PathVariable Long historyId, @RequestBody PlotHistory historyDetails, HttpServletRequest request) {
        Optional<FarmPlot> optionalPlot = farmPlotRepository.findById(plotId);
        if (optionalPlot.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Plot with id " + plotId + " not found", HttpStatus.NOT_FOUND, request);
        }

        if (!hasAccessToFarm(optionalPlot.get().getFarm().getId())) {
            return new GlobalResponseHandler().handleResponse("Access Denied", HttpStatus.FORBIDDEN, request);
        }

        Optional<PlotHistory> optionalHistory = plotHistoryRepository.findById(historyId);
        if (optionalHistory.isPresent()) {
            PlotHistory existingHistory = optionalHistory.get();
            if (!existingHistory.getFarmPlot().getId().equals(plotId)) {
                return new GlobalResponseHandler().handleResponse("History record does not belong to the specified plot", HttpStatus.BAD_REQUEST, request);
            }

            existingHistory.setRecordName(historyDetails.getRecordName());
            existingHistory.setStartDate(historyDetails.getStartDate());
            existingHistory.setEndDate(historyDetails.getEndDate());
            existingHistory.setNotes(historyDetails.getNotes());
            PlotHistory updatedHistory = plotHistoryRepository.save(existingHistory);

            return new GlobalResponseHandler().handleResponse("History entry updated", updatedHistory, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("History entry with id " + historyId + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    private boolean hasAccessToFarm(Long farmId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }
        return userXFarmRepository.existsById(new UserFarmId(farmId, currentUser.getId()));
    }
}