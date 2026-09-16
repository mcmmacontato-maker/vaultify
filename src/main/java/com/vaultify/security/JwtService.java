package com.vaultify.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(@Value("${jwt.secret:vaultify-super-secure-jwt-secret-2026-09-16}") String secret,
                      @Value("${jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMs,
                      @Value("${jwt.refresh-token-expiration-ms:604800000}") long refreshTokenExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(normalizeSecret(secret));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(String username) {
        return buildToken(username, accessTokenExpirationMs);
    }

    public String generateRefreshToken(String username) {
        return buildToken(username, refreshTokenExpirationMs);
    }

    public String getUsername(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            claims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private byte[] normalizeSecret(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= 32) {
            return bytes;
        }
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível normalizar a chave JWT", exception);
        }
    }

    private String buildToken(String username, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    private Claims claims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
