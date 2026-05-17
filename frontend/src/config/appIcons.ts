import type { LucideIcon } from 'lucide-react';
import {
  ArrowLeftRight,
  BadgeDollarSign,
  Bell,
  CalendarRange,
  CreditCard,
  Eye,
  EyeOff,
  HandCoins,
  Landmark,
  LayoutDashboard,
  LogOut,
  PiggyBank,
  ReceiptText,
  Search,
  Settings2,
  ShieldCheck,
  TrendingUp,
  Wallet,
} from 'lucide-react';

export const appIcons = {
  dashboard: LayoutDashboard,
  wallet: Wallet,
  transaction: ArrowLeftRight,
  logout: LogOut,
  money: BadgeDollarSign,
  bank: Landmark,
  saving: PiggyBank,
  card: CreditCard,
  receipt: ReceiptText,
  trend: TrendingUp,
  security: ShieldCheck,
  bell: Bell,
  search: Search,
  settings: Settings2,
  calendar: CalendarRange,
  cashflow: HandCoins,
  eye: Eye,
  eyeOff: EyeOff,
} satisfies Record<string, LucideIcon>;

export type AppIconName = keyof typeof appIcons;
