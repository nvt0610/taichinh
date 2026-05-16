package com.taichinh.app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taichinh.app.entity.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class JwtAuthenticationFilterTest {

    private static final String TEST_SECRET = "test-secret-key-with-at-least-32-bytes";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenSetsAuthentication() throws Exception {
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, objectMapper());
        String token = generateToken(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/wallets");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("11111111-1111-1111-1111-111111111111", authentication.getPrincipal());
        assertEquals(200, response.getStatus());
    }

    @Test
    void invalidBearerTokenReturnsUnauthorized() throws Exception {
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, objectMapper());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/wallets");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertEquals(null, SecurityContextHolder.getContext().getAuthentication());
    }

    private String generateToken(JwtService jwtService) {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        User user = new User("mai", "mai@example.com", "hashed-password");
        ReflectionTestUtils.setField(user, "id", userId);
        return jwtService.generateAccessToken(user, List.of("USER"));
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
