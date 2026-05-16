import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';

import {
  selectAuthError,
  selectAuthStatus,
  selectClearAuthError,
  selectRegister,
  useAuthStore,
} from '@/store/authStore';
import type { RegisterRequest } from '@/types/auth';

export function RegisterPage() {
  const registerAccount = useAuthStore(selectRegister);
  const status = useAuthStore(selectAuthStatus);
  const error = useAuthStore(selectAuthError);
  const clearError = useAuthStore(selectClearAuthError);
  const navigate = useNavigate();
  const isSubmitting = status === 'loading';

  const {
    formState: { errors },
    handleSubmit,
    register,
  } = useForm<RegisterRequest>({
    defaultValues: {
      username: '',
      email: '',
      password: '',
    },
  });

  useEffect(() => clearError, [clearError]);

  async function onSubmit(values: RegisterRequest) {
    await registerAccount(values);
    navigate('/dashboard', { replace: true });
  }

  return (
    <main className="auth-layout" aria-labelledby="register-title">
      <section className="auth-panel">
        <p className="eyebrow">Taichinh</p>
        <h1 id="register-title">Tạo tài khoản</h1>
        <p className="lede">
          Tài khoản mới sẽ được tạo qua backend thật và tự đăng nhập sau khi
          đăng ký thành công.
        </p>

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

          <label className="form-field">
            <span>Mật khẩu</span>
            <input
              autoComplete="new-password"
              aria-invalid={Boolean(errors.password || error?.fieldErrors.password)}
              type="password"
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
            <FormError message={errors.password?.message ?? error?.fieldErrors.password} />
          </label>

          {error ? <p className="form-alert">{error.message}</p> : null}

          <button className="primary-button" disabled={isSubmitting} type="submit">
            {isSubmitting ? 'Đang tạo tài khoản...' : 'Tạo tài khoản'}
          </button>
        </form>

        <p className="auth-link">
          Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
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
