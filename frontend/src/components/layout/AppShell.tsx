import { useMemo, useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';

import { ThemeQuickSwitch } from '@/components/theme/ThemeQuickSwitch';
import { AppIcon } from '@/components/ui/AppIcon';
import { APP_NAME } from '@/config/app';
import { selectAuthUser, selectLogout, useAuthStore } from '@/store/authStore';

const navItems = [
  { label: 'Dashboard', to: '/dashboard', icon: 'dashboard' },
  { label: 'Ví tiền', to: '/wallets', icon: 'wallet' },
  { label: 'Giao dịch', to: '/transactions', icon: 'transaction' },
] as const;

export function AppShell() {
  const logout = useAuthStore(selectLogout);
  const user = useAuthStore(selectAuthUser);
  const displayName = user?.username ?? 'người dùng';
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const todayLabel = useMemo(
    () =>
      new Intl.DateTimeFormat('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
      }).format(new Date()),
    [],
  );

  return (
    <div className="app-layout" data-sidebar-collapsed={isSidebarCollapsed}>
      <aside className="sidebar" aria-label="Điều hướng chính">
        <NavLink className="brand" to="/dashboard">
          <span className="brand-mark" aria-hidden="true">
            <img alt="" src="/logo_notext-no-bg.png" />
          </span>
          <span className="brand-text">{APP_NAME}</span>
        </NavLink>

        <button
          aria-expanded={!isSidebarCollapsed}
          aria-label={isSidebarCollapsed ? 'Mở rộng sidebar' : 'Thu nhỏ sidebar'}
          className="sidebar-toggle"
          type="button"
          onClick={() => setIsSidebarCollapsed((value) => !value)}
        >
          <span aria-hidden="true">{isSidebarCollapsed ? '›' : '‹'}</span>
        </button>

        <nav className="sidebar-nav">
          {navItems.map((item) => (
            <NavLink className="nav-link" key={item.to} title={item.label} to={item.to}>
              <AppIcon className="nav-link-icon" name={item.icon} size={18} strokeWidth={2.1} />
              <span className="nav-link-label">{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <ThemeQuickSwitch />
          <button className="menu-action-button logout-button" onClick={() => void logout()} type="button">
            <AppIcon className="button-icon" name="logout" size={16} strokeWidth={2.1} />
            <span className="sidebar-action-label">Đăng xuất</span>
          </button>
        </div>
      </aside>

      <div className="workspace">
        <header className="topbar">
          <div className="topbar-greeting">
            <span>Xin chào</span>
            <strong>{displayName}</strong>
          </div>
          <div className="topbar-chip">
            <span>Hôm nay</span>
            <strong>{todayLabel}</strong>
          </div>
        </header>

        <main className="content-shell">
          <Outlet />
        </main>

        <nav className="bottom-nav" aria-label="Điều hướng mobile">
          {navItems.map((item) => (
            <NavLink className="bottom-nav-link" key={item.to} to={item.to}>
              <AppIcon className="bottom-nav-icon" name={item.icon} size={16} strokeWidth={2.1} />
              {item.label}
            </NavLink>
          ))}
        </nav>
      </div>
    </div>
  );
}
