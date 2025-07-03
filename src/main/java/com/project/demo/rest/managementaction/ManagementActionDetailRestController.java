package com.project.demo.rest.managementaction;

import com.project.demo.logic.entity.managementaction.ManagementActionDetail;
import com.project.demo.logic.entity.managementaction.ManagementActionDetailRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controlador REST para gestionar los detalles de las acciones de manejo.
 * Estos son los registros transaccionales, como "Se usaron 50kg de Fertilizante X".
 */
@RestController
@RequestMapping("/management-action-details")
public class ManagementActionDetailRestController {

    @Autowired
    private ManagementActionDetailRepository detailRepository;

    /**
     * Crea un nuevo registro de detalle de acción.
     * @param detail El detalle a crear.
     * @return El detalle guardado.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'CORPORATION', 'ADMINISTRADOR')")
    public ResponseEntity<?> createDetail(@RequestBody ManagementActionDetail detail, HttpServletRequest request) {
        ManagementActionDetail savedDetail = detailRepository.save(detail);
        return new GlobalResponseHandler().handleResponse("Action detail created successfully", savedDetail, HttpStatus.CREATED, request);
    }

    /**
     * Obtiene un detalle de acción por su ID.
     * @param id El ID del detalle.
     * @return El detalle encontrado.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDetailById(@PathVariable Long id, HttpServletRequest request) {
        Optional<ManagementActionDetail> detail = detailRepository.findById(id);
        if (detail.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Action detail retrieved successfully", detail.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Action detail id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Actualiza un detalle de acción existente.
     * @param id El ID del detalle a actualizar.
     * @param detailDetails Los nuevos datos.
     * @return El detalle actualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'CORPORATION', 'ADMINISTRADOR')")
    public ResponseEntity<?> updateDetail(@PathVariable Long id, @RequestBody ManagementActionDetail detailDetails, HttpServletRequest request) {
        Optional<ManagementActionDetail> optionalDetail = detailRepository.findById(id);
        if (optionalDetail.isPresent()) {
            ManagementActionDetail existingDetail = optionalDetail.get();
            existingDetail.setMeasureUnit(detailDetails.getMeasureUnit());
            existingDetail.setMeasureValue(detailDetails.getMeasureValue());
            existingDetail.setValueSpent(detailDetails.getValueSpent());
            existingDetail.setActionDate(detailDetails.getActionDate());

            ManagementActionDetail updatedDetail = detailRepository.save(existingDetail);
            return new GlobalResponseHandler().handleResponse("Action detail updated successfully", updatedDetail, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Action detail id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina un detalle de acción.
     * @param id El ID del detalle a eliminar.
     * @return Una confirmación de la eliminación.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'CORPORATION', 'ADMINISTRADOR')")
    public ResponseEntity<?> deleteDetail(@PathVariable Long id, HttpServletRequest request) {
        Optional<ManagementActionDetail> optionalDetail = detailRepository.findById(id);
        if (optionalDetail.isPresent()) {
            detailRepository.delete(optionalDetail.get());
            return new GlobalResponseHandler().handleResponse("Action detail deleted successfully", optionalDetail.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Action detail id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }
}