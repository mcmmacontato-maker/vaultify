package com.vaultify.security;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class TotpService {
    private static final int SECRET_LENGTH = 20;
    private static final int PERIOD_SECONDS = 30;
    private static final int DIGITS = 6;
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateSecret() {
        byte[] bytes = new byte[SECRET_LENGTH];
        secureRandom.nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public int generateCode(String secret) {
        long counter = Instant.now().getEpochSecond() / PERIOD_SECONDS;
        return generateCode(secret, counter);
    }

    public boolean verify(String secret, int code) {
        int window = 1;
        long currentCounter = Instant.now().getEpochSecond() / PERIOD_SECONDS;
        for (int offset = -window; offset <= window; offset++) {
            if (generateCode(secret, currentCounter + offset) == code) {
                return true;
            }
        }
        return false;
    }

    public String generateOtpAuthUri(String username, String secret) {
        return "otpauth://totp/Vaultify%3A" + username + "?secret=" + secret + "&issuer=Vaultify";
    }

    private int generateCode(String secret, long counter) {
        byte[] key = java.util.Base64.getUrlDecoder().decode(secret);
        byte[] data = new byte[8];
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (counter & 0xFFL);
            counter >>= 8;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24) | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8) | (hash[offset + 3] & 0xFF);
            int code = binary % (int) Math.pow(10, DIGITS);
            return code;
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Erro ao gerar código TOTP", exception);
        }
    }
}
