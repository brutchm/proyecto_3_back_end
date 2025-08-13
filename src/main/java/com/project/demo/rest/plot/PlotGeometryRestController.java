package com.project.demo.rest.plot;

import com.project.demo.logic.entity.plot.FarmPlot;
import com.project.demo.logic.entity.plot.FarmPlotRepository;
import com.project.demo.logic.entity.plot.PlotGeometry;
import com.project.demo.logic.entity.plot.PlotGeometryRepository;
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

import java.util.Optional;

/**
 * Controlador REST para gestionar la geometría de una parcela.
 * La geometría es un recurso único (singleton) para cada parcela.
 */
@RestController
@RequestMapping("/plots/{plotId}/geometry")
public class PlotGeometryRestController {

    @Autowired
    private PlotGeometryRepository geometryRepository;

    @Autowired
    private FarmPlotRepository farmPlotRepository;
    @Autowired
    private UserXFarmRepository userXFarmRepository;

    /**
     * Crea la geometría de una parcela (solo si no existe).
     * @param plotId El ID de la parcela.
     * @param geometryPolygonJson El string GeoJSON del polígono.
     * @return La geometría creada.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createGeometry(@PathVariable Long plotId, @RequestBody String geometryPolygonJson, HttpServletRequest request) {
        Optional<FarmPlot> optionalPlot = farmPlotRepository.findById(plotId);
        if (optionalPlot.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Plot with id " + plotId + " not found", HttpStatus.NOT_FOUND, request);
        }

        if (!hasAccessToFarm(optionalPlot.get().getFarm().getId())) {
            return new GlobalResponseHandler().handleResponse("Access Denied to this plot's farm", HttpStatus.FORBIDDEN, request);
        }

        if (geometryRepository.findByFarmPlot_Id(plotId).isPresent()) {
            return new GlobalResponseHandler().handleResponse("Geometry already exists for plot " + plotId, HttpStatus.CONFLICT, request);
        }

        PlotGeometry geometry = new PlotGeometry();
        geometry.setFarmPlot(optionalPlot.get());
        geometry.setGeometryPolygon(geometryPolygonJson);

        PlotGeometry savedGeometry = geometryRepository.save(geometry);
        return new GlobalResponseHandler().handleResponse("Geometry created successfully", savedGeometry, HttpStatus.CREATED, request);
    }

    /**
     * Obtiene la geometría de una parcela específica, validando el acceso.
     * @param plotId El ID de la parcela.
     * @return La geometría encontrada.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getGeometry(@PathVariable Long plotId, HttpServletRequest request) {
        if (!hasAccessToPlot(plotId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to this plot", HttpStatus.FORBIDDEN, request);
        }

        Optional<PlotGeometry> geometry = geometryRepository.findByFarmPlot_Id(plotId);
        if (geometry.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Geometry retrieved successfully", geometry.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Geometry not found for plot " + plotId, HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Crea o actualiza la geometría de una parcela.
     * @param plotId El ID de la parcela.
     * @param geometryPolygonJson La solicitud con el string GeoJSON del polígono.
     * @return La geometría guardada.
     */
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> setGeometry(@PathVariable Long plotId, @RequestBody String geometryPolygonJson, HttpServletRequest request) {
        Optional<FarmPlot> optionalPlot = farmPlotRepository.findById(plotId);
        if (optionalPlot.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Plot with id " + plotId + " not found", HttpStatus.NOT_FOUND, request);
        }

        if (!hasAccessToFarm(optionalPlot.get().getFarm().getId())) {
            return new GlobalResponseHandler().handleResponse("Access Denied to this plot's farm", HttpStatus.FORBIDDEN, request);
        }

        PlotGeometry geometry = geometryRepository.findByFarmPlot_Id(plotId)
                .orElse(new PlotGeometry());

        geometry.setFarmPlot(optionalPlot.get());
        geometry.setGeometryPolygon(geometryPolygonJson);

        PlotGeometry savedGeometry = geometryRepository.save(geometry);
        return new GlobalResponseHandler().handleResponse("Geometry saved successfully", savedGeometry, HttpStatus.OK, request);
    }

    /**
     * Elimina la geometría de una parcela.
     * @param plotId El ID de la parcela.
     * @return Confirmación de la eliminación.
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteGeometry(@PathVariable Long plotId, HttpServletRequest request) {
        if (!hasAccessToPlot(plotId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to this plot", HttpStatus.FORBIDDEN, request);
        }

        Optional<PlotGeometry> optionalGeometry = geometryRepository.findByFarmPlot_Id(plotId);
        if (optionalGeometry.isPresent()) {
            geometryRepository.delete(optionalGeometry.get());
            return new GlobalResponseHandler().handleResponse("Geometry deleted successfully", optionalGeometry.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Geometry not found for plot " + plotId, HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Metodo de ayuda para verificar si el usuario actual tiene acceso a una parcela
     * a través de su pertenencia a la granja padre.
     */
    private boolean hasAccessToPlot(Long plotId) {
        Optional<FarmPlot> optionalPlot = farmPlotRepository.findById(plotId);
        return optionalPlot.map(farmPlot -> hasAccessToFarm(farmPlot.getFarm().getId())).orElse(false);
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