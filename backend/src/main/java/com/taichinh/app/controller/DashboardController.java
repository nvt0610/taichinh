package com.taichinh.app.controller;

import com.taichinh.app.dto.common.ApiResponse;
import com.taichinh.app.dto.dashboard.DashboardSummaryQueryParams;
import com.taichinh.app.dto.dashboard.DashboardSummaryResponse;
import com.taichinh.app.dto.dashboard.DashboardRecentTransactionResponse;
import com.taichinh.app.dto.dashboard.DashboardRecentTransactionsQueryParams;
import com.taichinh.app.dto.dashboard.DashboardTopSpendingCategoryResponse;
import com.taichinh.app.dto.dashboard.DashboardMonthlyStatisticResponse;
import com.taichinh.app.dto.dashboard.DashboardMonthlyStatisticsQueryParams;
import com.taichinh.app.security.AuthenticatedUserProvider;
import com.taichinh.app.service.DashboardService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public DashboardController(
            DashboardService dashboardService,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.dashboardService = dashboardService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            Authentication authentication,
            @Valid @ModelAttribute DashboardSummaryQueryParams queryParams) {
        DashboardSummaryResponse response = dashboardService.getSummary(
                authenticatedUserProvider.getUserId(authentication),
                queryParams);
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved successfully.", response));
    }

    @GetMapping("/recent-transactions")
    public ResponseEntity<ApiResponse<List<DashboardRecentTransactionResponse>>> getRecentTransactions(
            Authentication authentication,
            @Valid @ModelAttribute DashboardRecentTransactionsQueryParams queryParams) {
        List<DashboardRecentTransactionResponse> response = dashboardService.getRecentTransactions(
                authenticatedUserProvider.getUserId(authentication),
                queryParams);
        return ResponseEntity.ok(ApiResponse.success("Recent transactions retrieved successfully.", response));
    }

    @GetMapping("/top-spending-categories")
    public ResponseEntity<ApiResponse<List<DashboardTopSpendingCategoryResponse>>> getTopSpendingCategories(
            Authentication authentication,
            @Valid @ModelAttribute DashboardSummaryQueryParams queryParams) {
        List<DashboardTopSpendingCategoryResponse> response = dashboardService.getTopSpendingCategories(
                authenticatedUserProvider.getUserId(authentication),
                queryParams);
        return ResponseEntity.ok(ApiResponse.success("Top spending categories retrieved successfully.", response));
    }

    @GetMapping("/monthly-statistics")
    public ResponseEntity<ApiResponse<List<DashboardMonthlyStatisticResponse>>> getMonthlyStatistics(
            Authentication authentication,
            @Valid @ModelAttribute DashboardMonthlyStatisticsQueryParams queryParams) {
        List<DashboardMonthlyStatisticResponse> response = dashboardService.getMonthlyStatistics(
                authenticatedUserProvider.getUserId(authentication),
                queryParams);
        return ResponseEntity.ok(ApiResponse.success("Monthly statistics retrieved successfully.", response));
    }
}
