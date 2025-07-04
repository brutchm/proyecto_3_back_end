package com.project.demo.rest.farm;

import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.farm.FarmRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.userfarm.UserFarmId;
import com.project.demo.logic.entity.userfarm.UserXFarm;
import com.project.demo.logic.entity.userfarm.UserXFarmRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gestionar las operaciones CRUD de las granjas (Farms).
 * Implementa lógica de negocio para la propiedad y el acceso a las granjas.
 */
@RestController
@RequestMapping("/farms")
public class FarmRestController {

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private UserXFarmRepository userXFarmRepository;

    /**
     * Crea una nueva granja y la asigna automáticamente al usuario autenticado.
     * @param farm La información de la granja a crear.
     * @return La granja recién creada.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'CORPORATION')")
    public ResponseEntity<?> createFarm(@RequestBody Farm farm, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Farm savedFarm = farmRepository.save(farm);

        UserXFarm userFarmLink = new UserXFarm();
        userFarmLink.setId(new UserFarmId(savedFarm.getId(), currentUser.getId()));
        userFarmLink.setFarm(savedFarm);
        userFarmLink.setUser(currentUser);
        userFarmLink.setActive(true);
        userXFarmRepository.save(userFarmLink);

        return new GlobalResponseHandler().handleResponse("Farm created and assigned successfully", savedFarm, HttpStatus.CREATED, request);
    }

    /**
     * Obtiene una lista de granjas.
     * - Si el usuario es SUPER_ADMIN, devuelve todas las granjas de forma paginada.
     * - Si es otro rol, devuelve una lista de las granjas asociadas a ese usuario (sin paginar).
     * @return Lista de granjas.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFarms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<Farm> farmPage = farmRepository.findAll(pageable);
            Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
            meta.setTotalPages(farmPage.getTotalPages());
            meta.setTotalElements(farmPage.getTotalElements());
            return new GlobalResponseHandler().handleResponse("All farms retrieved successfully", farmPage.getContent(), HttpStatus.OK, meta);
        } else {
            List<Farm> userFarms = farmRepository.findFarmsByUserId(currentUser.getId());
            return new GlobalResponseHandler().handleResponse("User farms retrieved successfully", userFarms, HttpStatus.OK, request);
        }
    }

    /**
     * Obtiene una granja específica por su ID, validando los permisos del usuario.
     * @param id El ID de la granja a obtener.
     * @return La granja si se encuentra y se tienen permisos.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFarmById(@PathVariable Long id, HttpServletRequest request) {
        if (!hasAccessToFarm(id)) {
            return new GlobalResponseHandler().handleResponse("User does not have access to this farm", HttpStatus.FORBIDDEN, request);
        }

        Optional<Farm> farmOptional = farmRepository.findById(id);
        if (farmOptional.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Farm retrieved successfully", farmOptional.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Farm id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Actualiza una granja existente, validando los permisos del usuario.
     * @param id El ID de la granja a actualizar.
     * @param farmDetails Los nuevos datos para la granja.
     * @return La granja actualizada.
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateFarm(@PathVariable Long id, @RequestBody Farm farmDetails, HttpServletRequest request) {
        if (!hasAccessToFarm(id)) {
            return new GlobalResponseHandler().handleResponse("User does not have access to this farm", HttpStatus.FORBIDDEN, request);
        }

        Optional<Farm> farmOptional = farmRepository.findById(id);
        if (farmOptional.isPresent()) {
            Farm existingFarm = farmOptional.get();
            existingFarm.setFarmName(farmDetails.getFarmName());
            existingFarm.setFarmCountry(farmDetails.getFarmCountry());
            existingFarm.setFarmStateProvince(farmDetails.getFarmStateProvince());
            existingFarm.setFarmOtherDirections(farmDetails.getFarmOtherDirections());
            existingFarm.setFarmLocation(farmDetails.getFarmLocation());
            existingFarm.setFarmSize(farmDetails.getFarmSize());
            existingFarm.setFarmMeasureUnit(farmDetails.getFarmMeasureUnit());
            existingFarm.setActive(farmDetails.isActive());

            Farm updatedFarm = farmRepository.save(existingFarm);
            return new GlobalResponseHandler().handleResponse("Farm updated successfully", updatedFarm, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Farm id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina una granja, validando los permisos del usuario.
     * @param id El ID de la granja a eliminar.
     * @return Una confirmación de la eliminación.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteFarm(@PathVariable Long id, HttpServletRequest request) {
        if (!hasAccessToFarm(id)) {
            return new GlobalResponseHandler().handleResponse("User does not have access to this farm", HttpStatus.FORBIDDEN, request);
        }

        Optional<Farm> farmOptional = farmRepository.findById(id);
        if (farmOptional.isPresent()) {
            // se deberían eliminar también las relaciones en user_x_farm.
            farmRepository.delete(farmOptional.get());
            return new GlobalResponseHandler().handleResponse("Farm deleted successfully", farmOptional.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Farm id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Método privado para verificar si el usuario autenticado tiene acceso a una granja.
     * @param farmId El ID de la granja a verificar.
     * @return true si tiene acceso, false de lo contrario.
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