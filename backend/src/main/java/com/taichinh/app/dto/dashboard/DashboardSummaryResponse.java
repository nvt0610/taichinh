package com.taichinh.app.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DashboardSummaryResponse(
        BigDecimal totalBalance,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netCashFlow,
        LocalDateTime periodStart,
        LocalDateTime periodEnd) {
}
