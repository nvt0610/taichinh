import { apiClient, unwrapResponse } from '@/services/apiClient';
import type { ApiResponse } from '@/types/api';
import type {
  ListTransactionParams,
  Transaction,
  TransferTransactionResponse,
} from '@/types/transaction';

interface CreateIncomeExpensePayload {
  walletId: string;
  categoryId?: string;
  amount: number;
  title: string;
  note: string;
  transactionDate: string;
}

interface CreateTransferPayload {
  sourceWalletId: string;
  destinationWalletId: string;
  amount: number;
  title: string;
  note: string;
  transactionDate: string;
}

export async function listTransactions(params: ListTransactionParams = {}) {
  const response = await apiClient.get<ApiResponse<Transaction[]>>('/transactions', {
    params: {
      page: params.page ?? 1,
      size: params.size ?? 20,
      sort: 'transactionDate,desc',
      q: params.q || undefined,
      walletId: params.walletId || undefined,
      type: params.type || undefined,
    },
  });

  return unwrapResponse(response.data);
}

export async function createIncome(payload: CreateIncomeExpensePayload) {
  const response = await apiClient.post<ApiResponse<Transaction>>(
    '/transactions/income',
    payload,
  );
  return unwrapResponse(response.data);
}

export async function createExpense(payload: CreateIncomeExpensePayload) {
  const response = await apiClient.post<ApiResponse<Transaction>>(
    '/transactions/expense',
    payload,
  );
  return unwrapResponse(response.data);
}

export async function createTransfer(payload: CreateTransferPayload) {
  const response = await apiClient.post<ApiResponse<TransferTransactionResponse>>(
    '/transactions/transfer',
    payload,
  );
  return unwrapResponse(response.data);
}

export async function updateIncome(
  transactionId: string,
  payload: CreateIncomeExpensePayload,
) {
  const response = await apiClient.put<ApiResponse<Transaction>>(
    `/transactions/${transactionId}/income`,
    payload,
  );
  return unwrapResponse(response.data);
}

export async function updateExpense(
  transactionId: string,
  payload: CreateIncomeExpensePayload,
) {
  const response = await apiClient.put<ApiResponse<Transaction>>(
    `/transactions/${transactionId}/expense`,
    payload,
  );
  return unwrapResponse(response.data);
}

export async function updateTransfer(
  transactionId: string,
  payload: CreateTransferPayload,
) {
  const response = await apiClient.put<ApiResponse<TransferTransactionResponse>>(
    `/transactions/${transactionId}/transfer`,
    payload,
  );
  return unwrapResponse(response.data);
}

export async function deleteTransaction(transactionId: string) {
  const response = await apiClient.delete<ApiResponse<null>>(
    `/transactions/${transactionId}`,
  );
  return unwrapResponse(response.data);
}
