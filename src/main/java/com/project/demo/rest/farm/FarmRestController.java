package com.project.demo.rest.farm;

import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.farm.FarmRepository;
import com.project.demo.logic.entity.farm.FarmsTechnicalInformation;
import com.project.demo.logic.entity.farm.FarmsTechnicalInformationRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

record FarmCreationRequest(Farm farm, FarmsTechnicalInformation technicalInfo) {}

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

    @Autowired
    private FarmsTechnicalInformationRepository farmsTechnicalInformationRepository;

    /**
     * Crea una nueva granja y la asigna automáticamente al usuario autenticado.
     * Si se envía información técnica, también la guarda.
     * @param creationRequest Objeto que contiene la granja y su información técnica.
     * @return La granja recién creada con su información técnica.
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasAnyRole('USER', 'CORPORATION')")
    public ResponseEntity<?> createFarm(@RequestBody FarmCreationRequest creationRequest, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Farm farm = creationRequest.farm();
        FarmsTechnicalInformation technicalInfo = creationRequest.technicalInfo();

        // Save the farm
        Farm savedFarm = farmRepository.save(farm);

        // Link farm to the current user
        UserXFarm userFarmLink = new UserXFarm();
        userFarmLink.setId(new UserFarmId(savedFarm.getId(), currentUser.getId()));
        userFarmLink.setFarm(savedFarm);
        userFarmLink.setUser(currentUser);
        userFarmLink.setActive(true);
        userXFarmRepository.save(userFarmLink);

        FarmsTechnicalInformation savedTechnicalInfo = null;
        // Save technical information if provided
        if (technicalInfo != null) {
            technicalInfo.setFarm(savedFarm);
            technicalInfo.setIsActive(true); // Assuming it should be active by default
            savedTechnicalInfo = farmsTechnicalInformationRepository.save(technicalInfo);
        }

        // Return both farm and technical info in the response
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("farm", savedFarm);
        responseBody.put("technicalInfo", savedTechnicalInfo);
        return new GlobalResponseHandler().handleResponse("Farm created and assigned successfully", responseBody, HttpStatus.CREATED, request);
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
            List<Farm> allFarms = farmRepository.findAll();
            return new GlobalResponseHandler().handleResponse("All farms retrieved successfully", allFarms, HttpStatus.OK, request);
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
            Farm farm = farmOptional.get();
            Optional<FarmsTechnicalInformation> techInfoOptional = farmsTechnicalInformationRepository.findByFarmId(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("farm", farm);
            responseBody.put("technicalInfo", techInfoOptional.orElse(null));

            return new GlobalResponseHandler().handleResponse("Farm retrieved successfully", responseBody, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Farm id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Actualiza una granja existente, validando los permisos del usuario.
     * @param id El ID de la granja a actualizar.
     * @param request Los nuevos datos para la granja.
     * @return La granja actualizada.
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateFarm(@PathVariable Long id, @RequestBody FarmCreationRequest updateRequest, HttpServletRequest request) {
        if (!hasAccessToFarm(id)) {
            return new GlobalResponseHandler().handleResponse("User does not have access to this farm", HttpStatus.FORBIDDEN, request);
        }

        Optional<Farm> farmOptional = farmRepository.findById(id);
        if (farmOptional.isPresent()) {
            Farm existingFarm = farmOptional.get();
            Farm farmDetails = updateRequest.farm();

            // Update farm details
            existingFarm.setFarmName(farmDetails.getFarmName());
            existingFarm.setFarmCountry(farmDetails.getFarmCountry());
            existingFarm.setFarmStateProvince(farmDetails.getFarmStateProvince());
            existingFarm.setFarmOtherDirections(farmDetails.getFarmOtherDirections());
            existingFarm.setFarmLocation(farmDetails.getFarmLocation());
            existingFarm.setFarmSize(farmDetails.getFarmSize());
            existingFarm.setFarmMeasureUnit(farmDetails.getFarmMeasureUnit());
            existingFarm.setActive(farmDetails.isActive());
            Farm updatedFarm = farmRepository.save(existingFarm);

            // Update technical information
            FarmsTechnicalInformation technicalInfoDetails = updateRequest.technicalInfo();
            FarmsTechnicalInformation savedTechnicalInfo = null;
            if (technicalInfoDetails != null) {
                Optional<FarmsTechnicalInformation> techInfoOptional = farmsTechnicalInformationRepository.findByFarmId(id);
                FarmsTechnicalInformation existingTechInfo = techInfoOptional.orElse(new FarmsTechnicalInformation());
                existingTechInfo.setFarm(updatedFarm);
                existingTechInfo.setSoilPh(technicalInfoDetails.getSoilPh());
                existingTechInfo.setSoilNutrients(technicalInfoDetails.getSoilNutrients());
                existingTechInfo.setIrrigationSystem(technicalInfoDetails.getIrrigationSystem());
                existingTechInfo.setIrrigationSystemType(technicalInfoDetails.getIrrigationSystemType());
                existingTechInfo.setWaterAvailable(technicalInfoDetails.getWaterAvailable());
                existingTechInfo.setWaterUsageType(technicalInfoDetails.getWaterUsageType());
                existingTechInfo.setFertilizerPesticideUse(technicalInfoDetails.getFertilizerPesticideUse());
                existingTechInfo.setIsActive(technicalInfoDetails.getIsActive());
                savedTechnicalInfo = farmsTechnicalInformationRepository.save(existingTechInfo);
            }

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("farm", updatedFarm);
            responseBody.put("technicalInfo", savedTechnicalInfo);

            return new GlobalResponseHandler().handleResponse("Farm updated successfully", responseBody, HttpStatus.OK, request);
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
            farmRepository.delete(farmOptional.get());
            return new GlobalResponseHandler().handleResponse("Farm and all associated data deleted successfully", farmOptional.get(), HttpStatus.OK, request);
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

    /**
     * Obtiene todas las fincas asociadas al usuario autenticado (farmAdmin).
     */
    @GetMapping("/my-farms")
    @PreAuthorize("hasAnyRole('USER', 'CORPORATION', 'FARM_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getMyFarms(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            List<Farm> farms = farmRepository.findFarmsByUserId(currentUser.getId());
            List<Map<String, Object>> result = farms.stream().map(farm -> {
                Optional<FarmsTechnicalInformation> techInfoOptional = farmsTechnicalInformationRepository.findByFarmId(farm.getId());
                Map<String, Object> farmData = new HashMap<>();
                farmData.put("farm", farm);
                farmData.put("technicalInfo", techInfoOptional.orElse(null));
                return farmData;
            }).toList();
            return new GlobalResponseHandler().handleResponse("Farms retrieved successfully", result, HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse("Error retrieving farms: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR, request);
        }
    }
}