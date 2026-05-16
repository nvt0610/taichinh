import { NavLink, Outlet } from 'react-router-dom';

import { ThemeQuickSwitch } from '@/components/theme/ThemeQuickSwitch';
import { selectAuthUser, selectLogout, useAuthStore } from '@/store/authStore';

const navItems = [
  { label: 'Dashboard', to: '/dashboard' },
  { label: 'Ví tiền', to: '/wallets' },
  { label: 'Giao dịch', to: '/transactions' },
] as const;

export function AppShell() {
  const logout = useAuthStore(selectLogout);
  const user = useAuthStore(selectAuthUser);

  return (
    <div className="app-layout">
      <aside className="sidebar" aria-label="Điều hướng chính">
        <NavLink className="brand" to="/dashboard">
          <span className="brand-mark" aria-hidden="true">
            T
          </span>
          <span>Taichinh</span>
        </NavLink>

        <nav className="sidebar-nav">
          {navItems.map((item) => (
            <NavLink className="nav-link" key={item.to} to={item.to}>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="workspace">
        <header className="topbar">
          <div>
            <p className="topbar-kicker">Minimalist Finance Workspace</p>
            <h1>
              {user ? `Xin chào, ${user.username}` : 'Không gian quản lý tài chính'}
            </h1>
          </div>

          <div className="topbar-actions">
            <ThemeQuickSwitch />
            <button className="ghost-button" onClick={() => void logout()} type="button">
              Đăng xuất
            </button>
          </div>
        </header>

        <main className="content-shell">
          <Outlet />
        </main>

        <nav className="bottom-nav" aria-label="Điều hướng mobile">
          {navItems.map((item) => (
            <NavLink className="bottom-nav-link" key={item.to} to={item.to}>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </div>
    </div>
  );
}
