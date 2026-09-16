package com.vaultify.model;

import java.time.Instant;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vault_entries")
public class VaultEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private User owner;
    private String site;
    private String loginUsername;
    private String category;
    private String passwordIv;
    private String encryptedPassword;
    private String notesIv;
    private String encryptedNotes;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    protected VaultEntry() { }

    public VaultEntry(User owner, String site, String loginUsername, String category,
                      String passwordIv, String encryptedPassword, String notesIv, String encryptedNotes) {
        this.owner = owner;
        this.site = site;
        this.loginUsername = loginUsername;
        this.category = category;
        this.passwordIv = passwordIv;
        this.encryptedPassword = encryptedPassword;
        this.notesIv = notesIv;
        this.encryptedNotes = encryptedNotes;
    }

    public Long getId() { return id; }
    public User getOwner() { return owner; }
    public String getSite() { return site; }
    public String getLoginUsername() { return loginUsername; }
    public String getCategory() { return category; }
    public String getPasswordIv() { return passwordIv; }
    public String getEncryptedPassword() { return encryptedPassword; }
    public String getNotesIv() { return notesIv; }
    public String getEncryptedNotes() { return encryptedNotes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(String site, String loginUsername, String category,
                       String passwordIv, String encryptedPassword, String notesIv, String encryptedNotes) {
        this.site = site;
        this.loginUsername = loginUsername;
        this.category = category;
        this.passwordIv = passwordIv;
        this.encryptedPassword = encryptedPassword;
        this.notesIv = notesIv;
        this.encryptedNotes = encryptedNotes;
        this.updatedAt = Instant.now();
    }
}