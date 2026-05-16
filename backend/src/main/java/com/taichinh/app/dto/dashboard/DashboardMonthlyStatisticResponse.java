package com.taichinh.app.dto.dashboard;

import java.math.BigDecimal;

public record DashboardMonthlyStatisticResponse(
        String month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netCashFlow) {
}
