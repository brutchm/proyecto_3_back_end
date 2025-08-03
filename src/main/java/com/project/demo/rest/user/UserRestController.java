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

import java.util.List;
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
     * Actualiza el perfil del usuario autenticado.
     *
     * Este endpoint permite que un usuario autenticado edite sus propios datos personales y corporativos.
     *
     * Funcionalidades incluidas:
     * - Verifica si el nuevo businessId está siendo usado por otro usuario. En caso afirmativo, retorna un error.
     * - Verifica si el nuevo userEmail ya existe en la base de datos y pertenece a otro usuario. Si es así, retorna un error.
     * - Actualiza todos los campos personales del usuario: nombre, apellidos, género, teléfono, etc.
     * - Actualiza los campos corporativos: nombre de la empresa, misión, visión, ID, país, provincia, dirección y ubicación.
     * - Si se incluye una nueva contraseña, esta se encripta antes de almacenarse.
     * - Devuelve una respuesta estandarizada indicando éxito o conflicto, según corresponda.
     *
     * Solo se modifican los campos que han cambiado respecto a los datos actuales del usuario.
     *
     * @param userDetails Objeto con los nuevos datos que se desean actualizar.
     * @param request Objeto HttpServletRequest utilizado para construir la respuesta.
     * @return ResponseEntity con mensaje y estado HTTP según el resultado de la operación.
     */

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateAuthenticatedUser(@RequestBody User userDetails, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = (User) authentication.getPrincipal();
        String newBusinessId = userDetails.getBusinessId();
        if (newBusinessId != null && !newBusinessId.equals(existingUser.getBusinessId())) {
            Optional<User> foundBusinessId = userRepository.findByBusinessId(newBusinessId);

            if (foundBusinessId.isPresent()) {
                return new GlobalResponseHandler().handleResponse(
                        "El ID de Corporación '" + newBusinessId + "' ya está registrado por otro usuario.",
                        HttpStatus.CONFLICT, request
                );
            } else {
                existingUser.setBusinessId(newBusinessId);
            }
        }
        String newEmail = userDetails.getUserEmail();
        if (newEmail != null && !newEmail.equalsIgnoreCase(existingUser.getUserEmail())) {
            Optional<User> foundEmail = userRepository.findByUserEmail(newEmail);
            if (foundEmail.isPresent() && !foundEmail.get().getId().equals(existingUser.getId())) {
                return new GlobalResponseHandler().handleResponse(
                        "Este correo electrónico '" + newEmail + "' ya está registrado por otro usuario.",
                        HttpStatus.CONFLICT, request
                );
            } else {
                existingUser.setUserEmail(newEmail);
            }
        }
        // Campos personales
        existingUser.setName(userDetails.getName());
        existingUser.setUserFirstSurename(userDetails.getUserFirstSurename());
        existingUser.setUserSecondSurename(userDetails.getUserSecondSurename());
        existingUser.setUserGender(userDetails.getUserGender());
        existingUser.setUserPhoneNumber(userDetails.getUserPhoneNumber());
        // Campos corporativos
        existingUser.setBusinessName(userDetails.getBusinessName());
        existingUser.setBusinessMission(userDetails.getBusinessMission());
        existingUser.setBusinessVision(userDetails.getBusinessVision());
        existingUser.setBusinessId(userDetails.getBusinessId());
        existingUser.setBusinessCountry(userDetails.getBusinessCountry());
        existingUser.setBusinessStateProvince(userDetails.getBusinessStateProvince());
        existingUser.setBusinessOtherDirections(userDetails.getBusinessOtherDirections());
        existingUser.setBusinessLocation(userDetails.getBusinessLocation());
        if (userDetails.getUserPassword() != null && !userDetails.getUserPassword().isEmpty()) {
            existingUser.setUserPassword(passwordEncoder.encode(userDetails.getUserPassword()));
        }
        User updatedUser = userRepository.save(existingUser);
        return new GlobalResponseHandler().handleResponse("Perfil actualizado correctamente", updatedUser, HttpStatus.OK, request);
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
    public User authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }


    /**
     * Obtiene la lista de todos los usuarios corporativos registrados en el sistema.
     *
     * Este endpoint es accesible únicamente por usuarios con los roles USER o SUPER_ADMIN.
     *
     * @param request el objeto HttpServletRequest que contiene información de la solicitud HTTP.
     * @return una ResponseEntity que contiene:
     *         - Un mensaje de éxito y la lista de usuarios corporativos si existen.
     *         - Un mensaje de error con estado 404 NOT_FOUND si no se encuentran usuarios corporativos registrados.
     *
     * @see User
     * @see GlobalResponseHandler
     * @see org.springframework.security.access.prepost.PreAuthorize
     * @see org.springframework.web.bind.annotation.GetMapping
     */


    @PreAuthorize("hasAnyRole('USER','SUPER_ADMIN')")
    @GetMapping("/listcorporations")
    public ResponseEntity<?> getAllCorporations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> userCorporations = userRepository.findByRoleId(3L, pageable);

      //  List<User> userCorporations= userRepository.findByRoleId(3L);//Rol Corporations
        if (userCorporations.isEmpty()){
            return new GlobalResponseHandler().handleResponse("No se encontraron usuarios corporativos registrados en el sistema",null,HttpStatus.NOT_FOUND,request);
        }

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(userCorporations.getTotalPages());
        meta.setTotalElements(userCorporations.getTotalElements());
        meta.setPageNumber(userCorporations.getNumber() + 1);
        meta.setPageSize(userCorporations.getSize());

       // return new GlobalResponseHandler().handleResponse("Lista con todos las usuarios corporativos registrados",userCorporations,HttpStatus.OK,request);
        return new GlobalResponseHandler().handleResponse(
                "Lista con todos los usuarios corporativos registrados",
                userCorporations.getContent(),
                HttpStatus.OK,
                meta
        );

    }
}