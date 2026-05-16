import { apiClient, unwrapResponse } from '@/services/apiClient';
import type { ApiResponse } from '@/types/api';
import type {
  CreateWalletPayload,
  UpdateWalletPayload,
  Wallet,
} from '@/types/wallet';

export async function listWallets() {
  return listWalletsByPage(1, 50);
}

export async function listWalletsByPage(uiPage: number, size = 20) {
  const safeUiPage = Number.isFinite(uiPage) && uiPage > 0 ? Math.floor(uiPage) : 1;
  const safeSize = Number.isFinite(size) && size > 0 ? Math.floor(size) : 20;

  const response = await apiClient.get<ApiResponse<Wallet[]>>('/wallets', {
    params: {
      page: safeUiPage,
      size: safeSize,
      sort: 'createdAt,desc',
    },
  });

  return unwrapResponse(response.data);
}

export async function createWallet(payload: CreateWalletPayload) {
  const response = await apiClient.post<ApiResponse<Wallet>>('/wallets', payload);
  return unwrapResponse(response.data);
}

export async function updateWallet(walletId: string, payload: UpdateWalletPayload) {
  const response = await apiClient.put<ApiResponse<Wallet>>(
    `/wallets/${walletId}`,
    payload,
  );
  return unwrapResponse(response.data);
}

export async function deleteWallet(walletId: string) {
  const response = await apiClient.delete<ApiResponse<null>>(`/wallets/${walletId}`);
  return unwrapResponse(response.data);
}
