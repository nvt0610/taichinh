import { apiClient, unwrapResponse } from '@/services/apiClient';
import type { ApiResponse } from '@/types/api';
import type { Category } from '@/types/transaction';

export async function listCategories(type?: 'INCOME' | 'EXPENSE') {
  const response = await apiClient.get<ApiResponse<Category[]>>('/categories', {
    params: {
      page: 1,
      size: 100,
      sort: 'name,asc',
      type,
    },
  });

  return unwrapResponse(response.data);
}
