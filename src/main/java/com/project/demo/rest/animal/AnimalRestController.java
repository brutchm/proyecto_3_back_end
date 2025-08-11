package com.project.demo.rest.animal;

import com.project.demo.logic.entity.animal.Animal;
import com.project.demo.logic.entity.animal.AnimalRepository;
import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.farm.FarmRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.userfarm.UserFarmId;
import com.project.demo.logic.entity.userfarm.UserXFarmRepository;
import com.project.demo.logic.entity.animal.AnimalGroupRepository;
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
            return new GlobalResponseHandler().handleResponse("Acceso denegado a la Finca " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<Farm> farm = farmRepository.findById(farmId);
        if (farm.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Finca con código " + farmId + " no fue encontrada", HttpStatus.NOT_FOUND, request);
        }

        animal.setFarm(farm.get());
        // Asignar el usuario actual que está registrando el animal
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return new GlobalResponseHandler().handleResponse("No hay usuario autenticado", HttpStatus.UNAUTHORIZED, request);
        }
        User currentUser = (User) authentication.getPrincipal();
        animal.setUser(currentUser);

        Animal savedAnimal = animalRepository.save(animal);
        return new GlobalResponseHandler().handleResponse("Animal creado correctamente", savedAnimal, HttpStatus.CREATED, request);
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
            return new GlobalResponseHandler().handleResponse("Acceso denegado a la Finca " + farmId, HttpStatus.FORBIDDEN, request);
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
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }
        User currentUser = (User) authentication.getPrincipal();
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }
        return userXFarmRepository.existsById(new UserFarmId(farmId, currentUser.getId()));
    }

    /**
     * Obtiene la lista de animales que pertenecen a un grupo específico dentro de una finca.
     *
     * Este endpoint requiere autenticación previa. Se valida si el usuario tiene acceso a la finca indicada.
     * Si no tiene acceso, se devuelve un error 403. Si tiene acceso, se retornan todos los animales asociados
     * al grupo especificado dentro de esa finca.
     *
     * @param farmId ID de la finca a la que pertenece el grupo.
     * @param groupId ID del grupo del cual se desean listar los animales.
     * @param request Objeto HttpServletRequest utilizado para el manejo de la respuesta global.
     * @return ResponseEntity con un mensaje de éxito y la lista de animales del grupo si se tiene acceso,
     *         o un mensaje de error si el acceso es denegado.
     */
    @GetMapping("/group/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAnimalsByGroup(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable Long farmId,
            @PathVariable Long groupId,
            HttpServletRequest request
    ) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Acceso denegado a la Finca " + farmId, HttpStatus.FORBIDDEN, request);
        }

        //List<Animal> animals = animalRepository.findByFarmIdAndAnimalGroupId(farmId, groupId);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Animal> animals = animalRepository.findByFarmIdAndAnimalGroupId(farmId, groupId,pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(animals.getTotalPages());
        meta.setTotalElements(animals.getTotalElements());
        meta.setPageNumber(animals.getNumber() + 1);
        meta.setPageSize(animals.getSize());

        return new GlobalResponseHandler().handleResponse(
                "Animales del grupo " + groupId + " recuperados con éxito",
                animals.getContent(),
                HttpStatus.OK,
                meta
        );


    }


}