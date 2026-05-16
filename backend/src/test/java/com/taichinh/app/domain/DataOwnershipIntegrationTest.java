package com.taichinh.app.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.YearMonth;
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
class DataOwnershipIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void usersCannotAccessEachOtherDomainDataAndDashboard() throws Exception {
        Credentials userA = registerAndLogin("ownerA");
        Credentials userB = registerAndLogin("ownerB");

        String walletId = readBody(exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/wallets",
                userA.accessToken(),
                Map.of(
                        "name", "Owner Wallet",
                        "type", "BANK",
                        "balance", "200.00",
                        "description", "Wallet of user A")))
                .path("data").path("id").asText();

        String categoryId = readBody(exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/categories",
                userA.accessToken(),
                Map.of(
                        "name", "Owner Food",
                        "type", "EXPENSE",
                        "icon", "utensils",
                        "color", "#FF6600")))
                .path("data").path("id").asText();

        ResponseEntity<String> transactionResponse = exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/expense",
                userA.accessToken(),
                Map.of(
                        "walletId", walletId,
                        "categoryId", categoryId,
                        "amount", "50.00",
                        "title", "Owner expense",
                        "note", "Only user A should see this",
                        "transactionDate", YearMonth.now().atDay(10).atTime(9, 0).toString()));
        String transactionId = readBody(transactionResponse).path("data").path("id").asText();

        assertNotFound(userB.accessToken(), "/api/wallets/" + walletId);
        assertNotFound(userB.accessToken(), "/api/categories/" + categoryId);
        assertNotFound(userB.accessToken(), "/api/transactions/" + transactionId);

        JsonNode walletListBody = readBody(exchangeWithBearer(HttpMethod.GET, "/api/wallets?page=1&size=10", userB.accessToken()));
        assertEquals(0, walletListBody.path("data").size());
        assertEquals(0, walletListBody.path("pagination").path("totalItems").asInt());

        JsonNode categoryListBody = readBody(exchangeWithBearer(HttpMethod.GET, "/api/categories?page=1&size=10", userB.accessToken()));
        assertEquals(0, categoryListBody.path("data").size());
        assertEquals(0, categoryListBody.path("pagination").path("totalItems").asInt());

        JsonNode transactionListBody = readBody(exchangeWithBearer(HttpMethod.GET, "/api/transactions?page=1&size=10", userB.accessToken()));
        assertEquals(0, transactionListBody.path("data").size());
        assertEquals(0, transactionListBody.path("pagination").path("totalItems").asInt());

        JsonNode dashboardSummaryBody = readBody(exchangeWithBearer(HttpMethod.GET, "/api/dashboard/summary", userB.accessToken()));
        assertEquals("0", dashboardSummaryBody.path("data").path("totalBalance").decimalValue().stripTrailingZeros().toPlainString());
        assertEquals("0", dashboardSummaryBody.path("data").path("totalIncome").decimalValue().stripTrailingZeros().toPlainString());
        assertEquals("0", dashboardSummaryBody.path("data").path("totalExpense").decimalValue().stripTrailingZeros().toPlainString());

        JsonNode recentTransactionsBody = readBody(exchangeWithBearer(HttpMethod.GET, "/api/dashboard/recent-transactions?limit=5", userB.accessToken()));
        assertEquals(0, recentTransactionsBody.path("data").size());

        JsonNode topSpendingBody = readBody(exchangeWithBearer(HttpMethod.GET, "/api/dashboard/top-spending-categories", userB.accessToken()));
        assertEquals(0, topSpendingBody.path("data").size());
    }

    private Credentials registerAndLogin(String prefix) throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String username = prefix + suffix.substring(0, 18);
        String email = prefix + suffix + "@example.com";
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

        return new Credentials(accessToken);
    }

    private void assertNotFound(String accessToken, String path) throws Exception {
        ResponseEntity<String> response = exchangeWithBearer(HttpMethod.GET, path, accessToken);
        JsonNode body = readBody(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("NOT_FOUND", body.path("error").path("code").asText());
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

    private record Credentials(String accessToken) {
    }
}
