package com.project.demo.rest.cropsmanagement;

import com.project.demo.logic.entity.cropsmanagement.CropsManagement;
import com.project.demo.logic.entity.cropsmanagement.CropsManagementId;
import com.project.demo.logic.entity.cropsmanagement.CropsManagementRepository;
import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.role.Role;
import com.project.demo.logic.entity.role.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.userfarm.UserFarmId;
import com.project.demo.logic.entity.userfarm.UserXFarm;
import com.project.demo.logic.entity.userfarm.UserXFarmRepository;
import com.project.demo.logic.entity.crop.CropRepository;
import com.project.demo.logic.entity.farm.FarmRepository;
import com.project.demo.logic.entity.managementaction.ManagementActionRepository;
import com.project.demo.logic.entity.managementaction.ManagementActionDetailRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DTO para la solicitud de añadir un miembro a una granja.
 * Define un contrato claro: se necesita el ID del usuario y el ID de su rol.
 */
record AddMemberRequest(Long userId, Long employeeRoleId) {}

/**
 * Controlador REST para gestionar los registros de manejo de cultivos en una granja.
 * Se trata como una bitácora inmutable: solo se añaden y consultan registros.
 */
@RestController
@RequestMapping("/farms/{farmId}/management-records")
public class CropsManagementRestController {

    @Autowired
    private CropsManagementRepository cropsManagementRepository;

    @Autowired
    private FarmRepository farmRepository;


    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private ManagementActionRepository managementActionRepository;

    @Autowired
    private ManagementActionDetailRepository detailRepository;

    @Autowired
    private UserXFarmRepository userXFarmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Añade un usuario como miembro a una granja con un rol específico.
     * @param farmId El ID de la granja donde se añadirá el miembro.
     * @param addMemberRequest La solicitud con el ID del usuario y el ID de su rol.
     * @param request La solicitud HTTP.
     * @return La nueva relación de membresía creada.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addMemberToFarm(@PathVariable Long farmId, @RequestBody AddMemberRequest addMemberRequest, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to manage members for farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        Optional<Farm> farm = farmRepository.findById(farmId);
        Optional<User> userToAdd = userRepository.findById(addMemberRequest.userId());
        Optional<Role> roleToAssign = roleRepository.findById(addMemberRequest.employeeRoleId());

        if (farm.isEmpty() || userToAdd.isEmpty() || roleToAssign.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Farm, User or Role not found", HttpStatus.NOT_FOUND, request);
        }

        UserFarmId newId = new UserFarmId(farmId, addMemberRequest.userId());
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
     * Obtiene todos los registros de manejo para una granja.
     * @param farmId El ID de la granja.
     * @return Una lista de registros de manejo.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getManagementRecords(@PathVariable Long farmId, HttpServletRequest request) {
        if (!hasAccessToFarm(farmId)) {
            return new GlobalResponseHandler().handleResponse("Access Denied to farm " + farmId, HttpStatus.FORBIDDEN, request);
        }

        List<CropsManagement> records = cropsManagementRepository.findById_FarmId(farmId);
        return new GlobalResponseHandler().handleResponse("Management records retrieved successfully", records, HttpStatus.OK, request);
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