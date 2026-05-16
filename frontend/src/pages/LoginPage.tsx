import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useLocation, useNavigate } from 'react-router-dom';

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
  const isSubmitting = status === 'loading';

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

  return (
    <main className="auth-layout" aria-labelledby="login-title">
      <section className="auth-panel">
        <p className="eyebrow">Taichinh</p>
        <h1 id="login-title">Đăng nhập</h1>
        <p className="lede">
          Vào dashboard, ví tiền và giao dịch bằng tài khoản backend thật.
        </p>

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

          <label className="form-field">
            <span>Mật khẩu</span>
            <input
              autoComplete="current-password"
              aria-invalid={Boolean(errors.password || error?.fieldErrors.password)}
              type="password"
              {...register('password', {
                required: 'Vui lòng nhập mật khẩu.',
                maxLength: {
                  value: 100,
                  message: 'Mật khẩu không được vượt quá 100 ký tự.',
                },
              })}
            />
            <FormError message={errors.password?.message ?? error?.fieldErrors.password} />
          </label>

          {error ? <p className="form-alert">{error.message}</p> : null}

          <button className="primary-button" disabled={isSubmitting} type="submit">
            {isSubmitting ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>
        </form>

        <p className="auth-link">
          Chưa có tài khoản? <Link to="/register">Tạo tài khoản</Link>
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
