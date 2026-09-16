package com.vaultify.service;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import com.vaultify.model.User;
import com.vaultify.repository.UserRepository;
import com.vaultify.security.JwtService;
import com.vaultify.security.TotpService;
import com.vaultify.util.CryptoService;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final CryptoService cryptoService;
    private final SessionService sessionService;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final Argon2PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public AuthService(UserRepository userRepository, CryptoService cryptoService,
                       SessionService sessionService, JwtService jwtService, TotpService totpService) {
        this.userRepository = userRepository;
        this.cryptoService = cryptoService;
        this.sessionService = sessionService;
        this.jwtService = jwtService;
        this.totpService = totpService;
    }

    public void register(String username, String masterPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Usuário já cadastrado");
        }
        byte[] salt = cryptoService.newSalt();
        userRepository.save(new User(username, passwordEncoder.encode(masterPassword), salt));
    }

    public LoginResult login(String username, String masterPassword, String otpCode) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));
        if (!passwordEncoder.matches(masterPassword, user.getMasterPasswordHash())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        if (user.isTotpEnabled()) {
            if (!isValidOtp(user.getTotpSecret(), otpCode)) {
                throw new IllegalArgumentException("Código TOTP inválido");
            }
        }
        byte[] key = cryptoService.deriveKey(masterPassword.toCharArray(), user.getEncryptionSalt());
        String sessionToken = sessionService.create(user, key);
        return new LoginResult(sessionToken,
                jwtService.generateAccessToken(username),
                jwtService.generateRefreshToken(username));
    }

    private boolean isValidOtp(String secret, String otpCode) {
        if (otpCode == null || !otpCode.matches("\\d{6}")) {
            return false;
        }
        return totpService.verify(secret, Integer.parseInt(otpCode));
    }

    public TotpSetup setupTotp(String username, String masterPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));
        if (!passwordEncoder.matches(masterPassword, user.getMasterPasswordHash())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        String secret = totpService.generateSecret();
        user.setTotpSecret(secret);
        user.setTotpEnabled(true);
        userRepository.save(user);
        return new TotpSetup(secret, totpService.generateOtpAuthUri(username, secret));
    }

    public String refresh(String refreshToken) {
        if (!jwtService.isValid(refreshToken)) {
            throw new IllegalArgumentException("Refresh token inválido");
        }
        return jwtService.generateAccessToken(jwtService.getUsername(refreshToken));
    }

    public record LoginResult(String token, String accessToken, String refreshToken) { }
    public record TotpSetup(String secret, String otpauthUrl) { }
}