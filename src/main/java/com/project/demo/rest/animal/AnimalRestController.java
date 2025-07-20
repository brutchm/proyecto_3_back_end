package com.project.demo.rest.animal;

import com.project.demo.logic.entity.animal.Animal;
import com.project.demo.logic.entity.animal.AnimalRepository;
import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.farm.FarmRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.userfarm.UserFarmId;
import com.project.demo.logic.entity.userfarm.UserXFarmRepository;
import com.project.demo.logic.entity.animal.AnimalGroupRepository;
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
 * Controlador REST para gestionar los animales (Animal) como un sub-recurso de una granja.
 */
@RestController
@RequestMapping("/farms/{farmId}/animals")
public class AnimalRestController {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private UserXFarmRepository userXFarmRepository;
    @Autowired
    private AnimalGroupRepository animalGroupRepository;

    /**
     * Crea un nuevo registro de animal dentro de una granja específica.
     * @param farmId El ID de la granja padre.
     * @param animal El animal a crear.
     * @return El animal creado.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createAnimal(@PathVariable Long farmId, @RequestBody Animal animal, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<Farm> farm = farmRepository.findById(farmId);
        if (farm.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Farm with id " + farmId + " not found", HttpStatus.NOT_FOUND, request);
        }

        animal.setFarm(farm.get());
        // Asignar el usuario actual que está registrando el animal
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        animal.setUser(currentUser);

        Animal savedAnimal = animalRepository.save(animal);
        return new GlobalResponseHandler().handleResponse("Animal created successfully", savedAnimal, HttpStatus.CREATED, request);
    }

    /**
     * Obtiene todos los registros de animales de una granja específica.
     * @param farmId El ID de la granja padre.
     * @return Una lista de animales.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAnimalsByFarm(@PathVariable Long farmId, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        List<Animal> animals = animalRepository.findByFarmId(farmId);
        return new GlobalResponseHandler().handleResponse("Animals for farm " + farmId + " retrieved successfully", animals, HttpStatus.OK, request);
    }

    /**
     * Obtiene un animal específico por su ID, dentro de una granja.
     * @param farmId El ID de la granja padre.
     * @param animalId El ID del animal.
     * @return El animal encontrado.
     */
    @GetMapping("/{animalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAnimalById(@PathVariable Long farmId, @PathVariable Long animalId, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<Animal> animal = animalRepository.findByIdAndFarmId(animalId, farmId);
        if (animal.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Animal retrieved successfully", animal.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Animal with id " + animalId + " not found in farm " + farmId, HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Actualiza un registro de animal existente.
     * @param farmId El ID de la granja padre.
     * @param animalId El ID del animal a actualizar.
     * @param animalDetails Los nuevos datos para el animal.
     * @return El animal actualizado.
     */
    @PutMapping("/{animalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateAnimal(@PathVariable Long farmId, @PathVariable Long animalId, @RequestBody Animal animalDetails, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<Animal> optionalAnimal = animalRepository.findByIdAndFarmId(animalId, farmId);
        if (optionalAnimal.isPresent()) {
            Animal existingAnimal = optionalAnimal.get();
            existingAnimal.setSpecies(animalDetails.getSpecies());
            existingAnimal.setBreed(animalDetails.getBreed());
            existingAnimal.setCount(animalDetails.getCount());
            existingAnimal.setActive(animalDetails.isActive());
            Animal updatedAnimal = animalRepository.save(existingAnimal);
            return new GlobalResponseHandler().handleResponse("Animal updated successfully", updatedAnimal, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Animal with id " + animalId + " not found in farm " + farmId, HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina un registro de animal.
     * @param farmId El ID de la granja padre.
     * @param animalId El ID del animal a eliminar.
     * @return Confirmación de la eliminación.
     */
    @DeleteMapping("/{animalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteAnimal(@PathVariable Long farmId, @PathVariable Long animalId, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<Animal> optionalAnimal = animalRepository.findByIdAndFarmId(animalId, farmId);
        if (optionalAnimal.isPresent()) {
            animalRepository.delete(optionalAnimal.get());
            return new GlobalResponseHandler().handleResponse("Animal deleted successfully", optionalAnimal.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Animal with id " + animalId + " not found in farm " + farmId, HttpStatus.NOT_FOUND, request);
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