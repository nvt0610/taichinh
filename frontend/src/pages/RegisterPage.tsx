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
  selectRegister,
  useAuthStore,
} from '@/store/authStore';
import type { RegisterRequest } from '@/types/auth';

interface RouteState {
  authTransition?: 'from-login';
}

interface RegisterFormValues extends RegisterRequest {
  confirmPassword: string;
  acceptedTerms: boolean;
}

export function RegisterPage() {
  const registerAccount = useAuthStore(selectRegister);
  const status = useAuthStore(selectAuthStatus);
  const error = useAuthStore(selectAuthError);
  const clearError = useAuthStore(selectClearAuthError);
  const location = useLocation();
  const navigate = useNavigate();
  const cameFromLogin = (location.state as RouteState | null)?.authTransition === 'from-login';
  const isSubmitting = status === 'loading';
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isConfirmPasswordVisible, setIsConfirmPasswordVisible] = useState(false);

  const {
    formState: { errors },
    handleSubmit,
    register,
    watch,
  } = useForm<RegisterFormValues>({
    defaultValues: {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      acceptedTerms: false,
    },
  });

  const password = watch('password');

  useEffect(() => clearError, [clearError]);

  async function onSubmit(values: RegisterFormValues) {
    const payload = {
      email: values.email,
      password: values.password,
      username: values.username,
    };

    await registerAccount(payload);
    navigate('/dashboard', { replace: true });
  }

  function startSwitchToLogin() {
    navigate('/login', {
      state: {
        authTransition: 'from-register',
      },
      viewTransition: true,
    });
  }

  function handleLoginLinkClick(event: MouseEvent<HTMLAnchorElement>) {
    event.preventDefault();
    startSwitchToLogin();
  }

  const layoutClassName = [
    'auth-layout',
    'auth-layout-register',
    cameFromLogin ? 'auth-layout-enter-from-login' : '',
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <main className={layoutClassName} aria-labelledby="register-title">
      <LoginShowcase />

      <section className="auth-panel auth-panel-register">
        <button
          aria-label="Quay lại đăng nhập"
          className="auth-close-button"
          type="button"
          onClick={startSwitchToLogin}
        >
          ×
        </button>

        <p className="eyebrow">{APP_NAME}</p>
        <h1 id="register-title">Tạo tài khoản</h1>

        <form className="auth-form" onSubmit={handleSubmit(onSubmit)}>
          <label className="form-field">
            <span>Username</span>
            <input
              autoComplete="username"
              aria-invalid={Boolean(errors.username || error?.fieldErrors.username)}
              {...register('username', {
                required: 'Vui lòng nhập username.',
                minLength: {
                  value: 3,
                  message: 'Username cần ít nhất 3 ký tự.',
                },
                maxLength: {
                  value: 50,
                  message: 'Username không được vượt quá 50 ký tự.',
                },
                pattern: {
                  value: /^[a-zA-Z0-9._-]+$/,
                  message: 'Username chỉ gồm chữ, số, dấu chấm, gạch dưới và gạch nối.',
                },
              })}
            />
            <FormError message={errors.username?.message ?? error?.fieldErrors.username} />
          </label>

          <label className="form-field">
            <span>Email</span>
            <input
              autoComplete="email"
              aria-invalid={Boolean(errors.email || error?.fieldErrors.email)}
              type="email"
              {...register('email', {
                required: 'Vui lòng nhập email.',
                maxLength: {
                  value: 255,
                  message: 'Email không được vượt quá 255 ký tự.',
                },
                pattern: {
                  value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                  message: 'Email không hợp lệ.',
                },
              })}
            />
            <FormError message={errors.email?.message ?? error?.fieldErrors.email} />
          </label>

          <div className="form-field">
            <label htmlFor="register-password">Mật khẩu</label>
            <div className="password-input-wrap">
              <input
                id="register-password"
                autoComplete="new-password"
                aria-invalid={Boolean(errors.password || error?.fieldErrors.password)}
                type={isPasswordVisible ? 'text' : 'password'}
                {...register('password', {
                  required: 'Vui lòng nhập mật khẩu.',
                  minLength: {
                    value: 8,
                    message: 'Mật khẩu cần ít nhất 8 ký tự.',
                  },
                  maxLength: {
                    value: 100,
                    message: 'Mật khẩu không được vượt quá 100 ký tự.',
                  },
                  pattern: {
                    value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/,
                    message: 'Mật khẩu cần có chữ hoa, chữ thường và số.',
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

          <div className="form-field">
            <label htmlFor="register-confirm-password">Xác nhận mật khẩu</label>
            <div className="password-input-wrap">
              <input
                id="register-confirm-password"
                autoComplete="new-password"
                aria-invalid={Boolean(errors.confirmPassword)}
                type={isConfirmPasswordVisible ? 'text' : 'password'}
                {...register('confirmPassword', {
                  required: 'Vui lòng xác nhận mật khẩu.',
                  validate: (value) => value === password || 'Mật khẩu xác nhận chưa khớp.',
                })}
              />
              <button
                aria-label={
                  isConfirmPasswordVisible
                    ? 'Ẩn mật khẩu xác nhận'
                    : 'Hiện mật khẩu xác nhận'
                }
                className="password-toggle"
                type="button"
                onClick={() => setIsConfirmPasswordVisible((value) => !value)}
              >
                <AppIcon
                  name={isConfirmPasswordVisible ? 'eyeOff' : 'eye'}
                  size={18}
                  strokeWidth={2.2}
                />
              </button>
            </div>
            <FormError message={errors.confirmPassword?.message} />
          </div>

          <label className="terms-check">
            <input
              type="checkbox"
              {...register('acceptedTerms', {
                required: 'Bạn cần đồng ý điều khoản sử dụng để tạo tài khoản.',
              })}
            />
            <span>Tôi đồng ý với điều khoản sử dụng của {APP_NAME}.</span>
          </label>
          <FormError message={errors.acceptedTerms?.message} />

          {error ? <p className="form-alert">{error.message}</p> : null}

          <button className="primary-button" disabled={isSubmitting} type="submit">
            {isSubmitting ? 'Đang tạo tài khoản...' : 'Tạo tài khoản'}
          </button>
        </form>

        <p className="auth-link">
          Đã có tài khoản?{' '}
          <Link to="/login" onClick={handleLoginLinkClick}>
            Đăng nhập
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
