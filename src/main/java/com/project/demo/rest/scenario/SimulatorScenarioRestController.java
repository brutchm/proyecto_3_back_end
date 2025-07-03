package com.project.demo.rest.scenario;

import com.project.demo.logic.entity.scenario.SimulatorScenario;
import com.project.demo.logic.entity.scenario.SimulatorScenarioRepository;
import com.project.demo.logic.entity.user.User;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controlador REST para gestionar los escenarios de simulación de un usuario.
 * Todas las operaciones están acotadas al usuario autenticado.
 */
@RestController
@RequestMapping("/simulator-scenarios")
public class SimulatorScenarioRestController {

    @Autowired
    private SimulatorScenarioRepository scenarioRepository;

    /**
     * Crea un nuevo escenario de simulación para el usuario autenticado.
     * @param scenario El escenario a crear.
     * @return El escenario guardado.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createScenario(@RequestBody SimulatorScenario scenario, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        scenario.setUser(currentUser);
        SimulatorScenario savedScenario = scenarioRepository.save(scenario);
        return new GlobalResponseHandler().handleResponse("Scenario created successfully", savedScenario, HttpStatus.CREATED, request);
    }

    /**
     * Obtiene una lista paginada de los escenarios del usuario autenticado.
     * @return Una lista de escenarios.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserScenarios(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<SimulatorScenario> scenarioPage = scenarioRepository.findByUserId(currentUser.getId(), pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(scenarioPage.getTotalPages());
        meta.setTotalElements(scenarioPage.getTotalElements());
        meta.setPageNumber(scenarioPage.getNumber() + 1);
        meta.setPageSize(scenarioPage.getSize());

        return new GlobalResponseHandler().handleResponse("Scenarios retrieved successfully", scenarioPage.getContent(), HttpStatus.OK, meta);
    }

    /**
     * Obtiene un escenario específico, validando que pertenece al usuario.
     * @param id El ID del escenario.
     * @return El escenario encontrado.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getScenarioById(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<SimulatorScenario> scenario = scenarioRepository.findByIdAndUserId(id, currentUser.getId());
        if (scenario.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Scenario retrieved successfully", scenario.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Scenario not found or access denied", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Actualiza un escenario existente, validando la propiedad.
     * @param id El ID del escenario a actualizar.
     * @param scenarioDetails Los nuevos datos.
     * @return El escenario actualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateScenario(@PathVariable Long id, @RequestBody SimulatorScenario scenarioDetails, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<SimulatorScenario> optionalScenario = scenarioRepository.findByIdAndUserId(id, currentUser.getId());

        if (optionalScenario.isPresent()) {
            SimulatorScenario existingScenario = optionalScenario.get();
            existingScenario.setScenarioName(scenarioDetails.getScenarioName());
            existingScenario.setDescription(scenarioDetails.getDescription());
            existingScenario.setParameters(scenarioDetails.getParameters());
            SimulatorScenario updatedScenario = scenarioRepository.save(existingScenario);
            return new GlobalResponseHandler().handleResponse("Scenario updated successfully", updatedScenario, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Scenario not found or access denied", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina un escenario del usuario.
     * @param id El ID del escenario a eliminar.
     * @return Confirmación de la eliminación.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteScenario(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<SimulatorScenario> optionalScenario = scenarioRepository.findByIdAndUserId(id, currentUser.getId());

        if (optionalScenario.isPresent()) {
            scenarioRepository.delete(optionalScenario.get());
            return new GlobalResponseHandler().handleResponse("Scenario deleted successfully", optionalScenario.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Scenario not found or access denied", HttpStatus.NOT_FOUND, request);
        }
    }
}