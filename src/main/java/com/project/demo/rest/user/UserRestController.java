package com.project.demo.rest.user;

import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controlador REST para la gestión de usuarios por parte de administradores.
 */
@RestController
@RequestMapping("/users")
public class UserRestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtiene una lista paginada de todos los usuarios.
     * Solo accesible para SUPER_ADMIN.
     * @return Una respuesta con la lista de usuarios y metadatos de paginación.
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> usersPage = userRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(usersPage.getTotalPages());
        meta.setTotalElements(usersPage.getTotalElements());
        meta.setPageNumber(usersPage.getNumber() + 1);
        meta.setPageSize(usersPage.getSize());

        return new GlobalResponseHandler().handleResponse("Users retrieved successfully",
                usersPage.getContent(), HttpStatus.OK, meta);
    }

    /**
     * Añade un nuevo usuario al sistema
     * Accesible solo para rol SUPER_ADMIN
     *
     * @param user
     * @param request
     * @return
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> addUser(@RequestBody User user, HttpServletRequest request) {
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        userRepository.save(user);
        return new GlobalResponseHandler().handleResponse("User updated successfully",
                user, HttpStatus.OK, request);
    }

    /**
     * Actualiza los datos de un usuario existente.
     * Un SUPER_ADMIN puede editar cualquier usuario.
     * @param id El ID del usuario a actualizar.
     * @param userDetails Los nuevos detalles para el usuario.
     * @return El usuario con los datos actualizados.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails, HttpServletRequest request) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setUserName(userDetails.getUserName());
            existingUser.setUserFirstSurename(userDetails.getUserFirstSurename());
            existingUser.setUserSecondSurename(userDetails.getUserSecondSurename());
            existingUser.setIsActive(userDetails.getIsActive());

            if (userDetails.getUserPassword() != null && !userDetails.getUserPassword().isEmpty()) {
                existingUser.setUserPassword(passwordEncoder.encode(userDetails.getUserPassword()));
            }

            User updatedUser = userRepository.save(existingUser);
            return new GlobalResponseHandler().handleResponse("User updated successfully", updatedUser, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("User id " + id + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina usuario por ID
     * Accesible solo para rol SUPER_ADMIN
     *
     * @param userId
     * @param request
     * @return
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        Optional<User> foundOrder = userRepository.findById(userId);
        if(foundOrder.isPresent()) {
            userRepository.deleteById(userId);
            return new GlobalResponseHandler().handleResponse("User deleted successfully",
                    foundOrder.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("User id " + userId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Obtiene los detalles del usuario actualmente autenticado.
     * @return El objeto User del solicitante.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAuthenticatedUser(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Optional<User> user = userRepository.findById(currentUser.getId());

        if (user.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Authenticated user retrieved successfully", user.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Authenticated user not found in database", HttpStatus.NOT_FOUND, request);
        }
    }
}