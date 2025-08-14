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

/**
 * Controlador REST para autenticación y recuperación de contraseña de usuarios.
 * Incluye login, registro, registro de corporaciones y endpoints para recuperación de contraseña vía email.
 */
@RequestMapping("/auth")
@RestController
public class AuthRestController {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;


    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private EmailService emailService;

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

    /**
     * Autentica un usuario y retorna un JWT si las credenciales son válidas.
     * @param user Usuario con email y contraseña.
     * @return LoginResponse con token JWT y datos del usuario autenticado.
     */

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody User user, HttpServletRequest request) {
        try {
            User authenticatedUser = authenticationService.authenticate(user);

            String jwtToken = jwtService.generateToken((UserDetails) authenticatedUser);

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(jwtToken);
            loginResponse.setExpiresIn(jwtService.getExpirationTime());

            Optional<User> foundedUser = userRepository.findByUserEmail(user.getUserEmail());
            foundedUser.ifPresent(loginResponse::setAuthUser);

            return new GlobalResponseHandler().handleResponse("Login successful", loginResponse, HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse("Login failed: " + e.getMessage(), null, HttpStatus.UNAUTHORIZED, request);
        }
    }

    /**
     * Registra un nuevo usuario estándar (rol USER).
     * @param user Datos del usuario a registrar.
     * @return Usuario registrado o error si el email ya existe.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user, HttpServletRequest request) {
        try {
            Optional<User> existingUser = userRepository.findByUserEmail(user.getUserEmail());
            if (existingUser.isPresent()) {
                return new GlobalResponseHandler().handleResponse("Correo electronico actualmente en uso", null, HttpStatus.CONFLICT, request);
            }

            user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
            Optional<Role> optionalRole = roleRepository.findByRoleName(RoleEnum.USER);

            if (optionalRole.isEmpty()) {
                return new GlobalResponseHandler().handleResponse("Rol no encontrado", null, HttpStatus.BAD_REQUEST, request);
            }
            user.setRole(optionalRole.get());
            user.setIsActive(true);
            User savedUser = userRepository.save(user);

            return new GlobalResponseHandler().handleResponse("Usuario registrado correctamente", savedUser, HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse("El registro fallo: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR, request);
        }

    }

    record PasswordResetRequest(String email) {}
    record VerifyCodeRequest(String email, String code) {}
    record ResetPasswordRequest(String email, String code, String newPassword) {}
    public record MessageResponse(String message) {}

    /**
     * Solicita el envío de un código de verificación al email para recuperación de contraseña.
     * @param request Objeto con el email del usuario.
     * @return Mensaje de éxito o error si el email no existe.
     */
    @PostMapping("/reset-password/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest req, HttpServletRequest request) {
        try {
            Optional<User> optionalUser = userRepository.findByUserEmail(req.email());
            if (optionalUser.isEmpty()) {
                return new GlobalResponseHandler().handleResponse("Correo electronico no encontrado", null, HttpStatus.NOT_FOUND, request);
            }

            String verificationCode = verificationCodeService.generateCode(req.email());
            emailService.sendVerificationCode(req.email(), verificationCode);

            return new GlobalResponseHandler().handleResponse("Codigo de reseteo de contraseña enviado a tu correo electronico", new MessageResponse("Codigo de reseteo de contraseña enviado a tu correo electronico"), HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse("Password reset request failed: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR, request);
        }
    }

    /**
     * Verifica el código enviado al email del usuario para recuperación de contraseña.
     * @param request Objeto con email y código recibido.
     * @return Mensaje de éxito o error si el código es inválido o expiró.
     */
    @PostMapping("/reset-password/verify")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest req, HttpServletRequest request) {
        try {
            boolean isValid = verificationCodeService.verifyCode(req.email(), req.code());
            if (!isValid) {
                return new GlobalResponseHandler().handleResponse("Invalid or expired code", null, HttpStatus.BAD_REQUEST, request);
            }
            return new GlobalResponseHandler().handleResponse("Code verified successfully", new MessageResponse("Code verified successfully"), HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse("Code verification failed: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR, request);
        }
    }

    /**
     * Permite restablecer la contraseña de un usuario si el código es válido.
     * @param request Objeto con email, código y nueva contraseña.
     * @return Mensaje de éxito o error si el código es inválido o expiró.
     */
    @PostMapping("/reset-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req, HttpServletRequest request) {
        try {
            Optional<User> optionalUser = userRepository.findByUserEmail(req.email());
            if (optionalUser.isEmpty()) {
                return new GlobalResponseHandler().handleResponse("Correo electronico no encontrado", null, HttpStatus.NOT_FOUND, request);
            }

            boolean isValid = verificationCodeService.verifyCode(req.email(), req.code());
            if (!isValid) {
                return new GlobalResponseHandler().handleResponse("Invalid or expired code", null, HttpStatus.BAD_REQUEST, request);
            }

            User user = optionalUser.get();
            user.setUserPassword(passwordEncoder.encode(req.newPassword()));
            userRepository.save(user);

            return new GlobalResponseHandler().handleResponse("Password reset successfully", new MessageResponse("Password reset successfully"), HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalResponseHandler().handleResponse("Password reset failed: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR, request);
        }
    }
    
    /**
     * Registra un nuevo usuario corporativo (rol CORPORATION).
     * @param user Datos del usuario corporativo.
     * @param request HttpServletRequest para manejo de respuestas globales.
     * @return Usuario corporativo registrado o error si los datos ya existen o faltan campos obligatorios.
     */
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
            user.setIsActive(true);
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

            newUser.setIsActive(true);
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
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Este correo ya fue registrado."));
            }
            if (userRepository.findByBusinessId(request.userData().getBusinessId()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","La identificación empresarial indicada ya se encuentra en uso."));
            }

            User newUser = request.userData();
            newUser.setUserEmail(email);

            Optional<Role> role = roleRepository.findByRoleName(RoleEnum.CORPORATION);
            if (role.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Rol 'CORPORATION' no encontrado.");
            }
            newUser.setRole(role.get());

            newUser.setIsActive(true);
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