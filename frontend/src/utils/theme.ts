export const themes = {
  'minimal-mint': {
    id: 'minimal-mint',
    name: 'Mint',
    preview: 'linear-gradient(135deg, #1f9d7a, #d9f4ec)',
  },
  'minimal-graphite': {
    id: 'minimal-graphite',
    name: 'Graphite',
    preview: 'linear-gradient(135deg, #30343b, #d9dde5)',
  },
  'minimal-sunrise': {
    id: 'minimal-sunrise',
    name: 'Sunrise',
    preview: 'linear-gradient(135deg, #d97706, #ffedd5)',
  },
} as const;

export type ThemeId = keyof typeof themes;

export const defaultThemeId: ThemeId = 'minimal-mint';

export function isThemeId(value: unknown): value is ThemeId {
  return typeof value === 'string' && value in themes;
}

export function applyTheme(themeId: ThemeId) {
  document.documentElement.dataset.theme = themeId;
}

function clamp(value: number, min: number, max: number) {
  return Math.min(max, Math.max(min, value));
}

function hexToRgb(hex: string) {
  const cleaned = hex.replace('#', '').trim();
  if (!/^[0-9a-fA-F]{6}$/.test(cleaned)) {
    return null;
  }
  const value = Number.parseInt(cleaned, 16);
  return {
    r: (value >> 16) & 255,
    g: (value >> 8) & 255,
    b: value & 255,
  };
}

function rgbToHex(r: number, g: number, b: number) {
  const toHex = (channel: number) => clamp(Math.round(channel), 0, 255).toString(16).padStart(2, '0');
  return `#${toHex(r)}${toHex(g)}${toHex(b)}`;
}

function mixHex(baseHex: string, targetHex: string, ratio: number) {
  const base = hexToRgb(baseHex);
  const target = hexToRgb(targetHex);
  if (!base || !target) {
    return baseHex;
  }
  const r = base.r + (target.r - base.r) * ratio;
  const g = base.g + (target.g - base.g) * ratio;
  const b = base.b + (target.b - base.b) * ratio;
  return rgbToHex(r, g, b);
}

function getReadableTextColor(background: string) {
  const rgb = hexToRgb(background);
  if (!rgb) {
    return '#ffffff';
  }
  const luminance = (0.2126 * rgb.r + 0.7152 * rgb.g + 0.0722 * rgb.b) / 255;
  return luminance > 0.6 ? '#1b1f24' : '#ffffff';
}

function getLuminance(hex: string) {
  const rgb = hexToRgb(hex);
  if (!rgb) {
    return 0.5;
  }
  return (0.2126 * rgb.r + 0.7152 * rgb.g + 0.0722 * rgb.b) / 255;
}

function normalizePrimary(primaryColor: string) {
  const candidate = hexToRgb(primaryColor) ? primaryColor : '#1f9d7a';
  const luminance = getLuminance(candidate);
  if (luminance > 0.92) {
    return mixHex(candidate, '#7d7d7d', 0.34);
  }
  if (luminance < 0.08) {
    return mixHex(candidate, '#3d4a57', 0.32);
  }
  return candidate;
}

export interface ThemePalette {
  primary: string;
  primaryContrast: string;
  surfaceMuted: string;
  income: string;
  expense: string;
  transfer: string;
}

export function generatePalette(primaryColor: string): ThemePalette {
  const primary = normalizePrimary(primaryColor);
  return {
    primary,
    primaryContrast: getReadableTextColor(primary),
    surfaceMuted: mixHex(primary, '#ffffff', 0.9),
    income: mixHex(primary, '#0b6b53', 0.35),
    expense: mixHex(primary, '#df5c50', 0.8),
    transfer: mixHex(primary, '#3778bf', 0.82),
  };
}

export function applyDynamicPalette(primaryColor: string) {
  const palette = generatePalette(primaryColor);
  const root = document.documentElement;
  root.style.setProperty('--primary', palette.primary);
  root.style.setProperty('--primary-contrast', palette.primaryContrast);
  root.style.setProperty('--surface-muted', palette.surfaceMuted);
  root.style.setProperty('--income', palette.income);
  root.style.setProperty('--expense', palette.expense);
  root.style.setProperty('--transfer', palette.transfer);
  root.style.setProperty('--success', palette.income);
  root.style.setProperty('--danger', palette.expense);
  root.style.setProperty('--button-primary-bg', palette.primary);
  root.style.setProperty('--button-primary-fg', palette.primaryContrast);
  root.style.setProperty('--chart-income', palette.income);
  root.style.setProperty('--chart-expense', palette.expense);
  root.style.setProperty('--chart-transfer', palette.transfer);
}
