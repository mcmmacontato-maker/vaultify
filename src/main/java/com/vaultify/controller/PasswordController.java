package com.vaultify.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.vaultify.util.CryptoService;

@RestController
@RequestMapping("/api/passwords")
public class PasswordController {
    private final CryptoService cryptoService;

    public PasswordController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping("/generate")
    public PasswordResponse generate(@RequestParam(defaultValue = "24") int length) {
        return new PasswordResponse(cryptoService.generatePassword(length));
    }

    public record PasswordResponse(String password) { }
}