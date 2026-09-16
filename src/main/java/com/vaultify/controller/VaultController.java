package com.vaultify.controller;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.vaultify.model.VaultEntry;
import com.vaultify.repository.VaultEntryRepository;
import com.vaultify.service.SessionService;
import com.vaultify.util.CryptoService;

@RestController
@RequestMapping("/api/vault")
public class VaultController {
    private final VaultEntryRepository entryRepository;
    private final SessionService sessionService;
    private final CryptoService cryptoService;

    public VaultController(VaultEntryRepository entryRepository, SessionService sessionService, CryptoService cryptoService) {
        this.entryRepository = entryRepository;
        this.sessionService = sessionService;
        this.cryptoService = cryptoService;
    }

    @GetMapping
    public List<EntryResponse> list(@RequestHeader("Authorization") String authorization,
                                    @RequestParam(defaultValue = "") String search) {
        SessionService.Session session = session(authorization);
        List<VaultEntry> entries = search.isBlank()
                ? entryRepository.findByOwnerId(session.user().getId())
            : entryRepository.search(session.user().getId(), search);
        return entries.stream().map(entry -> response(entry, session.key())).toList();
    }

    @PutMapping("/{id}")
    public EntryResponse update(@RequestHeader("Authorization") String authorization, @PathVariable Long id,
                                @Valid @RequestBody EntryRequest request) {
        SessionService.Session session = session(authorization);
        VaultEntry entry = ownedEntry(session, id);
        CryptoService.EncryptedValue password = cryptoService.encrypt(request.password(), session.key());
        CryptoService.EncryptedValue notes = cryptoService.encrypt(request.notes() == null ? "" : request.notes(), session.key());
        entry.update(request.site(), request.username(), request.category(), password.iv(), password.ciphertext(),
                notes.iv(), notes.ciphertext());
        return response(entryRepository.save(entry), session.key());
    }

    @PostMapping
    public EntryResponse create(@RequestHeader("Authorization") String authorization,
                                @Valid @RequestBody EntryRequest request) {
        SessionService.Session session = session(authorization);
        CryptoService.EncryptedValue password = cryptoService.encrypt(request.password(), session.key());
        CryptoService.EncryptedValue notes = cryptoService.encrypt(request.notes() == null ? "" : request.notes(), session.key());
        VaultEntry entry = entryRepository.save(new VaultEntry(session.user(), request.site(), request.username(),
                request.category(), password.iv(), password.ciphertext(), notes.iv(), notes.ciphertext()));
        return response(entry, session.key());
    }

    @DeleteMapping("/{id}")
    public void delete(@RequestHeader("Authorization") String authorization, @PathVariable Long id) {
        SessionService.Session session = session(authorization);
        VaultEntry entry = ownedEntry(session, id);
        entryRepository.delete(entry);
    }

    private VaultEntry ownedEntry(SessionService.Session session, Long id) {
        VaultEntry entry = entryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!entry.getOwner().getId().equals(session.user().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return entry;
    }

    private SessionService.Session session(String authorization) {
        try {
            return sessionService.require(AuthController.token(authorization));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }
    }

    private EntryResponse response(VaultEntry entry, byte[] key) {
        return new EntryResponse(entry.getId(), entry.getSite(), entry.getLoginUsername(), entry.getCategory(),
                cryptoService.decrypt(entry.getPasswordIv(), entry.getEncryptedPassword(), key),
                cryptoService.decrypt(entry.getNotesIv(), entry.getEncryptedNotes(), key));
    }

    public record EntryRequest(@NotBlank String site, @NotBlank String username, @NotBlank String password,
                               String notes, String category) { }
    public record EntryResponse(Long id, String site, String username, String category, String password, String notes) { }
}