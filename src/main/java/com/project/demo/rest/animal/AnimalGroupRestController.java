package com.project.demo.rest.animal;

import com.project.demo.logic.entity.animal.AnimalGroup;
import com.project.demo.logic.entity.animal.AnimalGroupRepository;
import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.farm.FarmRepository;
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

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gestionar los grupos de animales (Animal Groups) como un sub-recurso de una granja.
 */
@RestController
@RequestMapping("/farms/{farmId}/animal-groups")
public class AnimalGroupRestController {

    @Autowired
    private AnimalGroupRepository animalGroupRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private UserXFarmRepository userXFarmRepository;

    /**
     * Crea un nuevo grupo de animales dentro de una granja específica.
     * @param farmId El ID de la granja padre.
     * @param group El grupo de animales a crear.
     * @return El grupo creado.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createAnimalGroup(@PathVariable Long farmId, @RequestBody AnimalGroup group, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<Farm> farm = farmRepository.findById(farmId);
        if (farm.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Farm with id " + farmId + " not found", HttpStatus.NOT_FOUND, request);
        }

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        group.setFarm(farm.get());
        group.setUser(currentUser);

        AnimalGroup savedGroup = animalGroupRepository.save(group);
        return new GlobalResponseHandler().handleResponse("Animal group created successfully", savedGroup, HttpStatus.CREATED, request);
    }

    /**
     * Obtiene todos los grupos de animales de una granja específica.
     * @param farmId El ID de la granja padre.
     * @return Una lista de grupos de animales.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAnimalGroupsByFarm(@PathVariable Long farmId, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        List<AnimalGroup> groups = animalGroupRepository.findByFarmId(farmId);
        return new GlobalResponseHandler().handleResponse("Animal groups for farm " + farmId + " retrieved successfully", groups, HttpStatus.OK, request);
    }

    /**
     * Obtiene un grupo de animales específico por su ID, dentro de una granja.
     * @param farmId El ID de la granja padre.
     * @param groupId El ID del grupo.
     * @return El grupo encontrado.
     */
    @GetMapping("/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAnimalGroupById(@PathVariable Long farmId, @PathVariable Long groupId, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<AnimalGroup> group = animalGroupRepository.findByIdAndFarmId(groupId, farmId);
        if (group.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Animal group retrieved successfully", group.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Animal group with id " + groupId + " not found in farm " + farmId, HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Actualiza un grupo de animales existente.
     * @param farmId El ID de la granja padre.
     * @param groupId El ID del grupo a actualizar.
     * @param groupDetails Los nuevos datos para el grupo.
     * @return El grupo actualizado.
     */
    @PutMapping("/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateAnimalGroup(@PathVariable Long farmId, @PathVariable Long groupId, @RequestBody AnimalGroup groupDetails, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<AnimalGroup> optionalGroup = animalGroupRepository.findByIdAndFarmId(groupId, farmId);
        if (optionalGroup.isPresent()) {
            AnimalGroup existingGroup = optionalGroup.get();
            existingGroup.setGroupName(groupDetails.getGroupName());
            existingGroup.setSpecies(groupDetails.getSpecies());
            existingGroup.setCount(groupDetails.getCount());
            existingGroup.setMeasure(groupDetails.getMeasure());
            existingGroup.setProductionType(groupDetails.getProductionType());
            existingGroup.setActive(groupDetails.isActive());
            AnimalGroup updatedGroup = animalGroupRepository.save(existingGroup);
            return new GlobalResponseHandler().handleResponse("Animal group updated successfully", updatedGroup, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Animal group with id " + groupId + " not found in farm " + farmId, HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina un grupo de animales.
     * @param farmId El ID de la granja padre.
     * @param groupId El ID del grupo a eliminar.
     * @return Confirmación de la eliminación.
     */
    @DeleteMapping("/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteAnimalGroup(@PathVariable Long farmId, @PathVariable Long groupId, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<AnimalGroup> optionalGroup = animalGroupRepository.findByIdAndFarmId(groupId, farmId);
        if (optionalGroup.isPresent()) {
            animalGroupRepository.delete(optionalGroup.get());
            return new GlobalResponseHandler().handleResponse("Animal group deleted successfully", optionalGroup.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Animal group with id " + groupId + " not found in farm " + farmId, HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Método de ayuda para verificar el acceso del usuario a la granja.
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