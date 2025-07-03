package com.project.demo.rest.managementaction;

import com.project.demo.logic.entity.managementaction.ManagementAction;
import com.project.demo.logic.entity.managementaction.ManagementActionRepository;
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
 * Controlador REST para la gestión completa (CRUD) del catálogo de Acciones de Manejo.
 * Estas son acciones maestras como "Siembra", "Cosecha", "Fertilización", etc.
 */
@RestController
@RequestMapping("/management-actions")
public class ManagementActionRestController {

    @Autowired
    private ManagementActionRepository managementActionRepository;

    /**
     * Crea una nueva acción de manejo en el catálogo del sistema.
     * Solo el SUPER_ADMIN puede definir qué tipos de acciones existen.
     * @param action La acción a crear.
     * @return La acción de manejo creada.
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> addAction(@RequestBody ManagementAction action, HttpServletRequest request) {
        ManagementAction savedAction = managementActionRepository.save(action);
        return new GlobalResponseHandler().handleResponse("Action created successfully", savedAction, HttpStatus.CREATED, request);
    }

    /**
     * Obtiene una lista paginada de todas las acciones de manejo disponibles.
     * @return Una respuesta con la lista de acciones y metadatos de paginación.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllActions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ManagementAction> actionPage = managementActionRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(actionPage.getTotalPages());
        meta.setTotalElements(actionPage.getTotalElements());
        meta.setPageNumber(actionPage.getNumber() + 1);
        meta.setPageSize(actionPage.getSize());
        return new GlobalResponseHandler().handleResponse("Management actions retrieved successfully", actionPage.getContent(), HttpStatus.OK, meta);
    }

    /**
     * Obtiene una acción de manejo específica por su ID.
     * @param id El ID de la acción.
     * @return La acción encontrada o un error 404.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getActionById(@PathVariable Long id, HttpServletRequest request) {
        Optional<ManagementAction> action = managementActionRepository.findById(id);
        if (action.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Action retrieved successfully", action.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Action id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Actualiza una acción de manejo existente en el catálogo.
     * Solo el SUPER_ADMIN puede modificarla.
     * @param id El ID de la acción a actualizar.
     * @param actionDetails Los nuevos datos para la acción.
     * @return La acción actualizada.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateAction(@PathVariable Long id, @RequestBody ManagementAction actionDetails, HttpServletRequest request) {
        Optional<ManagementAction> optionalAction = managementActionRepository.findById(id);
        if (optionalAction.isPresent()) {
            ManagementAction existingAction = optionalAction.get();
            existingAction.setActionName(actionDetails.getActionName());
            existingAction.setActionPictureUrl(actionDetails.getActionPictureUrl());
            existingAction.setActive(actionDetails.isActive());
            ManagementAction updatedAction = managementActionRepository.save(existingAction);
            return new GlobalResponseHandler().handleResponse("Action updated successfully", updatedAction, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Action id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina una acción de manejo del catálogo.
     * Solo el SUPER_ADMIN puede eliminarla.
     * @param id El ID de la acción a eliminar.
     * @return Una confirmación de la eliminación.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteAction(@PathVariable Long id, HttpServletRequest request) {
        Optional<ManagementAction> optionalAction = managementActionRepository.findById(id);
        if (optionalAction.isPresent()) {
            managementActionRepository.delete(optionalAction.get());
            return new GlobalResponseHandler().handleResponse("Action deleted successfully", optionalAction.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Action id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }
}