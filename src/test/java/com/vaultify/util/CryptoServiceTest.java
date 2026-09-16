package com.vaultify.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class CryptoServiceTest {
    private final CryptoService cryptoService = new CryptoService();

    @Test
    void encryptsAndDecryptsWithUniqueIvPerValue() {
        byte[] key = cryptoService.deriveKey("master-password".toCharArray(), cryptoService.newSalt());
        CryptoService.EncryptedValue first = cryptoService.encrypt("segredo", key);
        CryptoService.EncryptedValue second = cryptoService.encrypt("segredo", key);

        assertEquals("segredo", cryptoService.decrypt(first.iv(), first.ciphertext(), key));
        assertNotEquals(first.iv(), second.iv());
        assertNotEquals(first.ciphertext(), second.ciphertext());
    }
}