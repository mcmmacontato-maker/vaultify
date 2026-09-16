package com.vaultify.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import com.vaultify.service.AuthService;
import com.vaultify.service.SessionService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final SessionService sessionService;

    public AuthController(AuthService authService, SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    @PostMapping("/register")
    public void register(@Valid @RequestBody AuthRequest request) {
        try {
            authService.register(request.username(), request.masterPassword());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthService.LoginResult result = authService.login(request.username(), request.masterPassword(), request.otpCode());
            return new TokenResponse(result.token(), result.accessToken(), result.refreshToken());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestBody RefreshRequest request) {
        try {
            return new RefreshResponse(authService.refresh(request.refreshToken()));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }
    }

    @PostMapping("/totp/setup")
    public TotpSetupResponse setupTotp(@Valid @RequestBody TOTPSetupRequest request) {
        try {
            AuthService.TotpSetup setup = authService.setupTotp(request.username(), request.masterPassword());
            return new TotpSetupResponse(setup.secret(), setup.otpauthUrl());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(name = "Authorization", required = false) String authorization) {
        sessionService.remove(token(authorization));
    }

    @GetMapping("/me")
    public UserResponse me(@RequestHeader(name = "Authorization", required = false) String authorization) {
        try {
            return new UserResponse(sessionService.require(token(authorization)).user().getUsername());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }
    }

    static String token(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token ausente");
        }
        return authorization.substring(7);
    }

    public record AuthRequest(@NotBlank String username, @NotBlank String masterPassword) { }
    public record LoginRequest(@NotBlank String username, @NotBlank String masterPassword, String otpCode) { }
    public record TokenResponse(String token, String accessToken, String refreshToken) { }
    public record RefreshRequest(String refreshToken) { }
    public record RefreshResponse(String accessToken) { }
    public record TOTPSetupRequest(@NotBlank String username, @NotBlank String masterPassword) { }
    public record TotpSetupResponse(String secret, String otpauthUrl) { }
    public record UserResponse(String username) { }
}