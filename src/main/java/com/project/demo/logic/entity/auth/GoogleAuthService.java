package com.project.demo.logic.entity.auth;

import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Profile("!test")
@Service
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final GoogleIdTokenVerifier verifier;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Value("${google.token-uri}")
    private String tokenUri;

    public GoogleAuthService(UserRepository userRepository, JwtService jwtService, @Value("${google.client-id}") String clientId) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.restTemplate = new RestTemplate();
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public Map<String, Object> processGoogleLogin(String code) {
        ResponseEntity<Map> tokenResponse = exchangeCodeForToken(code);
        String idTokenString = (String) tokenResponse.getBody().get("id_token");

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("Token de Google inválido.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            if (!payload.getEmailVerified()) {
                throw new RuntimeException("El email de Google no está verificado.");
            }

            Optional<User> userOptional = userRepository.findByUserEmail(email);
            Map<String, Object> response = new HashMap<>();

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String sessionToken = jwtService.generateToken(user);
                response.put("status", "LOGIN_SUCCESS");
                response.put("token", sessionToken);
                response.put("authUser", user);
            } else {
                Map<String, Object> claims = new HashMap<>();
                claims.put("email", email);
                claims.put("given_name", payload.get("given_name"));
                claims.put("family_name", payload.get("family_name"));
                claims.put("picture", payload.get("picture"));

                String registrationToken = jwtService.generateRegistrationToken(claims);
                response.put("status", "REGISTRATION_REQUIRED");
                response.put("token", registrationToken);
            }
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la autenticación de Google: " + e.getMessage(), e);
        }
    }

    private ResponseEntity<Map> exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("code", code);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("redirect_uri", redirectUri);
        map.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForEntity(tokenUri, request, Map.class);
    }
}