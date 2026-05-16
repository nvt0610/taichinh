package com.taichinh.app.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void authFlowWorksEndToEnd() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String username = "user" + suffix.substring(0, 20);
        String email = "user" + suffix + "@example.com";
        String password = "Password1";

        ResponseEntity<String> healthResponse = restTemplate.getForEntity(url("/api/health"), String.class);
        JsonNode healthBody = readBody(healthResponse);
        assertEquals(HttpStatus.OK, healthResponse.getStatusCode());
        assertTrue(healthBody.get("success").asBoolean());

        ResponseEntity<String> protectedWithoutToken = restTemplate.getForEntity(url("/api/wallets"), String.class);
        JsonNode protectedWithoutTokenBody = readBody(protectedWithoutToken);
        assertEquals(HttpStatus.UNAUTHORIZED, protectedWithoutToken.getStatusCode());
        assertFalse(protectedWithoutTokenBody.get("success").asBoolean());

        ResponseEntity<String> registerResponse = postJson(
                "/api/auth/register",
                Map.of(
                        "username", username,
                        "email", email,
                        "password", password));
        JsonNode registerBody = readBody(registerResponse);
        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());
        assertTrue(registerBody.get("success").asBoolean());
        assertEquals(username, registerBody.path("data").path("username").asText());
        assertEquals(email, registerBody.path("data").path("email").asText());

        ResponseEntity<String> loginResponse = postJson(
                "/api/auth/login",
                Map.of(
                        "usernameOrEmail", username,
                        "password", password));
        JsonNode loginBody = readBody(loginResponse);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertTrue(loginBody.get("success").asBoolean());

        String accessToken = loginBody.path("data").path("accessToken").asText();
        String refreshToken = loginBody.path("data").path("refreshToken").asText();
        assertFalse(accessToken.isBlank());
        assertFalse(refreshToken.isBlank());

        ResponseEntity<String> protectedWithToken = exchangeWithBearer("/api/wallets", accessToken);
        JsonNode protectedWithTokenBody = readBody(protectedWithToken);
        assertEquals(HttpStatus.OK, protectedWithToken.getStatusCode());
        assertTrue(protectedWithTokenBody.get("success").asBoolean());
        assertEquals(0, protectedWithTokenBody.path("data").size());

        ResponseEntity<String> refreshResponse = postJson(
                "/api/auth/refresh",
                Map.of("refreshToken", refreshToken));
        JsonNode refreshBody = readBody(refreshResponse);
        assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());
        assertTrue(refreshBody.get("success").asBoolean());
        assertNotNull(refreshBody.path("data").path("accessToken").asText());

        ResponseEntity<String> logoutResponse = postJson(
                "/api/auth/logout",
                Map.of("refreshToken", refreshToken));
        JsonNode logoutBody = readBody(logoutResponse);
        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());
        assertTrue(logoutBody.get("success").asBoolean());

        ResponseEntity<String> refreshAfterLogoutResponse = postJson(
                "/api/auth/refresh",
                Map.of("refreshToken", refreshToken));
        JsonNode refreshAfterLogoutBody = readBody(refreshAfterLogoutResponse);
        assertEquals(HttpStatus.UNAUTHORIZED, refreshAfterLogoutResponse.getStatusCode());
        assertFalse(refreshAfterLogoutBody.get("success").asBoolean());
        assertEquals("INVALID_REFRESH_TOKEN", refreshAfterLogoutBody.path("error").path("code").asText());
    }

    private ResponseEntity<String> postJson(String path, Object body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url(path)))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.status(response.statusCode()).body(response.body());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private ResponseEntity<String> exchangeWithBearer(String path, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return restTemplate.exchange(url(path), HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    private JsonNode readBody(ResponseEntity<String> response) throws Exception {
        return objectMapper.readTree(response.getBody());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
