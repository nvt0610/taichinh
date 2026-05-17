import { create } from 'zustand';
import { createJSONStorage, persist, subscribeWithSelector } from 'zustand/middleware';

import * as authService from '@/services/authService';
import type { NormalizedApiError } from '@/types/api';
import type { AuthUser, LoginRequest, RegisterRequest } from '@/types/auth';
import { configureAuthHandlers, normalizeApiError } from '@/services/apiClient';
import { APP_NAME } from '@/config/app';

type AuthStatus = 'idle' | 'loading';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  tokenType: string;
  expiresIn: number | null;
  refreshExpiresIn: number | null;
  user: AuthUser | null;
  status: AuthStatus;
  error: NormalizedApiError | null;
}

interface AuthActions {
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshAccessToken: () => Promise<string>;
  clearError: () => void;
  clearSession: () => void;
}

export type AuthStore = AuthState & AuthActions;

const initialState: AuthState = {
  accessToken: null,
  refreshToken: null,
  tokenType: 'Bearer',
  expiresIn: null,
  refreshExpiresIn: null,
  user: null,
  status: 'idle',
  error: null,
};

export const useAuthStore = create<AuthStore>()(
  subscribeWithSelector(
    persist(
      (set, get) => ({
        ...initialState,
        login: async (payload) => {
          set({ status: 'loading', error: null });

          try {
            const response = await authService.login(payload);

            set({
              accessToken: response.accessToken,
              refreshToken: response.refreshToken,
              tokenType: response.tokenType,
              expiresIn: response.expiresIn,
              refreshExpiresIn: response.refreshExpiresIn,
              user: {
                id: response.id,
                username: response.username,
                email: response.email,
                roles: response.roles,
              },
              status: 'idle',
              error: null,
            });
          } catch (error) {
            const normalizedError = normalizeApiError(error);
            set({ status: 'idle', error: normalizedError });
            throw normalizedError;
          }
        },
        register: async (payload) => {
          set({ status: 'loading', error: null });

          try {
            await authService.register(payload);
            await get().login({
              usernameOrEmail: payload.email,
              password: payload.password,
            });
          } catch (error) {
            const normalizedError = normalizeApiError(error);
            set({ status: 'idle', error: normalizedError });
            throw normalizedError;
          }
        },
        logout: async () => {
          const token = get().refreshToken;

          try {
            if (token) {
              await authService.logout(token);
            }
          } finally {
            set(initialState);
          }
        },
        refreshAccessToken: async () => {
          const token = get().refreshToken;

          if (!token) {
            throw {
              message: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
              fieldErrors: {},
              status: 401,
            } satisfies NormalizedApiError;
          }

          const response = await authService.refresh(token);

          set({
            accessToken: response.accessToken,
            tokenType: response.tokenType,
            expiresIn: response.expiresIn,
          });

          return response.accessToken;
        },
        clearError: () => set({ error: null }),
        clearSession: () => set(initialState),
      }),
      {
        name: `${APP_NAME.toLowerCase()}-auth`,
        storage: createJSONStorage(() => localStorage),
        partialize: ({
          accessToken,
          refreshToken,
          tokenType,
          expiresIn,
          refreshExpiresIn,
          user,
        }) => ({
          accessToken,
          refreshToken,
          tokenType,
          expiresIn,
          refreshExpiresIn,
          user,
        }),
      },
    ),
  ),
);

export const selectIsAuthenticated = (state: AuthStore) => Boolean(state.accessToken);
export const selectAuthStatus = (state: AuthStore) => state.status;
export const selectAuthError = (state: AuthStore) => state.error;
export const selectAuthUser = (state: AuthStore) => state.user;
export const selectLogin = (state: AuthStore) => state.login;
export const selectRegister = (state: AuthStore) => state.register;
export const selectLogout = (state: AuthStore) => state.logout;
export const selectClearAuthError = (state: AuthStore) => state.clearError;

configureAuthHandlers({
  getAccessToken: () => useAuthStore.getState().accessToken,
  refreshAccessToken: () => useAuthStore.getState().refreshAccessToken(),
  clearSession: () => useAuthStore.getState().clearSession(),
});
