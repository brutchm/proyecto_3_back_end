package com.project.demo.rest.plot;

import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.farm.FarmRepository;
import com.project.demo.logic.entity.plot.FarmPlot;
import com.project.demo.logic.entity.plot.FarmPlotRepository;
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
 * Controlador REST para gestionar las parcelas (Farm Plots) como un sub-recurso de una granja.
 */
@RestController
@RequestMapping("/farms/{farmId}/plots")
public class FarmPlotRestController {

    @Autowired
    private FarmPlotRepository farmPlotRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private UserXFarmRepository userXFarmRepository;

    /**
     * Crea una nueva parcela dentro de una granja específica.
     * @param farmId El ID de la granja padre.
     * @param plot La parcela a crear.
     * @return La parcela creada.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPlot(@PathVariable Long farmId, @RequestBody FarmPlot plot, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<Farm> farm = farmRepository.findById(farmId);
        if (farm.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Farm with id " + farmId + " not found", HttpStatus.NOT_FOUND, request);
        }

        plot.setFarm(farm.get());
        FarmPlot savedPlot = farmPlotRepository.save(plot);
        return new GlobalResponseHandler().handleResponse("Plot created successfully", savedPlot, HttpStatus.CREATED, request);
    }

    /**
     * Obtiene todas las parcelas de una granja específica.
     * @param farmId El ID de la granja padre.
     * @return Una lista de parcelas.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPlotsByFarm(@PathVariable Long farmId, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        List<FarmPlot> plots = farmPlotRepository.findByFarmId(farmId);
        return new GlobalResponseHandler().handleResponse("Plots for farm " + farmId + " retrieved successfully", plots, HttpStatus.OK, request);
    }

    /**
     * Obtiene una parcela específica por su ID, dentro de una granja.
     * @param farmId El ID de la granja padre.
     * @param plotId El ID de la parcela.
     * @return La parcela encontrada.
     */
    @GetMapping("/{plotId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPlotById(@PathVariable Long farmId, @PathVariable Long plotId, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<FarmPlot> plot = farmPlotRepository.findByIdAndFarmId(plotId, farmId);
        if (plot.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Plot retrieved successfully", plot.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Plot with id " + plotId + " not found in farm " + farmId, HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Actualiza una parcela existente.
     * @param farmId El ID de la granja padre.
     * @param plotId El ID de la parcela a actualizar.
     * @param plotDetails Los nuevos datos para la parcela.
     * @return La parcela actualizada.
     */
    @PutMapping("/{plotId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePlot(@PathVariable Long farmId, @PathVariable Long plotId, @RequestBody FarmPlot plotDetails, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<FarmPlot> optionalPlot = farmPlotRepository.findByIdAndFarmId(plotId, farmId);
        if (optionalPlot.isPresent()) {
            FarmPlot existingPlot = optionalPlot.get();
            existingPlot.setPlotName(plotDetails.getPlotName());
            existingPlot.setPlotDescription(plotDetails.getPlotDescription());
            existingPlot.setPlotType(plotDetails.getPlotType());
            existingPlot.setCurrentUsage(plotDetails.getCurrentUsage());
            existingPlot.setActive(plotDetails.isActive());
            existingPlot.setGeometryPolygon(plotDetails.getGeometryPolygon());
            FarmPlot updatedPlot = farmPlotRepository.save(existingPlot);
            return new GlobalResponseHandler().handleResponse("Plot updated successfully", updatedPlot, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Plot with id " + plotId + " not found in farm " + farmId, HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina una parcela.
     * @param farmId El ID de la granja padre.
     * @param plotId El ID de la parcela a eliminar.
     * @return Confirmación de la eliminación.
     */
    @DeleteMapping("/{plotId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePlot(@PathVariable Long farmId, @PathVariable Long plotId, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<FarmPlot> optionalPlot = farmPlotRepository.findByIdAndFarmId(plotId, farmId);
        if (optionalPlot.isPresent()) {
            farmPlotRepository.delete(optionalPlot.get());
            return new GlobalResponseHandler().handleResponse("Plot deleted successfully", optionalPlot.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Plot with id " + plotId + " not found in farm " + farmId, HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Metodo de ayuda para verificar el acceso del usuario a la granja.
     */
    private boolean hasAccessToFarm(Long farmId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }
        return userXFarmRepository.existsById(new UserFarmId(farmId, currentUser.getId()));
    }
}