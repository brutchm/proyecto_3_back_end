package com.project.demo.rest.preferences;

import com.project.demo.logic.entity.dashboard.DashboardPreferences;
import com.project.demo.logic.entity.dashboard.DashboardPreferencesRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * DTO simple para la solicitud de actualización de preferencias.
 */
record PreferencesRequest(String preferencesJson) {}

/**
 * Controlador REST para gestionar las preferencias del dashboard del usuario.
 * Actúa sobre un recurso único por usuario.
 */
@RestController
@RequestMapping("/preferences/dashboard")
public class DashboardPreferencesRestController {

    @Autowired
    private DashboardPreferencesRepository preferencesRepository;

    /**
     * Obtiene las preferencias del dashboard para el usuario autenticado.
     * @param request La solicitud HTTP.
     * @return Las preferencias guardadas o un 404 si no existen.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDashboardPreferences(HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<DashboardPreferences> preferences = preferencesRepository.findById(currentUser.getId());

        if (preferences.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Dashboard preferences retrieved", preferences.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("No dashboard preferences found for this user", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Crea o actualiza las preferencias del dashboard para el usuario autenticado.
     * @param preferencesRequest La solicitud con el string JSON de las preferencias.
     * @param request La solicitud HTTP.
     * @return Las preferencias guardadas.
     */
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> setDashboardPreferences(@RequestBody PreferencesRequest preferencesRequest, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DashboardPreferences preferences = preferencesRepository.findById(currentUser.getId())
                .orElse(new DashboardPreferences());

        preferences.setId(currentUser.getId());
        preferences.setUser(currentUser);
        preferences.setPreferences(preferencesRequest.preferencesJson());

        DashboardPreferences savedPreferences = preferencesRepository.save(preferences);
        return new GlobalResponseHandler().handleResponse("Dashboard preferences saved successfully", savedPreferences, HttpStatus.OK, request);
    }
}