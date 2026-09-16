package com.vaultify.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import com.vaultify.model.User;

@Service
public class SessionService {
    private static final Duration IDLE_TIMEOUT = Duration.ofMinutes(15);
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public String create(User user, byte[] derivedKey) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new Session(user, derivedKey, Instant.now()));
        return token;
    }

    public Session require(String token) {
        Session session = sessions.get(token);
        if (session == null || session.lastAccess().plus(IDLE_TIMEOUT).isBefore(Instant.now())) {
            sessions.remove(token);
            throw new IllegalArgumentException("Sessão ausente ou expirada");
        }
        session.touch();
        return session;
    }

    public void remove(String token) { sessions.remove(token); }

    public static final class Session {
        private final User user;
        private final byte[] key;
        private volatile Instant lastAccess;

        private Session(User user, byte[] key, Instant lastAccess) {
            this.user = user;
            this.key = key;
            this.lastAccess = lastAccess;
        }
        public User user() { return user; }
        public byte[] key() { return key; }
        public Instant lastAccess() { return lastAccess; }
        private void touch() { lastAccess = Instant.now(); }
    }
}