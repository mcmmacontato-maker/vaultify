package com.vaultify.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class CryptoService {
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final String KDF = "PBKDF2WithHmacSHA256";
    private static final int KEY_BITS = 256;
    private static final int ITERATIONS = 600_000;
    private static final int SALT_BYTES = 16;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private final SecureRandom random = new SecureRandom();

    public byte[] deriveKey(char[] masterPassword, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(masterPassword, salt, ITERATIONS, KEY_BITS);
            return SecretKeyFactory.getInstance(KDF).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Falha ao derivar chave do cofre", exception);
        }
    }

    public EncryptedValue encrypt(String plaintext, byte[] key) {
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(key), new GCMParameterSpec(TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return new EncryptedValue(Base64.getEncoder().encodeToString(iv),
                    Base64.getEncoder().encodeToString(ciphertext));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Falha ao criptografar dado", exception);
        }
    }

    public String decrypt(String iv, String ciphertext, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, secretKey(key), new GCMParameterSpec(TAG_BITS,
                    Base64.getDecoder().decode(iv)));
            return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Dado criptografado inválido ou chave incorreta", exception);
        }
    }

    public byte[] newSalt() {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return salt;
    }

    public String generatePassword(int length) {
        if (length < 16 || length > 128) {
            throw new IllegalArgumentException("O tamanho deve estar entre 16 e 128 caracteres");
        }
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*_-";
        StringBuilder password = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            password.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return password.toString();
    }

    private SecretKey secretKey(byte[] key) {
        return new SecretKeySpec(key, "AES");
    }

    public record EncryptedValue(String iv, String ciphertext) { }
}