package com.taichinh.app.dto.dashboard;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class DashboardMonthlyStatisticsQueryParams {

    private static final int DEFAULT_MONTHS = 6;
    private static final int MAX_MONTHS = 12;

    @Min(1)
    @Max(MAX_MONTHS)
    private int months = DEFAULT_MONTHS;

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }
}
