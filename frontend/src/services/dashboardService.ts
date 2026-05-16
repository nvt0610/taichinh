import { apiClient, unwrapResponse } from '@/services/apiClient';
import type { ApiResponse } from '@/types/api';
import type {
  DashboardMonthlyStatistic,
  DashboardRecentTransaction,
  DashboardSummary,
  DashboardTopSpendingCategory,
} from '@/types/dashboard';

export async function getDashboardSummary() {
  const response = await apiClient.get<ApiResponse<DashboardSummary>>('/dashboard/summary');
  return unwrapResponse(response.data);
}

export async function getRecentTransactions(limit = 5) {
  const response = await apiClient.get<ApiResponse<DashboardRecentTransaction[]>>(
    '/dashboard/recent-transactions',
    {
      params: { limit },
    },
  );
  return unwrapResponse(response.data);
}

export async function getTopSpendingCategories() {
  const response = await apiClient.get<ApiResponse<DashboardTopSpendingCategory[]>>(
    '/dashboard/top-spending-categories',
  );
  return unwrapResponse(response.data);
}

export async function getMonthlyStatistics(months = 6) {
  const response = await apiClient.get<ApiResponse<DashboardMonthlyStatistic[]>>(
    '/dashboard/monthly-statistics',
    {
      params: { months },
    },
  );
  return unwrapResponse(response.data);
}
