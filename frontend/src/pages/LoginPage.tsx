import { useEffect, useState } from 'react';
import type { MouseEvent } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useLocation, useNavigate } from 'react-router-dom';

import { LoginShowcase } from '@/components/auth/LoginShowcase';
import { AppIcon } from '@/components/ui/AppIcon';
import { APP_NAME } from '@/config/app';
import {
  selectAuthError,
  selectAuthStatus,
  selectClearAuthError,
  selectIsAuthenticated,
  selectLogin,
  useAuthStore,
} from '@/store/authStore';
import type { LoginRequest } from '@/types/auth';

interface RouteState {
  from?: {
    pathname?: string;
  };
  authTransition?: 'from-register';
}

export function LoginPage() {
  const login = useAuthStore(selectLogin);
  const status = useAuthStore(selectAuthStatus);
  const error = useAuthStore(selectAuthError);
  const clearError = useAuthStore(selectClearAuthError);
  const isAuthenticated = useAuthStore(selectIsAuthenticated);
  const location = useLocation();
  const navigate = useNavigate();
  const from = (location.state as RouteState | null)?.from?.pathname ?? '/dashboard';
  const cameFromRegister = (location.state as RouteState | null)?.authTransition === 'from-register';
  const isSubmitting = status === 'loading';
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);

  const {
    formState: { errors },
    handleSubmit,
    register,
  } = useForm<LoginRequest>({
    defaultValues: {
      usernameOrEmail: '',
      password: '',
    },
  });

  useEffect(() => {
    if (isAuthenticated) {
      navigate(from, { replace: true });
    }
  }, [from, isAuthenticated, navigate]);

  useEffect(() => clearError, [clearError]);

  async function onSubmit(values: LoginRequest) {
    await login(values);
    navigate(from, { replace: true });
  }

  function handleRegisterLinkClick(event: MouseEvent<HTMLAnchorElement>) {
    event.preventDefault();
    navigate('/register', {
      state: {
        authTransition: 'from-login',
      },
      viewTransition: true,
    });
  }

  const layoutClassName = [
    'auth-layout',
    cameFromRegister ? 'auth-layout-enter-from-register' : '',
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <main className={layoutClassName} aria-labelledby="login-title">
      <LoginShowcase />

      <section className="auth-panel auth-panel-login">
        <p className="eyebrow">{APP_NAME}</p>
        <h1 id="login-title">Đăng nhập</h1>

        <form className="auth-form" onSubmit={handleSubmit(onSubmit)}>
          <label className="form-field">
            <span>Username hoặc email</span>
            <input
              autoComplete="username"
              aria-invalid={Boolean(errors.usernameOrEmail || error?.fieldErrors.usernameOrEmail)}
              {...register('usernameOrEmail', {
                required: 'Vui lòng nhập username hoặc email.',
                maxLength: {
                  value: 255,
                  message: 'Username hoặc email không được vượt quá 255 ký tự.',
                },
              })}
            />
            <FormError
              message={errors.usernameOrEmail?.message ?? error?.fieldErrors.usernameOrEmail}
            />
          </label>

          <div className="form-field">
            <label htmlFor="login-password">Mật khẩu</label>
            <div className="password-input-wrap">
              <input
                id="login-password"
                autoComplete="current-password"
                aria-invalid={Boolean(errors.password || error?.fieldErrors.password)}
                type={isPasswordVisible ? 'text' : 'password'}
                {...register('password', {
                  required: 'Vui lòng nhập mật khẩu.',
                  maxLength: {
                    value: 100,
                    message: 'Mật khẩu không được vượt quá 100 ký tự.',
                  },
                })}
              />
              <button
                aria-label={isPasswordVisible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                className="password-toggle"
                type="button"
                onClick={() => setIsPasswordVisible((value) => !value)}
              >
                <AppIcon name={isPasswordVisible ? 'eyeOff' : 'eye'} size={18} strokeWidth={2.2} />
              </button>
            </div>
            <FormError message={errors.password?.message ?? error?.fieldErrors.password} />
          </div>

          {error ? <p className="form-alert">{error.message}</p> : null}

          <button className="primary-button" disabled={isSubmitting} type="submit">
            {isSubmitting ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>
        </form>

        <p className="auth-link">
          Chưa có tài khoản?{' '}
          <Link to="/register" onClick={handleRegisterLinkClick}>
            Tạo tài khoản
          </Link>
        </p>
      </section>
    </main>
  );
}

function FormError({ message }: { message?: string }) {
  if (!message) {
    return null;
  }

  return <span className="form-error">{message}</span>;
}
