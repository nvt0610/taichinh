import { create } from 'zustand';
import { createJSONStorage, persist, subscribeWithSelector } from 'zustand/middleware';

import { defaultThemeId, isThemeId, themes, type ThemeId } from '@/utils/theme';
import { APP_NAME } from '@/config/app';

export type ThemeMode = 'auto' | 'manual';

interface ThemeState {
  themeMode: ThemeMode;
  themeId: ThemeId;
  primaryColor: string;
  customPalette: Record<string, string>;
}

interface ThemeActions {
  setTheme: (themeId: ThemeId) => void;
  setPrimaryColor: (primaryColor: string) => void;
  setThemeMode: (themeMode: ThemeMode) => void;
  resetTheme: () => void;
}

export type ThemeStore = ThemeState & ThemeActions;

const initialState: ThemeState = {
  themeMode: 'auto',
  themeId: defaultThemeId,
  primaryColor: themes[defaultThemeId].primaryColor,
  customPalette: {},
};

export const useThemeStore = create<ThemeStore>()(
  subscribeWithSelector(
    persist(
      (set) => ({
        ...initialState,
        setTheme: (themeId) => set({ themeId, primaryColor: themes[themeId].primaryColor, themeMode: 'auto' }),
        setPrimaryColor: (primaryColor) => set({ primaryColor, themeMode: 'manual' }),
        setThemeMode: (themeMode) =>
          set((state) => ({
            themeMode,
            primaryColor: themeMode === 'auto' ? themes[state.themeId].primaryColor : state.primaryColor,
          })),
        resetTheme: () => set(initialState),
      }),
      {
        name: `${APP_NAME.toLowerCase()}-theme`,
        storage: createJSONStorage(() => localStorage),
        partialize: ({ themeMode, themeId, primaryColor, customPalette }) => ({
          themeMode,
          themeId,
          primaryColor,
          customPalette,
        }),
        merge: (persisted, current) => {
          if (!persisted || typeof persisted !== 'object') {
            return current;
          }

          const saved = persisted as Partial<ThemeState>;

          return {
            ...current,
            ...saved,
            themeId: isThemeId(saved.themeId) ? saved.themeId : defaultThemeId,
            primaryColor: saved.primaryColor ?? current.primaryColor,
            customPalette: saved.customPalette ?? current.customPalette,
            themeMode: saved.themeMode ?? current.themeMode,
          };
        },
      },
    ),
  ),
);

export const selectThemeId = (state: ThemeStore) => state.themeId;
export const selectPrimaryColor = (state: ThemeStore) => state.primaryColor;
export const selectSetTheme = (state: ThemeStore) => state.setTheme;
export const selectSetPrimaryColor = (state: ThemeStore) => state.setPrimaryColor;
export const selectThemeMode = (state: ThemeStore) => state.themeMode;
export const selectSetThemeMode = (state: ThemeStore) => state.setThemeMode;
export const selectResetTheme = (state: ThemeStore) => state.resetTheme;
