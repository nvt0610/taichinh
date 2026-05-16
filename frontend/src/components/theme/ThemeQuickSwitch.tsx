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

  return (
    <div className="theme-picker" aria-label="Theme picker">
      <div className="theme-picker-row">
        <span>Preset</span>
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
            />
          ))}
        </div>
      </div>

      <div className="theme-picker-row">
        <span>Màu chủ đạo</span>
        <input
          aria-label="Chọn màu chủ đạo"
          className="theme-color-input"
          onChange={(event) => setPrimaryColor(event.target.value)}
          type="color"
          value={primaryColor}
        />
      </div>

      <div className="theme-picker-row">
        <span>Chế độ</span>
        <div className="segmented">
          <button
            className="segmented-btn"
            data-active={themeMode === 'auto'}
            onClick={() => setThemeMode('auto')}
            type="button"
          >
            Auto
          </button>
          <button
            className="segmented-btn"
            data-active={themeMode === 'manual'}
            onClick={() => setThemeMode('manual')}
            type="button"
          >
            Manual
          </button>
        </div>
      </div>

      <button className="ghost-button" onClick={resetTheme} type="button">
        Reset to Recommended
      </button>
    </div>
  );
}
