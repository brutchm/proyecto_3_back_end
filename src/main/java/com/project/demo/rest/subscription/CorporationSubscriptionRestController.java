package com.project.demo.rest.subscription;

import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.subscription.CorporationUserSubscription;
import com.project.demo.logic.entity.subscription.CorporationUserSubscriptionId;
import com.project.demo.logic.entity.subscription.CorporationUserSubscriptionRepository;
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
 * Controlador REST para gestionar las suscripciones entre usuarios y corporaciones.
 */
@RestController
@RequestMapping("/corporations")
public class CorporationSubscriptionRestController {

    @Autowired
    private CorporationUserSubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Permite a un usuario autenticado (USER) suscribirse a una corporación.
     * @param corporationId El ID del usuario CORPORACION al que se va a suscribir.
     * @param request La solicitud HTTP.
     * @return La relación de suscripción creada.
     */
    @PostMapping("/{corporationId}/subscribe")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> subscribeToCorporation(@PathVariable Long corporationId, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<User> optionalCorporation = userRepository.findById(corporationId);
        if (optionalCorporation.isEmpty() || !optionalCorporation.get().getRole().getRoleName().equals("CORPORATION")) {
            return new GlobalResponseHandler().handleResponse("Corporation not found", HttpStatus.NOT_FOUND, request);
        }

        CorporationUserSubscriptionId subscriptionId = new CorporationUserSubscriptionId(corporationId, currentUser.getId());
        if (subscriptionRepository.existsById(subscriptionId)) {
            return new GlobalResponseHandler().handleResponse("User is already subscribed to this corporation", HttpStatus.CONFLICT, request);
        }

        CorporationUserSubscription newSubscription = new CorporationUserSubscription();
        newSubscription.setId(subscriptionId);
        newSubscription.setCorporation(optionalCorporation.get());
        newSubscription.setIndividualUser(currentUser);
        newSubscription.setActiveSubscription(true);

        CorporationUserSubscription savedSubscription = subscriptionRepository.save(newSubscription);
        return new GlobalResponseHandler().handleResponse("Successfully subscribed to corporation", savedSubscription, HttpStatus.CREATED, request);
    }

    /**
     * Permite a un usuario autenticado (USER) cancelar su suscripción a una corporación.
     * @param corporationId El ID de la corporación de la que se va a desuscribir.
     * @param request La solicitud HTTP.
     * @return Confirmación de la cancelación.
     */
    @DeleteMapping("/{corporationId}/unsubscribe")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> unsubscribeFromCorporation(@PathVariable Long corporationId, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CorporationUserSubscriptionId subscriptionId = new CorporationUserSubscriptionId(corporationId, currentUser.getId());

        Optional<CorporationUserSubscription> optionalSubscription = subscriptionRepository.findById(subscriptionId);
        if (optionalSubscription.isPresent()) {
            subscriptionRepository.delete(optionalSubscription.get());
            return new GlobalResponseHandler().handleResponse("Successfully unsubscribed from corporation", optionalSubscription.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Subscription not found", HttpStatus.NOT_FOUND, request);
        }
    }
}