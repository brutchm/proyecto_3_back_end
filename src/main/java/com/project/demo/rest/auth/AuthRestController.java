package com.project.demo.rest.auth;

import com.project.demo.logic.entity.auth.AuthenticationService;
import com.project.demo.logic.entity.auth.GoogleAuthService;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.role.Role;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.RoleRepository;
import com.project.demo.logic.entity.user.LoginResponse;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import io.jsonwebtoken.Claims;
import com.project.demo.logic.entity.auth.VerificationCodeService;
import com.project.demo.logic.entity.mail.EmailService;
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

import java.util.Map;
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
    private final GoogleAuthService googleAuthService;

    public AuthRestController(JwtService jwtService, AuthenticationService authenticationService, GoogleAuthService googleAuthService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.googleAuthService = googleAuthService;
    }

    // --- DTOs para el flujo de Google ---
    private record GoogleAuthRequest(String code) {}
    private record CompleteGoogleSignupRequest(String registrationToken, User userData) {}


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
            return new GlobalResponseHandler().handleResponse("Este correo ya se encuentra en uso.",null,HttpStatus.CONFLICT,request);
        }

        if (existingBusinessId.isPresent()) {
            return new GlobalResponseHandler().handleResponse("La cédula indicada ya se encuentra en uso.",null,HttpStatus.CONFLICT,request);
        }

        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        Optional<Role> optionalRole = roleRepository.findByRoleName(RoleEnum.CORPORATION);

        if (optionalRole.isEmpty()) {

            return new GlobalResponseHandler().handleResponse("Rol no encontrado.",null,HttpStatus.CONFLICT,request);
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

    /**
     * Gestiona el callback de la autenticación de Google. Recibe un código de autorización,
     * lo intercambia por un token de Google y determina si el usuario ya existe.
     *
     * @param request El DTO que contiene el código de autorización ('code') de Google.
     * @return Un ResponseEntity que contiene un mapa con el estado del proceso.
     * - Si el usuario existe, devuelve 'status: LOGIN_SUCCESS' y un token de sesión.
     * - Si es un usuario nuevo, devuelve 'status: REGISTRATION_REQUIRED' y un token de registro temporal.
     * - Devuelve 401 UNAUTHORIZED si ocurre un error de validación.
     */
    @PostMapping("/google/callback")
    public ResponseEntity<Map<String, Object>> googleCallback(@RequestBody GoogleAuthRequest request) {
        try {
            Map<String, Object> response = googleAuthService.processGoogleLogin(request.code());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Finaliza el registro para un nuevo usuario estándar (rol USER) que se autenticó vía Google.
     * Este flujo es sin contraseña y se valida mediante un token de registro temporal.
     *
     * @param request El DTO que contiene el 'registrationToken' y los datos adicionales del usuario ('userData') del formulario.
     * @return Un ResponseEntity con el usuario recién creado y un estado 201 CREATED.
     * - Devuelve 401 UNAUTHORIZED si el token de registro es inválido o ha expirado.
     * - Devuelve 409 CONFLICT si el email ya se encuentra registrado.
     */
    @PostMapping("/google-signup/user")
    public ResponseEntity<?> completeUserSignup(@RequestBody CompleteGoogleSignupRequest request) {
        try {
            Claims claims = jwtService.extractAllClaims(request.registrationToken());
            String email = claims.getSubject();

            if (userRepository.findByUserEmail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Este correo ya fue registrado.");
            }

            User newUser = request.userData();
            newUser.setUserEmail(email);

            Optional<Role> role = roleRepository.findByRoleName(RoleEnum.USER);
            if (role.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Rol 'USER' no encontrado.");
            }
            newUser.setRole(role.get());

            User savedUser = userRepository.save(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de registro inválido o expirado.");
        }
    }

    /**
     * Finaliza el registro para un nuevo usuario corporativo (rol CORPORATION) que se autenticó vía Google.
     * Este flujo es sin contraseña y se valida mediante un token de registro temporal.
     *
     * @param request El DTO que contiene el 'registrationToken' y los datos adicionales del usuario y la empresa ('userData').
     * @return Un ResponseEntity con el usuario recién creado y un estado 201 CREATED.
     * - Devuelve 401 UNAUTHORIZED si el token de registro es inválido o ha expirado.
     * - Devuelve 409 CONFLICT si el email o la cédula de la empresa ya se encuentran registrados.
     */
    @PostMapping("/google-signup/corporation")
    public ResponseEntity<?> completeCorporationSignup(@RequestBody CompleteGoogleSignupRequest request) {
        try {
            Claims claims = jwtService.extractAllClaims(request.registrationToken());
            String email = claims.getSubject();

            if (userRepository.findByUserEmail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Este correo ya fue registrado.");
            }
            if (userRepository.findByBusinessId(request.userData().getBusinessId()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("La cédula indicada ya se encuentra en uso.");
            }

            User newUser = request.userData();
            newUser.setUserEmail(email);

            Optional<Role> role = roleRepository.findByRoleName(RoleEnum.CORPORATION);
            if (role.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Rol 'CORPORATION' no encontrado.");
            }
            newUser.setRole(role.get());

            User savedUser = userRepository.save(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de registro inválido o expirado.");
        } catch (org.springframework.dao.DataAccessException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error de base de datos al guardar el usuario corporativo.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error inesperado en el servidor.");
        }
    }

}