package com.project.demo.rest.crop;

import com.project.demo.logic.entity.crop.Crop;
import com.project.demo.logic.entity.crop.CropRepository;
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
 * Controlador REST para gestionar las operaciones CRUD de los cultivos (Crops).
 */
@RestController
@RequestMapping("/crops")
public class CropRestController {

    @Autowired
    private CropRepository cropRepository;

    /**
     * Obtiene una lista paginada de todos los cultivos.
     * @param page Número de la página a obtener (por defecto es 1).
     * @param size Tamaño de la página (por defecto es 10).
     * @param request La solicitud HTTP.
     * @return Una respuesta con la lista de cultivos y metadatos de paginación.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllCrops(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Crop> cropPage = cropRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(cropPage.getTotalPages());
        meta.setTotalElements(cropPage.getTotalElements());
        meta.setPageNumber(cropPage.getNumber() + 1);
        meta.setPageSize(cropPage.getSize());

        return new GlobalResponseHandler().handleResponse("Crops retrieved successfully",
                cropPage.getContent(), HttpStatus.OK, meta);
    }

    /**
     * Obtiene un cultivo específico por su ID.
     * @param id El ID del cultivo a buscar.
     * @param request La solicitud HTTP.
     * @return El cultivo encontrado o un error 404 si no existe.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCropById(@PathVariable Long id, HttpServletRequest request) {
        Optional<Crop> crop = cropRepository.findById(id);
        if (crop.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Crop retrieved successfully", crop.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Crop id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Crea un nuevo cultivo en el sistema.
     * Acceso restringido a SUPER_ADMIN.
     * @param crop El objeto Crop a crear.
     * @param request La solicitud HTTP.
     * @return El cultivo creado.
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> addCrop(@RequestBody Crop crop, HttpServletRequest request) {
        Crop savedCrop = cropRepository.save(crop);
        return new GlobalResponseHandler().handleResponse("Crop created successfully", savedCrop, HttpStatus.CREATED, request);
    }

    /**
     * Actualiza un cultivo existente.
     * Acceso restringido a SUPER_ADMIN.
     * @param id El ID del cultivo a actualizar.
     * @param cropDetails Los nuevos detalles del cultivo.
     * @param request La solicitud HTTP.
     * @return El cultivo actualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateCrop(@PathVariable Long id, @RequestBody Crop cropDetails, HttpServletRequest request) {
        Optional<Crop> cropOptional = cropRepository.findById(id);
        if (cropOptional.isPresent()) {
            Crop existingCrop = cropOptional.get();
            existingCrop.setCropName(cropDetails.getCropName());
            existingCrop.setCropType(cropDetails.getCropType());
            existingCrop.setCropVariety(cropDetails.getCropVariety());
            existingCrop.setCropPicture(cropDetails.getCropPicture());
            existingCrop.setActive(cropDetails.isActive());
            Crop updatedCrop = cropRepository.save(existingCrop);
            return new GlobalResponseHandler().handleResponse("Crop updated successfully", updatedCrop, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Crop id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina un cultivo del sistema.
     * Acceso restringido a SUPER_ADMIN.
     * @param id El ID del cultivo a eliminar.
     * @param request La solicitud HTTP.
     * @return Una confirmación de la eliminación.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteCrop(@PathVariable Long id, HttpServletRequest request) {
        Optional<Crop> cropOptional = cropRepository.findById(id);
        if (cropOptional.isPresent()) {
            Crop crop = cropOptional.get();
            cropRepository.delete(crop);
            return new GlobalResponseHandler().handleResponse("Crop deleted successfully", crop, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Crop id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }
}