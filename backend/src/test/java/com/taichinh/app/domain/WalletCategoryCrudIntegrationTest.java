package com.taichinh.app.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
class WalletCategoryCrudIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void walletAndCategoryCrudWorksEndToEnd() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String username = "crud" + suffix.substring(0, 20);
        String email = "crud" + suffix + "@example.com";
        String password = "Password1";

        postJson("/api/auth/register", Map.of(
                "username", username,
                "email", email,
                "password", password));

        JsonNode loginBody = readBody(postJson("/api/auth/login", Map.of(
                "usernameOrEmail", username,
                "password", password)));
        String accessToken = loginBody.path("data").path("accessToken").asText();
        assertFalse(accessToken.isBlank());

        ResponseEntity<String> createWalletResponse = exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/wallets",
                accessToken,
                Map.of(
                        "name", "Main Wallet",
                        "type", "BANK",
                        "balance", "125000.50",
                        "description", "Primary spending wallet"));
        JsonNode createWalletBody = readBody(createWalletResponse);
        assertEquals(HttpStatus.CREATED, createWalletResponse.getStatusCode());
        assertTrue(createWalletBody.path("success").asBoolean());
        String walletId = createWalletBody.path("data").path("id").asText();
        assertEquals("Main Wallet", createWalletBody.path("data").path("name").asText());
        assertEquals("BANK", createWalletBody.path("data").path("type").asText());

        ResponseEntity<String> listWalletsResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/wallets?page=1&size=10&q=Main&sort=createdAt,desc",
                accessToken);
        JsonNode listWalletsBody = readBody(listWalletsResponse);
        assertEquals(HttpStatus.OK, listWalletsResponse.getStatusCode());
        assertEquals(1, listWalletsBody.path("data").size());
        assertEquals(1, listWalletsBody.path("pagination").path("totalItems").asInt());

        ResponseEntity<String> getWalletResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/wallets/" + walletId,
                accessToken);
        JsonNode getWalletBody = readBody(getWalletResponse);
        assertEquals(HttpStatus.OK, getWalletResponse.getStatusCode());
        assertEquals("Main Wallet", getWalletBody.path("data").path("name").asText());

        ResponseEntity<String> updateWalletResponse = exchangeJsonWithBearer(
                HttpMethod.PUT,
                "/api/wallets/" + walletId,
                accessToken,
                Map.of(
                        "name", "Emergency Wallet",
                        "type", "SAVINGS",
                        "description", "Reserve fund"));
        JsonNode updateWalletBody = readBody(updateWalletResponse);
        assertEquals(HttpStatus.OK, updateWalletResponse.getStatusCode());
        assertEquals("Emergency Wallet", updateWalletBody.path("data").path("name").asText());
        assertEquals("SAVINGS", updateWalletBody.path("data").path("type").asText());
        assertEquals(0, new BigDecimal("125000.50").compareTo(updateWalletBody.path("data").path("balance").decimalValue()));

        ResponseEntity<String> deleteWalletResponse = exchangeWithBearer(
                HttpMethod.DELETE,
                "/api/wallets/" + walletId,
                accessToken);
        JsonNode deleteWalletBody = readBody(deleteWalletResponse);
        assertEquals(HttpStatus.OK, deleteWalletResponse.getStatusCode());
        assertTrue(deleteWalletBody.path("success").asBoolean());

        ResponseEntity<String> getDeletedWalletResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/wallets/" + walletId,
                accessToken);
        JsonNode getDeletedWalletBody = readBody(getDeletedWalletResponse);
        assertEquals(HttpStatus.NOT_FOUND, getDeletedWalletResponse.getStatusCode());
        assertEquals("NOT_FOUND", getDeletedWalletBody.path("error").path("code").asText());

        ResponseEntity<String> createExpenseCategoryResponse = exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/categories",
                accessToken,
                Map.of(
                        "name", "Food",
                        "type", "EXPENSE",
                        "icon", "utensils",
                        "color", "#FF6600"));
        JsonNode createExpenseCategoryBody = readBody(createExpenseCategoryResponse);
        assertEquals(HttpStatus.CREATED, createExpenseCategoryResponse.getStatusCode());
        String expenseCategoryId = createExpenseCategoryBody.path("data").path("id").asText();

        ResponseEntity<String> createIncomeCategoryResponse = exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/categories",
                accessToken,
                Map.of(
                        "name", "Salary",
                        "type", "INCOME",
                        "icon", "briefcase",
                        "color", "#00AA55"));
        assertEquals(HttpStatus.CREATED, createIncomeCategoryResponse.getStatusCode());

        ResponseEntity<String> listExpenseCategoriesResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/categories?page=1&size=10&type=EXPENSE&q=Foo&sort=name,asc",
                accessToken);
        JsonNode listExpenseCategoriesBody = readBody(listExpenseCategoriesResponse);
        assertEquals(HttpStatus.OK, listExpenseCategoriesResponse.getStatusCode());
        assertEquals(1, listExpenseCategoriesBody.path("data").size());
        assertEquals("Food", listExpenseCategoriesBody.path("data").get(0).path("name").asText());
        assertEquals(1, listExpenseCategoriesBody.path("pagination").path("totalItems").asInt());

        ResponseEntity<String> getCategoryResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/categories/" + expenseCategoryId,
                accessToken);
        JsonNode getCategoryBody = readBody(getCategoryResponse);
        assertEquals(HttpStatus.OK, getCategoryResponse.getStatusCode());
        assertEquals("EXPENSE", getCategoryBody.path("data").path("type").asText());

        ResponseEntity<String> updateCategoryResponse = exchangeJsonWithBearer(
                HttpMethod.PUT,
                "/api/categories/" + expenseCategoryId,
                accessToken,
                Map.of(
                        "name", "Dining Out",
                        "type", "EXPENSE",
                        "icon", "utensils-crossed",
                        "color", "#FF8844"));
        JsonNode updateCategoryBody = readBody(updateCategoryResponse);
        assertEquals(HttpStatus.OK, updateCategoryResponse.getStatusCode());
        assertEquals("Dining Out", updateCategoryBody.path("data").path("name").asText());

        ResponseEntity<String> deleteCategoryResponse = exchangeWithBearer(
                HttpMethod.DELETE,
                "/api/categories/" + expenseCategoryId,
                accessToken);
        JsonNode deleteCategoryBody = readBody(deleteCategoryResponse);
        assertEquals(HttpStatus.OK, deleteCategoryResponse.getStatusCode());
        assertTrue(deleteCategoryBody.path("success").asBoolean());

        ResponseEntity<String> getDeletedCategoryResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/categories/" + expenseCategoryId,
                accessToken);
        JsonNode getDeletedCategoryBody = readBody(getDeletedCategoryResponse);
        assertEquals(HttpStatus.NOT_FOUND, getDeletedCategoryResponse.getStatusCode());
        assertEquals("NOT_FOUND", getDeletedCategoryBody.path("error").path("code").asText());
    }

    private ResponseEntity<String> postJson(String path, Object body) throws Exception {
        return exchangeJson(HttpMethod.POST, path, body);
    }

    private ResponseEntity<String> exchangeJson(HttpMethod method, String path, Object body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(
                url(path),
                method,
                new HttpEntity<>(objectMapper.writeValueAsString(body), headers),
                String.class);
    }

    private ResponseEntity<String> exchangeJsonWithBearer(
            HttpMethod method,
            String path,
            String accessToken,
            Object body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return restTemplate.exchange(
                url(path),
                method,
                new HttpEntity<>(objectMapper.writeValueAsString(body), headers),
                String.class);
    }

    private ResponseEntity<String> exchangeWithBearer(HttpMethod method, String path, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return restTemplate.exchange(url(path), method, new HttpEntity<>(headers), String.class);
    }

    private JsonNode readBody(ResponseEntity<String> response) throws Exception {
        return objectMapper.readTree(response.getBody());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
