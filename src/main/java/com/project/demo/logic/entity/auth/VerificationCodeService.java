package com.project.demo.logic.entity.auth;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationCodeService {
    // Store codes in memory with expiration time
    private final Map<String, CodeData> verificationCodes = new ConcurrentHashMap<>();
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRATION_MINUTES = 60;

    private static class CodeData {
        String code;
        LocalDateTime expirationTime;

        CodeData(String code, LocalDateTime expirationTime) {
            this.code = code;
            this.expirationTime = expirationTime;
        }
    }

    public String generateCode(String email) {
        String code = generateRandomCode();
        verificationCodes.put(email, new CodeData(code, LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES)));
        return code;
    }

    public boolean verifyCode(String email, String code) {
        CodeData codeData = verificationCodes.get(email);
        if (codeData == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(codeData.expirationTime)) {
            verificationCodes.remove(email);
            return false;
        }

        if (codeData.code.equals(code)) {
            verificationCodes.remove(email);
            return true;
        }

        return false;
    }

    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        
        return code.toString();
    }
}
