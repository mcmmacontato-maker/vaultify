package com.vaultify.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import com.vaultify.model.User;

class SessionServiceTest {
    @Test
    void rejectsExpiredSession() throws Exception {
        SessionService service = new SessionService();
        User user = new User("tester", "hash", new byte[16]);
        String token = service.create(user, new byte[32]);

        Field sessionsField = SessionService.class.getDeclaredField("sessions");
        sessionsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, SessionService.Session> sessions =
                (ConcurrentHashMap<String, SessionService.Session>) sessionsField.get(service);
        SessionService.Session session = sessions.get(token);
        Field lastAccessField = SessionService.Session.class.getDeclaredField("lastAccess");
        lastAccessField.setAccessible(true);
        lastAccessField.set(session, Instant.now().minusSeconds(16 * 60));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.require(token));
        assertTrue(exception.getMessage().contains("expirada"));
    }
}