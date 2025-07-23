package com.project.demo.rest.farm;

import com.project.demo.logic.entity.equipment.EquipmentMachinery;
import com.project.demo.logic.entity.equipment.EquipmentMachineryRepository;
import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.farm.FarmRepository;
import com.project.demo.logic.entity.farmequipment.FarmEquipment;
import com.project.demo.logic.entity.farmequipment.FarmEquipmentId;
import com.project.demo.logic.entity.farmequipment.FarmEquipmentRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.userfarm.UserFarmId;
import com.project.demo.logic.entity.userfarm.UserXFarmRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DTO (Data Transfer Object) para la solicitud de un nuevo registro de mantenimiento.
 * Usar un record define un contrato claro y seguro para la API.
 */
record AddMaintenanceLogRequest(Long equipmentId, String maintenanceType, String maintenanceLog, Double maintenanceCost) {}

/**
 * Controlador REST para gestionar los registros de mantenimiento de equipos en una granja.
 * Se trata como un "log" o bitácora, por lo que no se implementa PUT ni DELETE.
 */
@RestController
@RequestMapping("/farms/{farmId}/equipment-logs")
public class FarmEquipmentRestController {

    @Autowired
    private FarmEquipmentRepository farmEquipmentRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private EquipmentMachineryRepository equipmentRepository;

    @Autowired
    private UserXFarmRepository userXFarmRepository;

    /**
     * Añade un nuevo registro de mantenimiento para un equipo en una granja.
     * @param farmId El ID de la granja.
     * @param logRequest La solicitud con los detalles del mantenimiento.
     * @return El registro de mantenimiento creado.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addMaintenanceLog(@PathVariable Long farmId, @RequestBody AddMaintenanceLogRequest logRequest, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<Farm> farm = farmRepository.findById(farmId);
        Optional<EquipmentMachinery> equipment = equipmentRepository.findById(logRequest.equipmentId());

        if (farm.isEmpty() || equipment.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Farm or Equipment not found", HttpStatus.NOT_FOUND, request);
        }

        FarmEquipment newLog = new FarmEquipment();
        newLog.setId(new FarmEquipmentId(logRequest.equipmentId(), farmId, LocalDateTime.now()));
        newLog.setFarm(farm.get());
        newLog.setEquipment(equipment.get());
        newLog.setMaintenanceType(logRequest.maintenanceType());
        newLog.setMaintenanceLog(logRequest.maintenanceLog());
        newLog.setMaintenanceCost(logRequest.maintenanceCost());
        newLog.setActive(true);

        FarmEquipment savedLog = farmEquipmentRepository.save(newLog);
        return new GlobalResponseHandler().handleResponse("Maintenance log added successfully", savedLog, HttpStatus.CREATED, request);
    }

    /**
     * Obtiene todos los registros de mantenimiento para una granja.
     * @param farmId El ID de la granja.
     * @return Una lista de registros de mantenimiento.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMaintenanceLogsForFarm(@PathVariable Long farmId, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        List<FarmEquipment> logs = farmEquipmentRepository.findById_FarmId(farmId);
        return new GlobalResponseHandler().handleResponse("Maintenance logs retrieved successfully", logs, HttpStatus.OK, request);
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