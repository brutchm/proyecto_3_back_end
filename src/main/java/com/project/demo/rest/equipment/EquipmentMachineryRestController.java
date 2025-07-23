package com.project.demo.rest.equipment;

import com.project.demo.logic.entity.equipment.EquipmentMachinery;
import com.project.demo.logic.entity.equipment.EquipmentMachineryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controlador REST para gestionar las operaciones CRUD de Equipos y Maquinaria.
 */
@RestController
@RequestMapping("/equipment-machinery")
public class EquipmentMachineryRestController {

    @Autowired
    private EquipmentMachineryRepository equipmentRepository;

    /**
     * Obtiene una lista paginada de todos los equipos.
     * @return Una respuesta con la lista de equipos y metadatos de paginación.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllEquipment(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<EquipmentMachinery> equipmentPage = equipmentRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(equipmentPage.getTotalPages());
        meta.setTotalElements(equipmentPage.getTotalElements());
        meta.setPageNumber(equipmentPage.getNumber() + 1);
        meta.setPageSize(equipmentPage.getSize());

        return new GlobalResponseHandler().handleResponse("Equipment retrieved successfully",
                equipmentPage.getContent(), HttpStatus.OK, meta);
    }

    /**
     * Obtiene un equipo específico por su ID.
     * @return El equipo encontrado o un error 404 si no existe.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getEquipmentById(@PathVariable Long id, HttpServletRequest request) {
        Optional<EquipmentMachinery> equipment = equipmentRepository.findById(id);
        if (equipment.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Equipment retrieved successfully", equipment.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Equipment id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Crea un nuevo equipo. Acceso restringido a SUPER_ADMIN.
     * @return El equipo creado.
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> addEquipment(@RequestBody EquipmentMachinery equipment, HttpServletRequest request) {
        EquipmentMachinery savedEquipment = equipmentRepository.save(equipment);
        return new GlobalResponseHandler().handleResponse("Equipment created successfully", savedEquipment, HttpStatus.CREATED, request);
    }

    /**
     * Actualiza un equipo existente. Acceso restringido a SUPER_ADMIN.
     * @return El equipo actualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateEquipment(@PathVariable Long id, @RequestBody EquipmentMachinery equipmentDetails, HttpServletRequest request) {
        Optional<EquipmentMachinery> optionalEquipment = equipmentRepository.findById(id);
        if (optionalEquipment.isPresent()) {
            EquipmentMachinery existingEquipment = optionalEquipment.get();
            existingEquipment.setEquipmentName(equipmentDetails.getEquipmentName());
            existingEquipment.setEquipmentModel(equipmentDetails.getEquipmentModel());
            existingEquipment.setActive(equipmentDetails.isActive());
            EquipmentMachinery updatedEquipment = equipmentRepository.save(existingEquipment);
            return new GlobalResponseHandler().handleResponse("Equipment updated successfully", updatedEquipment, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Equipment id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina un equipo. Acceso restringido a SUPER_ADMIN.
     * @return Una confirmación de la eliminación.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteEquipment(@PathVariable Long id, HttpServletRequest request) {
        Optional<EquipmentMachinery> optionalEquipment = equipmentRepository.findById(id);
        if (optionalEquipment.isPresent()) {
            equipmentRepository.delete(optionalEquipment.get());
            return new GlobalResponseHandler().handleResponse("Equipment deleted successfully", optionalEquipment.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Equipment id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }
}