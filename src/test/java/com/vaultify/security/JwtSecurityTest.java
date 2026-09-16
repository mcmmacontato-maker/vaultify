package com.vaultify.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JwtSecurityTest {
    @Test
    void generatesAccessAndRefreshTokens() {
        JwtService jwtService = new JwtService("test-secret-key-1234567890-1234567890", 900000L, 604800000L);
        String access = jwtService.generateAccessToken("alice");
        String refresh = jwtService.generateRefreshToken("alice");

        assertNotNull(access);
        assertNotNull(refresh);
        assertTrue(access.length() > 20);
        assertTrue(refresh.length() > 20);
        assertEquals("alice", jwtService.getUsername(access));
        assertEquals("alice", jwtService.getUsername(refresh));
    }

    @Test
    void verifiesTotpCodes() {
        TotpService totpService = new TotpService();
        String secret = totpService.generateSecret();
        int code = totpService.generateCode(secret);

        assertTrue(totpService.verify(secret, code));
    }
}
