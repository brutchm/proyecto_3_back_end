package com.project.demo.rest.auth;

import com.project.demo.logic.entity.auth.AuthenticationService;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.role.Role;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.RoleRepository;
import com.project.demo.logic.entity.user.LoginResponse;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RequestMapping("/auth")
@RestController
public class AuthRestController {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;



    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public AuthRestController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody User user) {
        System.out.println(user.getUserEmail());
        System.out.println(user.getUserPassword());
        User authenticatedUser = authenticationService.authenticate(user);

        String jwtToken = jwtService.generateToken((UserDetails) authenticatedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        Optional<User> foundedUser = userRepository.findByUserEmail(user.getUserEmail());

        foundedUser.ifPresent(loginResponse::setAuthUser);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByUserEmail(user.getUserEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }

        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        Optional<Role> optionalRole = roleRepository.findByRoleName(RoleEnum.USER);

        if (optionalRole.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role not found");
        }
        user.setRole(optionalRole.get());
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/signup/corporation")
    public ResponseEntity<?> registerUserCorporation(@RequestBody User user, HttpServletRequest request) {
        Optional<User> existingUser = userRepository.findByUserEmail(user.getUserEmail());
        Optional<User> existingBusinessId = userRepository.findByBusinessId(user.getBusinessId());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Este correo ya se encuentra en uso.");
        }

        if (existingBusinessId.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("La cédula indicada ya se encuentra en uso.");
        }

        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        Optional<Role> optionalRole = roleRepository.findByRoleName(RoleEnum.USER);

        if (optionalRole.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rol no encontrado.");
        }

        user.setRole(optionalRole.get());

        if (user.getBusinessName().isEmpty() || user.getBusinessId().isEmpty()
                || user.getBusinessCountry().isEmpty() || user.getBusinessStateProvince().isEmpty()
                || user.getPassword().isEmpty() ||
                user.getBusinessMission().isEmpty() || user.getBusinessVision().isEmpty()) {

            return new GlobalResponseHandler().handleResponse("Debe ingresar los campos obligatorios.",user,HttpStatus.NOT_ACCEPTABLE,request);

        }else{
            User savedUser = userRepository.save(user);
            return new GlobalResponseHandler().handleResponse("Usuario corporativo registrado exitosamente.",savedUser,HttpStatus.CREATED,request);
        }

    }
}