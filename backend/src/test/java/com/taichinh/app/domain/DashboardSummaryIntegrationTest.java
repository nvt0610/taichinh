package com.taichinh.app.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class DashboardSummaryIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void dashboardSummaryReturnsTotalBalanceAndIncomeExpenseSummary() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String username = "dash" + suffix.substring(0, 20);
        String email = "dash" + suffix + "@example.com";
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

        String walletId = readBody(exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/wallets",
                accessToken,
                Map.of(
                        "name", "Main",
                        "type", "BANK",
                        "balance", "100.00",
                        "description", "Main wallet")))
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

        exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/income",
                accessToken,
                Map.of(
                        "walletId", walletId,
                        "categoryId", incomeCategoryId,
                        "amount", "500.00",
                        "title", "Monthly salary",
                        "note", "May income",
                        "transactionDate", "2026-05-10T08:00:00"));

        exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/expense",
                accessToken,
                Map.of(
                        "walletId", walletId,
                        "categoryId", expenseCategoryId,
                        "amount", "120.00",
                        "title", "Groceries",
                        "note", "May food",
                        "transactionDate", "2026-05-11T12:00:00"));

        exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/income",
                accessToken,
                Map.of(
                        "walletId", walletId,
                        "categoryId", incomeCategoryId,
                        "amount", "999.00",
                        "title", "Old income",
                        "note", "Outside range",
                        "transactionDate", "2026-04-01T08:00:00"));

        ResponseEntity<String> summaryResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/dashboard/summary?startDate=2026-05-01T00:00:00&endDate=2026-05-31T23:59:59",
                accessToken);
        JsonNode summaryBody = readBody(summaryResponse);
        assertEquals(HttpStatus.OK, summaryResponse.getStatusCode());
        assertEquals(0, new BigDecimal("1479.00").compareTo(summaryBody.path("data").path("totalBalance").decimalValue()));
        assertEquals(0, new BigDecimal("500.00").compareTo(summaryBody.path("data").path("totalIncome").decimalValue()));
        assertEquals(0, new BigDecimal("120.00").compareTo(summaryBody.path("data").path("totalExpense").decimalValue()));
        assertEquals(0, new BigDecimal("380.00").compareTo(summaryBody.path("data").path("netCashFlow").decimalValue()));

        ResponseEntity<String> invalidRangeResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/dashboard/summary?startDate=2026-05-31T23:59:59&endDate=2026-05-01T00:00:00",
                accessToken);
        JsonNode invalidRangeBody = readBody(invalidRangeResponse);
        assertEquals(HttpStatus.BAD_REQUEST, invalidRangeResponse.getStatusCode());
        assertEquals("BAD_REQUEST", invalidRangeBody.path("error").path("code").asText());
    }

    @Test
    void dashboardRecentTransactionsTopSpendingAndMonthlyStatisticsReturnExpectedData() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String username = "dashx" + suffix.substring(0, 19);
        String email = "dashx" + suffix + "@example.com";
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

        String cashWalletId = readBody(exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/wallets",
                accessToken,
                Map.of(
                        "name", "Cash",
                        "type", "CASH",
                        "balance", "0.00",
                        "description", "Cash wallet")))
                .path("data").path("id").asText();

        String bankWalletId = readBody(exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/wallets",
                accessToken,
                Map.of(
                        "name", "Bank",
                        "type", "BANK",
                        "balance", "0.00",
                        "description", "Bank wallet")))
                .path("data").path("id").asText();

        String salaryCategoryId = readBody(exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/categories",
                accessToken,
                Map.of(
                        "name", "Salary",
                        "type", "INCOME",
                        "icon", "briefcase",
                        "color", "#00AA55")))
                .path("data").path("id").asText();

        String foodCategoryId = readBody(exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/categories",
                accessToken,
                Map.of(
                        "name", "Food",
                        "type", "EXPENSE",
                        "icon", "utensils",
                        "color", "#FF6600")))
                .path("data").path("id").asText();

        String transportCategoryId = readBody(exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/categories",
                accessToken,
                Map.of(
                        "name", "Transport",
                        "type", "EXPENSE",
                        "icon", "bus",
                        "color", "#0055FF")))
                .path("data").path("id").asText();

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime currentMonthDay5 = currentMonth.atDay(5).atTime(9, 0);
        LocalDateTime currentMonthDay10 = currentMonth.atDay(10).atTime(12, 0);
        LocalDateTime currentMonthDay12 = currentMonth.atDay(12).atTime(18, 0);
        YearMonth previousMonth = currentMonth.minusMonths(1);
        LocalDateTime previousMonthDay20 = previousMonth.atDay(Math.min(20, previousMonth.lengthOfMonth())).atTime(8, 30);

        exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/income",
                accessToken,
                Map.of(
                        "walletId", cashWalletId,
                        "categoryId", salaryCategoryId,
                        "amount", "1000.00",
                        "title", "Salary",
                        "note", "Main income",
                        "transactionDate", currentMonthDay5.toString()));

        exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/expense",
                accessToken,
                Map.of(
                        "walletId", cashWalletId,
                        "categoryId", foodCategoryId,
                        "amount", "200.00",
                        "title", "Groceries",
                        "note", "Food expense",
                        "transactionDate", currentMonthDay10.toString()));

        exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/expense",
                accessToken,
                Map.of(
                        "walletId", cashWalletId,
                        "categoryId", transportCategoryId,
                        "amount", "80.00",
                        "title", "Bus card",
                        "note", "Transport expense",
                        "transactionDate", currentMonthDay12.toString()));

        exchangeJsonWithBearer(
                HttpMethod.POST,
                "/api/transactions/income",
                accessToken,
                Map.of(
                        "walletId", bankWalletId,
                        "categoryId", salaryCategoryId,
                        "amount", "300.00",
                        "title", "Side income",
                        "note", "Previous month income",
                        "transactionDate", previousMonthDay20.toString()));

        ResponseEntity<String> recentTransactionsResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/dashboard/recent-transactions?limit=2",
                accessToken);
        JsonNode recentTransactionsBody = readBody(recentTransactionsResponse);
        assertEquals(HttpStatus.OK, recentTransactionsResponse.getStatusCode());
        assertEquals(2, recentTransactionsBody.path("data").size());
        assertEquals("Bus card", recentTransactionsBody.path("data").get(0).path("title").asText());
        assertEquals("Cash", recentTransactionsBody.path("data").get(0).path("walletName").asText());
        assertEquals("Transport", recentTransactionsBody.path("data").get(0).path("categoryName").asText());
        assertEquals("Groceries", recentTransactionsBody.path("data").get(1).path("title").asText());

        LocalDate currentMonthStart = currentMonth.atDay(1);
        LocalDate currentMonthEnd = currentMonth.atEndOfMonth();
        ResponseEntity<String> topSpendingResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/dashboard/top-spending-categories?startDate=" + currentMonthStart.atStartOfDay()
                        + "&endDate=" + currentMonthEnd.atTime(23, 59, 59),
                accessToken);
        JsonNode topSpendingBody = readBody(topSpendingResponse);
        assertEquals(HttpStatus.OK, topSpendingResponse.getStatusCode());
        assertEquals(2, topSpendingBody.path("data").size());
        assertEquals("Food", topSpendingBody.path("data").get(0).path("categoryName").asText());
        assertEquals(0, new BigDecimal("200.00").compareTo(
                topSpendingBody.path("data").get(0).path("totalAmount").decimalValue()));
        assertEquals("Transport", topSpendingBody.path("data").get(1).path("categoryName").asText());
        assertEquals(0, new BigDecimal("80.00").compareTo(
                topSpendingBody.path("data").get(1).path("totalAmount").decimalValue()));

        ResponseEntity<String> monthlyStatisticsResponse = exchangeWithBearer(
                HttpMethod.GET,
                "/api/dashboard/monthly-statistics?months=2",
                accessToken);
        JsonNode monthlyStatisticsBody = readBody(monthlyStatisticsResponse);
        assertEquals(HttpStatus.OK, monthlyStatisticsResponse.getStatusCode());
        assertEquals(2, monthlyStatisticsBody.path("data").size());
        assertEquals(previousMonth.toString(), monthlyStatisticsBody.path("data").get(0).path("month").asText());
        assertEquals(0, new BigDecimal("300.00").compareTo(
                monthlyStatisticsBody.path("data").get(0).path("totalIncome").decimalValue()));
        assertEquals(currentMonth.toString(), monthlyStatisticsBody.path("data").get(1).path("month").asText());
        assertEquals(0, new BigDecimal("1000.00").compareTo(
                monthlyStatisticsBody.path("data").get(1).path("totalIncome").decimalValue()));
        assertEquals(0, new BigDecimal("280.00").compareTo(
                monthlyStatisticsBody.path("data").get(1).path("totalExpense").decimalValue()));
        assertEquals(0, new BigDecimal("720.00").compareTo(
                monthlyStatisticsBody.path("data").get(1).path("netCashFlow").decimalValue()));
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
