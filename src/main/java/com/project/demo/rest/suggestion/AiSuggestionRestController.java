package com.project.demo.rest.suggestion;

import com.project.demo.logic.entity.suggestion.AiSuggestion;
import com.project.demo.logic.entity.suggestion.AiSuggestionRepository;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para consultar y gestionar las sugerencias de la IA para el usuario autenticado.
 * Los usuarios no pueden crear ni editar sugerencias, solo leerlas y eliminarlas (descartarlas).
 */
@RestController
@RequestMapping("/ai-suggestions")
public class AiSuggestionRestController {

    @Autowired
    private AiSuggestionRepository aiSuggestionRepository;

    /**
     * Obtiene una lista paginada de las sugerencias de IA para el usuario autenticado.
     * @param page El número de página a solicitar.
     * @param size El número de elementos por página.
     * @param request La solicitud HTTP.
     * @return ResponseEntity con una lista de sugerencias y metadatos de paginación.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserSuggestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<AiSuggestion> suggestionPage = aiSuggestionRepository.findByUserId(currentUser.getId(), pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(suggestionPage.getTotalPages());
        meta.setTotalElements(suggestionPage.getTotalElements());
        meta.setPageNumber(suggestionPage.getNumber() + 1);
        meta.setPageSize(suggestionPage.getSize());

        return new GlobalResponseHandler().handleResponse("AI suggestions retrieved successfully", suggestionPage.getContent(), HttpStatus.OK, meta);
    }

    /**
     * Elimina (o descarta) una sugerencia específica del usuario autenticado.
     * @param id El ID de la sugerencia a eliminar.
     * @param request La solicitud HTTP.
     * @return ResponseEntity con el objeto eliminado o un error si no se encuentra.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteSuggestion(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<AiSuggestion> optionalSuggestion = aiSuggestionRepository.findByIdAndUserId(id, currentUser.getId());

        if (optionalSuggestion.isPresent()) {
            aiSuggestionRepository.delete(optionalSuggestion.get());
            return new GlobalResponseHandler().handleResponse("Suggestion dismissed successfully", optionalSuggestion.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Suggestion not found or access denied", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Crea y guarda una nueva sugerencia asociada al usuario autenticado.
     * @param ai Objeto AiSuggestion recibido en el cuerpo de la solicitud.
     * @param currentUser Usuario actualmente autenticado.
     * @param request La solicitud HTTP.
     * @return ResponseEntity con el objeto guardado y mensaje de éxito.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addUserSuggestions(
            @RequestBody AiSuggestion ai,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {

        ai.setUser(currentUser);
        AiSuggestion aiSaved= aiSuggestionRepository.save(ai);

        return new GlobalResponseHandler().handleResponse("Sugerencia creada correctamente", aiSaved, HttpStatus.CREATED,request);
    }


    @Autowired
    private AiSuggestionService aiSuggestionService;
    /**
     * Genera una sugerencia temporal basada en el prompt y la finca especificada para el usuario autenticado.
     * @param payload Mapa con los parámetros 'prompt' (mensaje) y 'farmId' (ID de la finca).
     * @param currentUser Usuario actualmente autenticado.
     * @param request La solicitud HTTP.
     * @return ResponseEntity con la sugerencia generada o error si faltan parámetros obligatorios.
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> generateTemporarySuggestion(
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {

        String prompt = (String) payload.get("prompt");
        Long farmId = payload.get("farmId") != null ? Long.valueOf(payload.get("farmId").toString()) : null;

        if (prompt == null || prompt.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("El mensaje es requerido", HttpStatus.BAD_REQUEST, request);
        }
        if (farmId == null) {
            return new GlobalResponseHandler().handleResponse("El farmId es requerido", HttpStatus.BAD_REQUEST, request);
        }

        String suggestion = aiSuggestionService.generateSuggestion(prompt, farmId);

        return new GlobalResponseHandler().handleResponse("Sugerencia generada exitosamente", suggestion, HttpStatus.OK, request);
    }

}