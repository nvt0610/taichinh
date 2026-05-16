package com.taichinh.app.dto.dashboard;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class DashboardRecentTransactionsQueryParams {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;

    @Min(1)
    @Max(MAX_LIMIT)
    private int limit = DEFAULT_LIMIT;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
