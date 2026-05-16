import axios, {
  AxiosError,
  type InternalAxiosRequestConfig,
} from 'axios';

import type { ApiResponse, FieldError, NormalizedApiError } from '@/types/api';

declare module 'axios' {
  export interface AxiosRequestConfig {
    skipAuthRefresh?: boolean;
  }
}

interface RetriableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
  skipAuthRefresh?: boolean;
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api';

let getAccessToken: () => string | null = () => null;
let refreshAccessToken: () => Promise<string> = async () => {
  throw {
    message: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
    fieldErrors: {},
    status: 401,
  } satisfies NormalizedApiError;
};
let clearSession: () => void = () => undefined;

export const apiClient = axios.create({
  baseURL: apiBaseUrl,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = getAccessToken();

  if (token && !config.headers.Authorization) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const originalRequest = error.config as RetriableRequestConfig | undefined;

    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      !originalRequest.skipAuthRefresh
    ) {
      originalRequest._retry = true;

      try {
        const token = await refreshAccessToken();
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        clearSession();
        return Promise.reject(normalizeApiError(refreshError));
      }
    }

    return Promise.reject(normalizeApiError(error));
  },
);

export function configureAuthHandlers(handlers: {
  getAccessToken: () => string | null;
  refreshAccessToken: () => Promise<string>;
  clearSession: () => void;
}) {
  getAccessToken = handlers.getAccessToken;
  refreshAccessToken = handlers.refreshAccessToken;
  clearSession = handlers.clearSession;
}

export function unwrapResponse<T>(response: ApiResponse<T>): T {
  if (!response.success) {
    throw {
      message: response.message,
      code: response.error?.code,
      fieldErrors: toFieldErrorMap(response.error?.details),
    } satisfies NormalizedApiError;
  }

  return response.data;
}

export function normalizeApiError(error: unknown): NormalizedApiError {
  if (axios.isAxiosError<ApiResponse<unknown>>(error)) {
    const data = error.response?.data;

    return {
      message: data?.message ?? error.message ?? 'Không thể kết nối máy chủ.',
      code: data?.error?.code,
      fieldErrors: toFieldErrorMap(data?.error?.details),
      status: error.response?.status,
    };
  }

  if (isNormalizedApiError(error)) {
    return error;
  }

  return {
    message: 'Đã có lỗi không xác định. Vui lòng thử lại.',
    fieldErrors: {},
  };
}

function toFieldErrorMap(details?: FieldError[]): Record<string, string> {
  if (!Array.isArray(details)) {
    return {};
  }

  return details.reduce<Record<string, string>>((acc, item) => {
    if (item.field && item.message) {
      acc[item.field] = item.message;
    }

    return acc;
  }, {});
}

function isNormalizedApiError(error: unknown): error is NormalizedApiError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'message' in error &&
    'fieldErrors' in error
  );
}
