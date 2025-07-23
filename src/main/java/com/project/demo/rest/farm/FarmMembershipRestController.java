package com.project.demo.rest.farm;

import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.farm.FarmRepository;
import com.project.demo.logic.entity.role.Role;
import com.project.demo.logic.entity.role.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.userfarm.UserFarmId;
import com.project.demo.logic.entity.userfarm.UserXFarm;
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
import java.util.Map;
import java.util.Optional;

/**
 * DTO (Data Transfer Object) para la solicitud de añadir un miembro a una granja.
 * Define un contrato claro: se necesita el ID del usuario y el ID de su rol.
 */
record AddMemberRequest(Long userId, Long employeeRoleId) {}

/**
 * Controlador REST para gestionar la membresía de usuarios en una granja.
 */
@RestController
@RequestMapping("/farms/{farmId}/members")
public class FarmMembershipRestController {

    @Autowired
    private UserXFarmRepository userXFarmRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Añade un usuario como miembro a una granja con un rol específico.
     * @param farmId El ID de la granja donde se añadirá el miembro.
     * @param addMemberRequest La solicitud con el ID del usuario y el ID de su rol, encapsulada en un DTO.
     * @param request La solicitud HTTP.
     * @return La nueva relación de membresía creada.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addMemberToFarm(@PathVariable Long farmId, @RequestBody AddMemberRequest addMemberRequest, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to manage members for farm " + farmId, HttpStatus.FORBIDDEN, request);
        }
        Long userId = addMemberRequest.userId();
        Long employeeRoleId = addMemberRequest.employeeRoleId();

        Optional<Farm> farm = farmRepository.findById(farmId);
        Optional<User> userToAdd = userRepository.findById(userId);
        Optional<Role> roleToAssign = roleRepository.findById(employeeRoleId);

        if (farm.isEmpty() || userToAdd.isEmpty() || roleToAssign.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Farm, User or Role not found", HttpStatus.NOT_FOUND, request);
        }

        UserFarmId newId = new UserFarmId(farmId, userId);
        if (userXFarmRepository.existsById(newId)) {
            return new GlobalResponseHandler().handleResponse("User is already a member of this farm", HttpStatus.CONFLICT, request);
        }

        UserXFarm newMember = new UserXFarm();
        newMember.setId(newId);
        newMember.setFarm(farm.get());
        newMember.setUser(userToAdd.get());
        newMember.setEmployeeRole(roleToAssign.get());
        newMember.setActive(true);

        UserXFarm savedMember = userXFarmRepository.save(newMember);
        return new GlobalResponseHandler().handleResponse("User added to farm successfully", savedMember, HttpStatus.CREATED, request);
    }

    /**
     * Obtiene la lista de todos los miembros de una granja.
     * @param farmId El ID de la granja.
     * @return Una lista de las relaciones de membresía.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFarmMembers(@PathVariable Long farmId, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to view members of farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        List<UserXFarm> members = userXFarmRepository.findByFarmId(farmId);
        return new GlobalResponseHandler().handleResponse("Members retrieved successfully", members, HttpStatus.OK, request);
    }

    /**
     * Elimina a un miembro de una granja.
     * @param farmId El ID de la granja.
     * @param userId El ID del usuario a eliminar.
     * @return Una confirmación de la eliminación.
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeMemberFromFarm(@PathVariable Long farmId, @PathVariable Long userId, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to manage members for farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        UserFarmId memberId = new UserFarmId(farmId, userId);
        Optional<UserXFarm> optionalMember = userXFarmRepository.findById(memberId);

        if (optionalMember.isPresent()) {
            userXFarmRepository.delete(optionalMember.get());
            return new GlobalResponseHandler().handleResponse("Member removed successfully", optionalMember.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Member not found in this farm", HttpStatus.NOT_FOUND, request);
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