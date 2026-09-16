package com.vaultify.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 120)
    private String username;
    @Column(nullable = false)
    private String masterPasswordHash;
    @Column(nullable = false)
    private byte[] encryptionSalt;
    @Column(nullable = false)
    private boolean totpEnabled = false;
    @Column(length = 64)
    private String totpSecret;

    protected User() { }

    public User(String username, String masterPasswordHash, byte[] encryptionSalt) {
        this.username = username;
        this.masterPasswordHash = masterPasswordHash;
        this.encryptionSalt = encryptionSalt;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getMasterPasswordHash() { return masterPasswordHash; }
    public byte[] getEncryptionSalt() { return encryptionSalt; }
    public boolean isTotpEnabled() { return totpEnabled; }
    public String getTotpSecret() { return totpSecret; }
    public void setTotpEnabled(boolean totpEnabled) { this.totpEnabled = totpEnabled; }
    public void setTotpSecret(String totpSecret) { this.totpSecret = totpSecret; }
}