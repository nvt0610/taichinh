import { useState } from 'react';

import { themes } from '@/utils/theme';
import {
  selectPrimaryColor,
  selectResetTheme,
  selectSetPrimaryColor,
  selectSetTheme,
  selectThemeId,
  selectThemeMode,
  selectSetThemeMode,
  useThemeStore,
} from '@/store/themeStore';

const themeOptions = Object.values(themes);

export function ThemeQuickSwitch() {
  const themeId = useThemeStore(selectThemeId);
  const setTheme = useThemeStore(selectSetTheme);
  const primaryColor = useThemeStore(selectPrimaryColor);
  const setPrimaryColor = useThemeStore(selectSetPrimaryColor);
  const themeMode = useThemeStore(selectThemeMode);
  const setThemeMode = useThemeStore(selectSetThemeMode);
  const resetTheme = useThemeStore(selectResetTheme);
  const [isPickerOpen, setIsPickerOpen] = useState(false);

  return (
    <div className="theme-picker-shell" data-open={isPickerOpen}>
      <button
        aria-expanded={isPickerOpen}
        className="menu-action-button"
        type="button"
        onClick={() => setIsPickerOpen((value) => !value)}
      >
        <span className="theme-action-icon" aria-hidden="true">
          ◐
        </span>
        <span className="theme-action-label">Chỉnh màu hệ thống</span>
        <span className="theme-action-chevron" aria-hidden="true">
          ▾
        </span>
      </button>

      {isPickerOpen ? (
        <div className="theme-picker" aria-label="Bộ đổi giao diện">
          <div className="theme-picker-row">
            <span>Bảng màu</span>
            <div className="theme-switch">
              {themeOptions.map((theme) => (
                <button
                  aria-label={`Đổi sang theme ${theme.name}`}
                  className="theme-dot"
                  data-active={theme.id === themeId}
                  key={theme.id}
                  onClick={() => setTheme(theme.id)}
                  style={{ background: theme.preview }}
                  type="button"
                  title={theme.primaryColor}
                />
              ))}
            </div>
          </div>

          <div className="theme-picker-row">
            <span>Màu nhấn</span>
            <input
              aria-label="Chọn màu nhấn"
              className="theme-color-input"
              onChange={(event) => setPrimaryColor(event.target.value)}
              type="color"
              value={primaryColor}
            />
          </div>

          <div className="theme-picker-row">
            <span>Cách áp dụng màu</span>
            <div className="segmented">
              <button
                className="segmented-btn"
                data-active={themeMode === 'auto'}
                onClick={() => setThemeMode('auto')}
                type="button"
              >
                Tự động
              </button>
              <button
                className="segmented-btn"
                data-active={themeMode === 'manual'}
                onClick={() => setThemeMode('manual')}
                type="button"
              >
                Tùy chỉnh
              </button>
            </div>
          </div>

          <button className="ghost-button theme-reset-button" onClick={resetTheme} type="button">
            Mặc định
          </button>
        </div>
      ) : null}
    </div>
  );
}
