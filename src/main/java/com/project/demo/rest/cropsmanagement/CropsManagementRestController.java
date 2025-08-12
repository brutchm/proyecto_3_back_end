package com.project.demo.rest.cropsmanagement;

import com.project.demo.logic.entity.crop.Crop;
import com.project.demo.logic.entity.cropsmanagement.CropsManagement;
import com.project.demo.logic.entity.cropsmanagement.CropsManagementRepository;

import com.project.demo.logic.entity.plot.FarmPlot;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.role.Role;
import com.project.demo.logic.entity.role.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.userfarm.UserFarmId;
import com.project.demo.logic.entity.userfarm.UserXFarm;
import com.project.demo.logic.entity.userfarm.UserXFarmRepository;
import com.project.demo.logic.entity.crop.CropRepository;
import com.project.demo.logic.entity.plot.FarmPlotRepository;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Controlador REST para gestionar los registros de manejo de cultivos en una granja.
 * Se trata como una bitácora inmutable: solo se añaden y consultan registros.
 */
@RestController
@RequestMapping("/plots/{plotId}/management-records")
public class CropsManagementRestController {

    @Autowired
    private CropsManagementRepository cropsManagementRepository;

    @Autowired
    private FarmPlotRepository farmPlotRepository;


    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private UserXFarmRepository userXFarmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Obtiene todos los registros de manejo para una parcela.
     * @param plotId El ID de la parcela.
     * @return Una lista de registros de manejo.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getManagementRecords(@PathVariable Long plotId, HttpServletRequest request) {
        if (!hasAccessToPlot(plotId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to plot " + plotId, HttpStatus.FORBIDDEN, request);
        }
        List<CropsManagement> records = cropsManagementRepository.findByFarmPlot_Id(plotId);
        return new GlobalResponseHandler().handleResponse("Management records retrieved successfully", records, HttpStatus.OK, request);
    }

        /**
     * Obtiene un registro de manejo agrícola por su ID.
     */
    @GetMapping("/{recordId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getManagementRecordById(@PathVariable Long plotId, @PathVariable Long recordId, HttpServletRequest request) {
        if (!hasAccessToPlot(plotId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to plot " + plotId, HttpStatus.FORBIDDEN, request);
        }
        Optional<CropsManagement> recordOpt = cropsManagementRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Record not found", HttpStatus.NOT_FOUND, request);
        }
        return new GlobalResponseHandler().handleResponse("Record retrieved successfully", recordOpt.get(), HttpStatus.OK, request);
    }

    /**
     * Crea un nuevo registro de manejo agrícola para una parcela.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createManagementRecord(@PathVariable Long plotId, @RequestBody CreateManagementRecordRequest req, HttpServletRequest request) {
        if (!hasAccessToPlot(plotId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to plot " + plotId, HttpStatus.FORBIDDEN, request);
        }
        Optional<FarmPlot> plot = farmPlotRepository.findById(plotId);
        Optional<Crop> crop = cropRepository.findById(req.cropId());
        if (plot.isEmpty() || crop.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Plot or Crop not found", HttpStatus.NOT_FOUND, request);
        }
        CropsManagement record = new CropsManagement();
        record.setFarmPlot(plot.get());
        record.setCrop(crop.get());
        record.setFarmId(plot.get().getFarm().getId());
        record.setActionName(req.actionName());
        record.setActionPictureUrl(req.actionPictureUrl());
        record.setMeasureUnit(req.measureUnit());
        record.setMeasureValue(req.measureValue());
        record.setValueSpent(req.valueSpent());
        record.setActionDate(req.actionDate() != null ? req.actionDate() : LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        record.setActive(true);
        CropsManagement saved = cropsManagementRepository.save(record);
        // Build response with cropId included
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", saved.getId());
        response.put("cropId", saved.getCropId());
        response.put("farmId", saved.getFarmId());
        response.put("actionName", saved.getActionName());
        response.put("actionPictureUrl", saved.getActionPictureUrl());
        response.put("measureUnit", saved.getMeasureUnit());
        response.put("measureValue", saved.getMeasureValue());
        response.put("valueSpent", saved.getValueSpent());
        response.put("actionDate", saved.getActionDate());
        response.put("createdAt", saved.getCreatedAt());
        response.put("updatedAt", saved.getUpdatedAt());
        response.put("active", saved.isActive());
        return new GlobalResponseHandler().handleResponse("Record created", response, HttpStatus.CREATED, request);
    }

    /**
     * Actualiza un registro de manejo agrícola para una parcela.
     */
    @PutMapping("/{recordId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateManagementRecord(@PathVariable Long plotId, @PathVariable Long recordId, @RequestBody UpdateManagementRecordRequest req, HttpServletRequest request) {
        if (!hasAccessToPlot(plotId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to plot " + plotId, HttpStatus.FORBIDDEN, request);
        }
        Optional<CropsManagement> recordOpt = cropsManagementRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Record not found", HttpStatus.NOT_FOUND, request);
        }
        CropsManagement record = recordOpt.get();
        if (req.actionName() != null) {
            record.setActionName(req.actionName());
        }
        if (req.actionPictureUrl() != null) {
            record.setActionPictureUrl(req.actionPictureUrl());
        }
        if (req.measureUnit() != null) {
            record.setMeasureUnit(req.measureUnit());
        }
        if (req.measureValue() != null) {
            record.setMeasureValue(req.measureValue());
        }
        if (req.valueSpent() != null) {
            record.setValueSpent(req.valueSpent());
        }
        if (req.actionDate() != null) {
            record.setActionDate(req.actionDate());
        }
        if (req.active() != null) {
            record.setActive(req.active());
        }
        // Update crop if cropId is provided
        if (req.cropId() != null) {
            Optional<Crop> cropOpt = cropRepository.findById(req.cropId());
            cropOpt.ifPresent(record::setCrop);
        }
        record.setUpdatedAt(LocalDateTime.now());
        CropsManagement updated = cropsManagementRepository.save(record);
        return new GlobalResponseHandler().handleResponse("Record updated", updated, HttpStatus.OK, request);
    }

    /**
     * Elimina un registro de manejo agrícola para una parcela.
     */
    @DeleteMapping("/{recordId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteManagementRecord(@PathVariable Long plotId, @PathVariable Long recordId, HttpServletRequest request) {
        if (!hasAccessToPlot(plotId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to plot " + plotId, HttpStatus.FORBIDDEN, request);
        }
        if (!cropsManagementRepository.existsById(recordId)) {
            return new GlobalResponseHandler().handleResponse("Record not found", HttpStatus.NOT_FOUND, request);
        }
        cropsManagementRepository.deleteById(recordId);
        return new GlobalResponseHandler().handleResponse("Record deleted", null, HttpStatus.NO_CONTENT, request);
    }

    /**
     * Método privado para verificar si el usuario autenticado tiene acceso a una parcela.
     * @param plotId El ID de la parcela a verificar.
     * @return true si tiene acceso, false de lo contrario.
     */
    private boolean hasAccessToPlot(Long plotId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }
        Optional<FarmPlot> plot = farmPlotRepository.findById(plotId);
        if (plot.isEmpty()) return false;
        Long farmId = plot.get().getFarm().getId();
        return userXFarmRepository.existsById(new UserFarmId(farmId, currentUser.getId()));
    }

    /**
     * DTO para crear registro de manejo agrícola
     */

    public record CreateManagementRecordRequest(
        Long cropId,
        String actionName,
        String actionPictureUrl,
        String measureUnit,
        Double measureValue,
        Double valueSpent,
        java.time.LocalDateTime actionDate
    ) {}

    public record UpdateManagementRecordRequest(
        Long cropId,
        String actionName,
        String actionPictureUrl,
        String measureUnit,
        Double measureValue,
        Double valueSpent,
        java.time.LocalDateTime actionDate,
        Boolean active
    ) {}
}