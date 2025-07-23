package com.project.demo.rest.notification;

import com.project.demo.logic.entity.notification.Notification;
import com.project.demo.logic.entity.notification.NotificationRepository;
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
 * Controlador REST para gestionar las notificaciones del usuario autenticado.
 * La creación de notificaciones es una tarea del sistema, por lo que no se expone un endpoint POST público.
 */
@RestController
@RequestMapping("/notifications")
public class NotificationRestController {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Obtiene las notificaciones del usuario autenticado de forma paginada.
     * @param page El número de página a solicitar.
     * @param size El número de elementos por página.
     * @param request La solicitud HTTP.
     * @return ResponseEntity con una lista de notificaciones y metadatos de paginación.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Notification> notificationPage = notificationRepository.findByUserId(currentUser.getId(), pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(notificationPage.getTotalPages());
        meta.setTotalElements(notificationPage.getTotalElements());
        meta.setPageNumber(notificationPage.getNumber() + 1);
        meta.setPageSize(notificationPage.getSize());

        return new GlobalResponseHandler().handleResponse("Notifications retrieved successfully", notificationPage.getContent(), HttpStatus.OK, meta);
    }

    /**
     * Marca una notificación específica del usuario como leída.
     * @param id El ID de la notificación a marcar.
     * @param request La solicitud HTTP.
     * @return ResponseEntity con la notificación actualizada.
     */
    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Notification> optionalNotification = notificationRepository.findByIdAndUserId(id, currentUser.getId());

        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notification.setReadStatus(true);
            Notification updatedNotification = notificationRepository.save(notification);
            return new GlobalResponseHandler().handleResponse("Notification marked as read", updatedNotification, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Notification not found or access denied", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina una notificación del usuario autenticado.
     * @param id El ID de la notificación a eliminar.
     * @param request La solicitud HTTP.
     * @return ResponseEntity con el objeto eliminado o un error si no se encuentra.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Notification> optionalNotification = notificationRepository.findByIdAndUserId(id, currentUser.getId());

        if (optionalNotification.isPresent()) {
            notificationRepository.delete(optionalNotification.get());
            return new GlobalResponseHandler().handleResponse("Notification deleted successfully", optionalNotification.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Notification not found or access denied", HttpStatus.NOT_FOUND, request);
        }
    }
}