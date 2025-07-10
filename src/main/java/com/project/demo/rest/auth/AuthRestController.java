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

    public AuthRestController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    /**
     * Autentica un usuario y retorna un JWT si las credenciales son válidas.
     * @param user Usuario con email y contraseña.
     * @return LoginResponse con token JWT y datos del usuario autenticado.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody User user) {
        User authenticatedUser = authenticationService.authenticate(user);

        String jwtToken = jwtService.generateToken((UserDetails) authenticatedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        Optional<User> foundedUser = userRepository.findByUserEmail(user.getUserEmail());

        foundedUser.ifPresent(loginResponse::setAuthUser);

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Registra un nuevo usuario estándar (rol USER).
     * @param user Datos del usuario a registrar.
     * @return Usuario registrado o error si el email ya existe.
     */
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
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        Optional<User> optionalUser = userRepository.findByUserEmail(request.email());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        }

        System.out.println(request.email());
        String verificationCode = verificationCodeService.generateCode(request.email());
        emailService.sendVerificationCode(request.email(), verificationCode);

        return ResponseEntity.ok(new MessageResponse("Password reset code sent to your email"));
    }

    /**
     * Verifica el código enviado al email del usuario para recuperación de contraseña.
     * @param request Objeto con email y código recibido.
     * @return Mensaje de éxito o error si el código es inválido o expiró.
     */
    @PostMapping("/reset-password/verify")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest request) {
        boolean isValid = verificationCodeService.verifyCode(request.email(), request.code());
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired code");
        }
        return ResponseEntity.ok(new MessageResponse("Code verified successfully"));
    }

    /**
     * Permite restablecer la contraseña de un usuario si el código es válido.
     * @param request Objeto con email, código y nueva contraseña.
     * @return Mensaje de éxito o error si el código es inválido o expiró.
     */
    @PostMapping("/reset-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        Optional<User> optionalUser = userRepository.findByUserEmail(request.email());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        }

        boolean isValid = verificationCodeService.verifyCode(request.email(), request.code());
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired code");
        }

        User user = optionalUser.get();
        user.setUserPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
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
            User savedUser = userRepository.save(user);
            return new GlobalResponseHandler().handleResponse("Usuario corporativo registrado exitosamente.",savedUser,HttpStatus.CREATED,request);
        }

    }
}