import { apiClient, unwrapResponse } from '@/services/apiClient';
import type { ApiResponse } from '@/types/api';
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  TokenResponse,
} from '@/types/auth';

export async function register(payload: RegisterRequest) {
  const response = await apiClient.post<ApiResponse<RegisterResponse>>(
    '/auth/register',
    payload,
    { skipAuthRefresh: true },
  );

  return unwrapResponse(response.data);
}

export async function login(payload: LoginRequest) {
  const response = await apiClient.post<ApiResponse<LoginResponse>>(
    '/auth/login',
    payload,
    { skipAuthRefresh: true },
  );

  return unwrapResponse(response.data);
}

export async function refresh(refreshToken: string) {
  const response = await apiClient.post<ApiResponse<TokenResponse>>(
    '/auth/refresh',
    { refreshToken },
    { skipAuthRefresh: true },
  );

  return unwrapResponse(response.data);
}

export async function logout(refreshToken: string) {
  const response = await apiClient.post<ApiResponse<null>>(
    '/auth/logout',
    { refreshToken },
    { skipAuthRefresh: true },
  );

  return unwrapResponse(response.data);
}
