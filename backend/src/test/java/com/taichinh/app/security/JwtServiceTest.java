package com.taichinh.app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.taichinh.app.entity.User;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private static final String TEST_SECRET = "test-secret-key-with-at-least-32-bytes";

    @Test
    void generateAccessTokenIncludesExpectedClaims() {
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000);
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        User user = new User("mai", "mai@example.com", "hashed-password");
        ReflectionTestUtils.setField(user, "id", userId);

        String token = jwtService.generateAccessToken(user, List.of("USER"));
        Claims claims = jwtService.parseClaims(token);

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("mai", claims.get("username", String.class));
        assertEquals("mai@example.com", claims.get("email", String.class));
        assertEquals(List.of("USER"), claims.get("roles", List.class));
        assertTrue(jwtService.isTokenValid(token));
    }
}
