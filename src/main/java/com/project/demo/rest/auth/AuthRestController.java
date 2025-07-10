package com.project.demo.rest.auth;

import com.project.demo.logic.entity.auth.AuthenticationService;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.role.Role;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.RoleRepository;
import com.project.demo.logic.entity.user.LoginResponse;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.auth.VerificationCodeService;
import com.project.demo.logic.entity.mail.EmailService;
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

    @PostMapping("/reset-password/verify")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest request) {
        boolean isValid = verificationCodeService.verifyCode(request.email(), request.code());
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired code");
        }
        return ResponseEntity.ok(new MessageResponse("Code verified successfully"));
    }

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
}