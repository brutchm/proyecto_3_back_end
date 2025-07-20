package com.project.demo.rest.animal;

import com.project.demo.logic.entity.animal.AnimalGroup;
import com.project.demo.logic.entity.animal.AnimalGroupRepository;
import com.project.demo.logic.entity.animal.AnimalGroupHistory;
import com.project.demo.logic.entity.animal.AnimalGroupHistoryRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.userfarm.UserFarmId;
import com.project.demo.logic.entity.userfarm.UserXFarmRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
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
 * Controlador para gestionar el historial de un grupo de animales.
 * La operación DELETE se omite para preservar la integridad del historial.
 */
@RestController
@RequestMapping("/animal-groups/{groupId}/history")
public class AnimalGroupHistoryRestController {

    @Autowired
    private AnimalGroupHistoryRepository historyRepository;

    @Autowired
    private AnimalGroupRepository animalGroupRepository;

    @Autowired
    private UserXFarmRepository userXFarmRepository;

    /**
     * Añade un nuevo registro al historial de un grupo de animales.
     * @param groupId El ID del grupo padre.
     * @param historyEntry El registro a crear.
     * @return El registro de historial creado.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addHistory(@PathVariable Long groupId, @RequestBody AnimalGroupHistory historyEntry, HttpServletRequest request) {
        Optional<AnimalGroup> optionalGroup = animalGroupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Animal Group with id " + groupId + " not found", HttpStatus.NOT_FOUND, request);
        }

        if (!hasAccessToFarm(optionalGroup.get().getFarm().getId())) {
            return new GlobalResponseHandler().handleResponse("Access Denied", HttpStatus.FORBIDDEN, request);
        }

        historyEntry.setAnimalGroup(optionalGroup.get());
        AnimalGroupHistory savedHistory = historyRepository.save(historyEntry);
        return new GlobalResponseHandler().handleResponse("History entry created successfully", savedHistory, HttpStatus.CREATED, request);
    }

    /**
     * Obtiene todo el historial de un grupo de animales.
     * @param groupId El ID del grupo padre.
     * @return Una lista de registros de historial.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getHistory(@PathVariable Long groupId, HttpServletRequest request) {
        Optional<AnimalGroup> optionalGroup = animalGroupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Animal Group with id " + groupId + " not found", HttpStatus.NOT_FOUND, request);
        }

        if (!hasAccessToFarm(optionalGroup.get().getFarm().getId())) {
            return new GlobalResponseHandler().handleResponse("Access Denied", HttpStatus.FORBIDDEN, request);
        }

        List<AnimalGroupHistory> history = historyRepository.findByAnimalGroupId(groupId);
        return new GlobalResponseHandler().handleResponse("History for animal group retrieved successfully", history, HttpStatus.OK, request);
    }

    /**
     * Actualiza un registro de historial existente.
     * @param groupId El ID del grupo padre.
     * @param historyId El ID del registro de historial.
     * @param historyDetails Los nuevos datos.
     * @return El registro actualizado.
     */
    @PutMapping("/{historyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateHistory(@PathVariable Long groupId, @PathVariable Long historyId, @RequestBody AnimalGroupHistory historyDetails, HttpServletRequest request) {
        Optional<AnimalGroup> optionalGroup = animalGroupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Animal Group with id " + groupId + " not found", HttpStatus.NOT_FOUND, request);
        }

        if (!hasAccessToFarm(optionalGroup.get().getFarm().getId())) {
            return new GlobalResponseHandler().handleResponse("Access Denied", HttpStatus.FORBIDDEN, request);
        }

        Optional<AnimalGroupHistory> optionalHistory = historyRepository.findById(historyId);
        if (optionalHistory.isPresent()) {
            AnimalGroupHistory existingHistory = optionalHistory.get();
            if (!existingHistory.getAnimalGroup().getId().equals(groupId)) {
                return new GlobalResponseHandler().handleResponse("History record does not belong to the specified animal group", HttpStatus.BAD_REQUEST, request);
            }

            existingHistory.setRecordDate(historyDetails.getRecordDate());
            existingHistory.setProductionValue(historyDetails.getProductionValue());
            existingHistory.setProductionMeasure(historyDetails.getProductionMeasure());
            existingHistory.setNotes(historyDetails.getNotes());
            AnimalGroupHistory updatedHistory = historyRepository.save(existingHistory);

            return new GlobalResponseHandler().handleResponse("History entry updated successfully", updatedHistory, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("History entry with id " + historyId + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina un registro de historial existente.
     * @param groupId El ID del grupo padre.
     * @param historyId El ID del registro de historial.
     * @return Confirmación de la eliminación.
     */
    @DeleteMapping("/{historyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteHistory(@PathVariable Long groupId, @PathVariable Long historyId, HttpServletRequest request) {
        Optional<AnimalGroup> optionalGroup = animalGroupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Animal Group with id " + groupId + " not found", HttpStatus.NOT_FOUND, request);
        }

        if (!hasAccessToFarm(optionalGroup.get().getFarm().getId())) {
            return new GlobalResponseHandler().handleResponse("Access Denied", HttpStatus.FORBIDDEN, request);
        }

        Optional<AnimalGroupHistory> optionalHistory = historyRepository.findById(historyId);
        if (optionalHistory.isPresent()) {
            AnimalGroupHistory history = optionalHistory.get();
            if (!history.getAnimalGroup().getId().equals(groupId)) {
                return new GlobalResponseHandler().handleResponse("History record does not belong to the specified animal group", HttpStatus.BAD_REQUEST, request);
            }
            historyRepository.delete(history);
            return new GlobalResponseHandler().handleResponse("History entry deleted successfully", null, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("History entry with id " + historyId + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    private boolean hasAccessToFarm(Long farmId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }
        return userXFarmRepository.existsById(new UserFarmId(farmId, currentUser.getId()));
    }
}