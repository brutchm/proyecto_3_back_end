package com.project.demo.rest.crop;

import com.project.demo.logic.entity.crop.Crop;
import com.project.demo.logic.entity.crop.CropRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.user.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @class CropRestController
 * @description
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
    @PreAuthorize("hasRole('SUPER_ADMIN')")
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
     * Obtiene una lista paginada de los cultivos del usuario actual.
     * @param page Número de la página a obtener (por defecto es 1).
     * @param size Tamaño de la página (por defecto es 10).
     * @param currentUser El usuario actualmente autenticado.
     * @param request La solicitud HTTP.
     * @return Una respuesta con la lista de cultivos y metadatos de paginación.
     */
    @GetMapping("/my-crops")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyCrops(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Crop> cropPage = cropRepository.findAllByUserId(currentUser.getId(), pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(cropPage.getTotalPages());
        meta.setTotalElements(cropPage.getTotalElements());
        meta.setPageNumber(cropPage.getNumber() + 1);
        meta.setPageSize(cropPage.getSize());

        return new GlobalResponseHandler().handleResponse("Cultivos obtenidos exitosamente",
                cropPage.getContent(), HttpStatus.OK, meta);
    }

    /**
     * Obtiene un cultivo específico por su ID.
     * @param id El ID del cultivo a buscar.
     * @param currentUser El usuario actualmente autenticado.
     * @param request La solicitud HTTP.
     * @return El cultivo encontrado o un error 404 si no existe.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCropById(@PathVariable Long id, @AuthenticationPrincipal User currentUser, HttpServletRequest request) {
        Optional<Crop> crop = cropRepository.findByIdAndUserId(id, currentUser.getId());
        if (crop.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Cultivo obtenido exitosamente", crop.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Cultivo con id " + id + " no encontrado o acceso denegado", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Crea un nuevo cultivo en el sistema.
     * Acceso restringido a USER.
     * @param crop El objeto Crop a crear.
     * @param currentUser El usuario actualmente autenticado.
     * @param request La solicitud HTTP.
     * @return El cultivo creado.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addCrop(@RequestBody Crop crop, @AuthenticationPrincipal User currentUser, HttpServletRequest request) {
        crop.setUser(currentUser);
        Crop savedCrop = cropRepository.save(crop);
        return new GlobalResponseHandler().handleResponse("Cultivo creado exitosamente", savedCrop, HttpStatus.CREATED, request);
    }

    /**
     * Actualiza un cultivo existente.
     * Acceso restringido a SUPER_ADMIN.
     * @param id El ID del cultivo a actualizar.
     * @param cropDetails Los nuevos detalles del cultivo.
     * @param currentUser El usuario actualmente autenticado.
     * @param request La solicitud HTTP.
     * @return El cultivo actualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateCrop(@PathVariable Long id, @RequestBody Crop cropDetails, @AuthenticationPrincipal User currentUser, HttpServletRequest request) {
        Optional<Crop> cropOptional = cropRepository.findByIdAndUserId(id, currentUser.getId());
        if (cropOptional.isPresent()) {
            Crop existingCrop = cropOptional.get();
            existingCrop.setCropName(cropDetails.getCropName());
            existingCrop.setCropType(cropDetails.getCropType());
            existingCrop.setCropVariety(cropDetails.getCropVariety());
            existingCrop.setCropPicture(cropDetails.getCropPicture());
            existingCrop.setIsActive(cropDetails.getIsActive());
            Crop updatedCrop = cropRepository.save(existingCrop);
            return new GlobalResponseHandler().handleResponse("Cultivo actualizado exitosamente", updatedCrop, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Cultivo con id " + id + " no encontrado o acceso denegado", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina un cultivo del sistema.
     * Acceso restringido a USER.
     * @param id El ID del cultivo a eliminar.
     * @param currentUser El usuario actualmente autenticado.
     * @param request La solicitud HTTP.
     * @return Una confirmación de la eliminación.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteCrop(@PathVariable Long id, @AuthenticationPrincipal User currentUser, HttpServletRequest request) {
        Optional<Crop> cropOptional = cropRepository.findByIdAndUserId(id, currentUser.getId());
        if (cropOptional.isPresent()) {
            Crop crop = cropOptional.get();
            cropRepository.delete(crop);
            return new GlobalResponseHandler().handleResponse("Cultivo desactivado exitosamente", crop, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Cultivo con id " + id + " no encontrado o acceso denegado", HttpStatus.NOT_FOUND, request);
        }
    }


    @GetMapping("/list-price-crops")
    @PreAuthorize("hasRole('CORPORATION')")
    public ResponseEntity<?> getAllCropsMarket(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Long userId = currentUser.getId();
        Page<Crop> cropPage = cropRepository.findAllCrops(userId,pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(cropPage.getTotalPages());
        meta.setTotalElements(cropPage.getTotalElements());
        meta.setPageNumber(cropPage.getNumber() + 1);
        meta.setPageSize(cropPage.getSize());

        return new GlobalResponseHandler().handleResponse("Cultivos obtenidos exitosamente",
                cropPage.getContent(), HttpStatus.OK, meta);
    }
}