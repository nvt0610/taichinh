import { useEffect } from 'react';
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
  useLocation,
} from 'react-router-dom';

import { AppShell } from '@/components/layout/AppShell';
import { ProtectedRoute } from '@/components/routing/ProtectedRoute';
import { DashboardPage } from '@/pages/DashboardPage';
import { LoginPage } from '@/pages/LoginPage';
import { NotFoundPage } from '@/pages/NotFoundPage';
import { RegisterPage } from '@/pages/RegisterPage';
import { TransactionsPage } from '@/pages/TransactionsPage';
import { WalletsPage } from '@/pages/WalletsPage';
import { selectPrimaryColor, selectThemeId, useThemeStore } from '@/store/themeStore';
import { applyDynamicPalette, applyTheme } from '@/utils/theme';

function ThemeSync() {
  const themeId = useThemeStore(selectThemeId);
  const primaryColor = useThemeStore(selectPrimaryColor);

  useEffect(() => {
    applyTheme(themeId);
  }, [themeId]);

  useEffect(() => {
    applyDynamicPalette(primaryColor);
  }, [primaryColor]);

  return null;
}

function RootRedirect() {
  const location = useLocation();

  return <Navigate replace state={{ from: location }} to="/dashboard" />;
}

export function App() {
  return (
    <BrowserRouter>
      <ThemeSync />
      <Routes>
        <Route element={<LoginPage />} path="/login" />
        <Route element={<RegisterPage />} path="/register" />
        <Route element={<RootRedirect />} path="/" />
        <Route element={<ProtectedRoute />}>
          <Route element={<AppShell />}>
            <Route element={<DashboardPage />} path="/dashboard" />
            <Route element={<WalletsPage />} path="/wallets" />
            <Route element={<TransactionsPage />} path="/transactions" />
          </Route>
        </Route>
        <Route element={<NotFoundPage />} path="*" />
      </Routes>
    </BrowserRouter>
  );
}
