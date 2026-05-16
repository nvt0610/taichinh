package com.taichinh.app.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taichinh.app.entity.Transaction;
import com.taichinh.app.repository.TransactionRepository;
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
class TransactionFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void incomeExpenseAndTransferUpdateBalancesCorrectly() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String username = "txn" + suffix.substring(0, 20);
        String email = "txn" + suffix + "@example.com";
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

        String sourceWalletId = readBody(exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/wallets",
                accessToken,
                Map.of(
                        "name", "Checking",
                        "type", "BANK",
                        "balance", "0.00",
                        "description", "Main wallet")))
                .path("data").path("id").asText();

        String destinationWalletId = readBody(exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/wallets",
                accessToken,
                Map.of(
                        "name", "Savings",
                        "type", "SAVINGS",
                        "balance", "0.00",
                        "description", "Reserve wallet")))
                .path("data").path("id").asText();

        String incomeCategoryId = readBody(exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/categories",
                accessToken,
                Map.of(
                        "name", "Salary",
                        "type", "INCOME",
                        "icon", "briefcase",
                        "color", "#00AA55")))
                .path("data").path("id").asText();

        String expenseCategoryId = readBody(exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/categories",
                accessToken,
                Map.of(
                        "name", "Food",
                        "type", "EXPENSE",
                        "icon", "utensils",
                        "color", "#FF6600")))
                .path("data").path("id").asText();

        ResponseEntity<String> incomeResponse = exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/income",
                accessToken,
                Map.of(
                        "walletId", sourceWalletId,
                        "categoryId", incomeCategoryId,
                        "amount", "500.00",
                        "title", "May salary",
                        "note", "Monthly income",
                        "transactionDate", "2026-05-15T08:00:00"));
        JsonNode incomeBody = readBody(incomeResponse);
        assertEquals(HttpStatus.CREATED, incomeResponse.getStatusCode());
        assertEquals("INCOME", incomeBody.path("data").path("type").asText());

        assertWalletBalance(accessToken, sourceWalletId, "500.00");

        ResponseEntity<String> updateIncomeResponse = exchangeJsonWithBearer(
                HttpMethod.PUT,
                "/api/transactions/" + incomeBody.path("data").path("id").asText() + "/income",
                accessToken,
                Map.of(
                        "walletId", sourceWalletId,
                        "categoryId", incomeCategoryId,
                        "amount", "550.00",
                        "title", "Updated salary",
                        "note", "Adjusted income",
                        "transactionDate", "2026-05-15T08:30:00"));
        JsonNode updateIncomeBody = readBody(updateIncomeResponse);
        assertEquals(HttpStatus.OK, updateIncomeResponse.getStatusCode());
        assertEquals("Updated salary", updateIncomeBody.path("data").path("title").asText());
        assertWalletBalance(accessToken, sourceWalletId, "550.00");

        ResponseEntity<String> expenseResponse = exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/expense",
                accessToken,
                Map.of(
                        "walletId", sourceWalletId,
                        "categoryId", expenseCategoryId,
                        "amount", "120.25",
                        "title", "Dinner",
                        "note", "Weekend meal",
                        "transactionDate", "2026-05-15T12:00:00"));
        JsonNode expenseBody = readBody(expenseResponse);
        assertEquals(HttpStatus.CREATED, expenseResponse.getStatusCode());
        assertEquals("EXPENSE", expenseBody.path("data").path("type").asText());

        assertWalletBalance(accessToken, sourceWalletId, "429.75");

        ResponseEntity<String> updateExpenseResponse = exchangeJsonWithBearer(
                HttpMethod.PUT,
                "/api/transactions/" + expenseBody.path("data").path("id").asText() + "/expense",
                accessToken,
                Map.of(
                        "walletId", sourceWalletId,
                        "categoryId", expenseCategoryId,
                        "amount", "100.00",
                        "title", "Updated dinner",
                        "note", "Adjusted expense",
                        "transactionDate", "2026-05-15T12:30:00"));
        JsonNode updateExpenseBody = readBody(updateExpenseResponse);
        assertEquals(HttpStatus.OK, updateExpenseResponse.getStatusCode());
        assertEquals("Updated dinner", updateExpenseBody.path("data").path("title").asText());
        assertWalletBalance(accessToken, sourceWalletId, "450.00");

        ResponseEntity<String> invalidCategoryResponse = exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/income",
                accessToken,
                Map.of(
                        "walletId", sourceWalletId,
                        "categoryId", expenseCategoryId,
                        "amount", "10.00",
                        "title", "Wrong category",
                        "note", "Should fail",
                        "transactionDate", "2026-05-15T13:00:00"));
        JsonNode invalidCategoryBody = readBody(invalidCategoryResponse);
        assertEquals(HttpStatus.BAD_REQUEST, invalidCategoryResponse.getStatusCode());
        assertFalse(invalidCategoryBody.path("success").asBoolean());

        ResponseEntity<String> insufficientExpenseResponse = exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/expense",
                accessToken,
                Map.of(
                        "walletId", sourceWalletId,
                        "categoryId", expenseCategoryId,
                        "amount", "1000.00",
                        "title", "Too much",
                        "note", "Should fail",
                        "transactionDate", "2026-05-15T14:00:00"));
        JsonNode insufficientExpenseBody = readBody(insufficientExpenseResponse);
        assertEquals(HttpStatus.BAD_REQUEST, insufficientExpenseResponse.getStatusCode());
        assertEquals("INSUFFICIENT_BALANCE", insufficientExpenseBody.path("error").path("code").asText());

        ResponseEntity<String> transferResponse = exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/transfer",
                accessToken,
                Map.of(
                        "sourceWalletId", sourceWalletId,
                        "destinationWalletId", destinationWalletId,
                        "amount", "79.75",
                        "title", "Move to savings",
                        "note", "Monthly reserve",
                        "transactionDate", "2026-05-15T15:00:00"));
        JsonNode transferBody = readBody(transferResponse);
        assertEquals(HttpStatus.CREATED, transferResponse.getStatusCode());
        assertEquals("TRANSFER", transferBody.path("data").path("sourceTransaction").path("type").asText());
        assertEquals("TRANSFER", transferBody.path("data").path("destinationTransaction").path("type").asText());
        assertEquals(0, new BigDecimal("-79.75").compareTo(
                transferBody.path("data").path("sourceTransaction").path("amount").decimalValue()));
        assertEquals(0, new BigDecimal("79.75").compareTo(
                transferBody.path("data").path("destinationTransaction").path("amount").decimalValue()));

        UUID sourceTransactionId = UUID.fromString(transferBody.path("data").path("sourceTransaction").path("id").asText());
        UUID destinationTransactionId = UUID.fromString(transferBody.path("data").path("destinationTransaction").path("id").asText());
        Transaction sourceTransaction = transactionRepository.findById(sourceTransactionId).orElseThrow();
        Transaction destinationTransaction = transactionRepository.findById(destinationTransactionId).orElseThrow();
        assertEquals(destinationTransactionId, sourceTransaction.getReferenceTransactionId());
        assertEquals(sourceTransactionId, destinationTransaction.getReferenceTransactionId());

        assertWalletBalance(accessToken, sourceWalletId, "370.25");
        assertWalletBalance(accessToken, destinationWalletId, "79.75");

        ResponseEntity<String> transactionListResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/transactions?page=1&size=10&type=TRANSFER&walletId=" + sourceWalletId + "&q=Move&sort=transactionDate,desc",
                accessToken);
        JsonNode transactionListBody = readBody(transactionListResponse);
        assertEquals(HttpStatus.OK, transactionListResponse.getStatusCode());
        assertEquals(1, transactionListBody.path("data").size());
        assertEquals(1, transactionListBody.path("pagination").path("totalItems").asInt());
        assertEquals(sourceTransactionId.toString(), transactionListBody.path("data").get(0).path("id").asText());

        ResponseEntity<String> updateTransferResponse = exchangeJsonWithBearer(
                HttpMethod.PUT,
                "/api/transactions/" + sourceTransactionId + "/transfer",
                accessToken,
                Map.of(
                        "sourceWalletId", sourceWalletId,
                        "destinationWalletId", destinationWalletId,
                        "amount", "70.00",
                        "title", "Updated transfer",
                        "note", "Adjusted reserve",
                        "transactionDate", "2026-05-15T15:30:00"));
        JsonNode updateTransferBody = readBody(updateTransferResponse);
        assertEquals(HttpStatus.OK, updateTransferResponse.getStatusCode());
        assertEquals(0, new BigDecimal("-70.00").compareTo(
                updateTransferBody.path("data").path("sourceTransaction").path("amount").decimalValue()));
        assertWalletBalance(accessToken, sourceWalletId, "380.00");
        assertWalletBalance(accessToken, destinationWalletId, "70.00");

        ResponseEntity<String> deleteExpenseResponse = exchangeWithBearer(
                HttpMethod.DELETE,
                "/api/transactions/" + expenseBody.path("data").path("id").asText(),
                accessToken);
        JsonNode deleteExpenseBody = readBody(deleteExpenseResponse);
        assertEquals(HttpStatus.OK, deleteExpenseResponse.getStatusCode());
        assertTrue(deleteExpenseBody.path("success").asBoolean());
        Transaction deletedExpense = transactionRepository.findById(UUID.fromString(expenseBody.path("data").path("id").asText())).orElseThrow();
        assertTrue(deletedExpense.getDeletedAt() != null);
        assertWalletBalance(accessToken, sourceWalletId, "480.00");

        ResponseEntity<String> deletedExpenseGetResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/transactions/" + expenseBody.path("data").path("id").asText(),
                accessToken);
        JsonNode deletedExpenseGetBody = readBody(deletedExpenseGetResponse);
        assertEquals(HttpStatus.NOT_FOUND, deletedExpenseGetResponse.getStatusCode());
        assertEquals("NOT_FOUND", deletedExpenseGetBody.path("error").path("code").asText());

        ResponseEntity<String> deleteTransferResponse = exchangeWithBearer(
                HttpMethod.DELETE,
                "/api/transactions/" + sourceTransactionId,
                accessToken);
        JsonNode deleteTransferBody = readBody(deleteTransferResponse);
        assertEquals(HttpStatus.OK, deleteTransferResponse.getStatusCode());
        assertTrue(deleteTransferBody.path("success").asBoolean());
        Transaction deletedSourceTransfer = transactionRepository.findById(sourceTransactionId).orElseThrow();
        Transaction deletedDestinationTransfer = transactionRepository.findById(destinationTransactionId).orElseThrow();
        assertTrue(deletedSourceTransfer.getDeletedAt() != null);
        assertTrue(deletedDestinationTransfer.getDeletedAt() != null);
        assertWalletBalance(accessToken, sourceWalletId, "550.00");
        assertWalletBalance(accessToken, destinationWalletId, "0.00");

        ResponseEntity<String> transactionListAfterDeleteResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/transactions?page=1&size=10&sort=transactionDate,desc",
                accessToken);
        JsonNode transactionListAfterDeleteBody = readBody(transactionListAfterDeleteResponse);
        assertEquals(HttpStatus.OK, transactionListAfterDeleteResponse.getStatusCode());
        assertEquals(1, transactionListAfterDeleteBody.path("pagination").path("totalItems").asInt());
        assertEquals("Updated salary", transactionListAfterDeleteBody.path("data").get(0).path("title").asText());

        ResponseEntity<String> destinationIncomeResponse = exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/income",
                accessToken,
                Map.of(
                        "walletId", destinationWalletId,
                        "categoryId", incomeCategoryId,
                        "amount", "25.00",
                        "title", "Pocket money",
                        "note", "For delete guard",
                        "transactionDate", "2026-05-15T18:00:00"));
        JsonNode destinationIncomeBody = readBody(destinationIncomeResponse);
        assertEquals(HttpStatus.CREATED, destinationIncomeResponse.getStatusCode());
        assertWalletBalance(accessToken, destinationWalletId, "25.00");

        ResponseEntity<String> destinationExpenseResponse = exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/expense",
                accessToken,
                Map.of(
                        "walletId", destinationWalletId,
                        "categoryId", expenseCategoryId,
                        "amount", "25.00",
                        "title", "Snack",
                        "note", "Consumes destination income",
                        "transactionDate", "2026-05-15T18:30:00"));
        assertEquals(HttpStatus.CREATED, destinationExpenseResponse.getStatusCode());
        assertWalletBalance(accessToken, destinationWalletId, "0.00");

        ResponseEntity<String> deleteConsumedIncomeResponse = exchangeWithBearer(
                HttpMethod.DELETE,
                "/api/transactions/" + destinationIncomeBody.path("data").path("id").asText(),
                accessToken);
        JsonNode deleteConsumedIncomeBody = readBody(deleteConsumedIncomeResponse);
        assertEquals(HttpStatus.BAD_REQUEST, deleteConsumedIncomeResponse.getStatusCode());
        assertEquals("INSUFFICIENT_BALANCE", deleteConsumedIncomeBody.path("error").path("code").asText());

        ResponseEntity<String> insufficientTransferResponse = exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/transfer",
                accessToken,
                Map.of(
                        "sourceWalletId", sourceWalletId,
                        "destinationWalletId", destinationWalletId,
                        "amount", "999.00",
                        "title", "Too much transfer",
                        "note", "Should fail",
                        "transactionDate", "2026-05-15T16:00:00"));
        JsonNode insufficientTransferBody = readBody(insufficientTransferResponse);
        assertEquals(HttpStatus.BAD_REQUEST, insufficientTransferResponse.getStatusCode());
        assertEquals("INSUFFICIENT_BALANCE", insufficientTransferBody.path("error").path("code").asText());

        ResponseEntity<String> sameWalletTransferResponse = exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/transfer",
                accessToken,
                Map.of(
                        "sourceWalletId", sourceWalletId,
                        "destinationWalletId", sourceWalletId,
                        "amount", "10.00",
                        "title", "Invalid transfer",
                        "note", "Should fail",
                        "transactionDate", "2026-05-15T17:00:00"));
        JsonNode sameWalletTransferBody = readBody(sameWalletTransferResponse);
        assertEquals(HttpStatus.BAD_REQUEST, sameWalletTransferResponse.getStatusCode());
        assertFalse(sameWalletTransferBody.path("success").asBoolean());
    }

    private void assertWalletBalance(String accessToken, String walletId, String expectedBalance) throws Exception {
        ResponseEntity<String> walletResponse = exchangeWithBearer(HttpMethod.GET, "/api/wallets/" + walletId, accessToken);
        JsonNode walletBody = readBody(walletResponse);
        assertEquals(HttpStatus.OK, walletResponse.getStatusCode());
        assertEquals(0, new BigDecimal(expectedBalance).compareTo(walletBody.path("data").path("balance").decimalValue()));
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
