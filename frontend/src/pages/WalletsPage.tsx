import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';

import { normalizeApiError } from '@/services/apiClient';
import * as walletService from '@/services/walletService';
import type { NormalizedApiError } from '@/types/api';
import type {
  CreateWalletPayload,
  UpdateWalletPayload,
  Wallet,
  WalletType,
} from '@/types/wallet';

interface WalletFormValues {
  name: string;
  type: WalletType;
  balance: number;
  description: string;
}

const walletTypeLabels: Record<WalletType, string> = {
  BANK: 'Ngân hàng',
  CASH: 'Tiền mặt',
  EWALLET: 'Ví điện tử',
  SAVINGS: 'Tiết kiệm',
};

export function WalletsPage() {
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<NormalizedApiError | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [editingWallet, setEditingWallet] = useState<Wallet | null>(null);

  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setValue,
  } = useForm<WalletFormValues>({
    defaultValues: {
      balance: 0,
      description: '',
      name: '',
      type: 'CASH',
    },
  });

  useEffect(() => {
    void reloadWallets();
  }, []);

  const totalBalance = useMemo(
    () =>
      wallets.reduce((total, wallet) => total + Number(wallet.balance), 0),
    [wallets],
  );

  async function reloadWallets() {
    setIsLoading(true);
    setError(null);

    try {
      const data = await walletService.listWallets();
      setWallets(data);
    } catch (caughtError) {
      setError(normalizeApiError(caughtError));
    } finally {
      setIsLoading(false);
    }
  }

  function openCreateForm() {
    setEditingWallet(null);
    setSuccessMessage(null);
    setError(null);
    reset({
      balance: 0,
      description: '',
      name: '',
      type: 'CASH',
    });
  }

  function openEditForm(wallet: Wallet) {
    setEditingWallet(wallet);
    setSuccessMessage(null);
    setError(null);
    setValue('name', wallet.name);
    setValue('type', wallet.type);
    setValue('description', wallet.description ?? '');
    setValue('balance', Number(wallet.balance));
  }

  async function onSubmit(values: WalletFormValues) {
    setIsSaving(true);
    setError(null);
    setSuccessMessage(null);

    try {
      if (editingWallet) {
        const payload: UpdateWalletPayload = {
          description: values.description,
          name: values.name,
          type: values.type,
        };
        await walletService.updateWallet(editingWallet.id, payload);
        setSuccessMessage('Đã cập nhật ví thành công.');
      } else {
        const payload: CreateWalletPayload = {
          balance: Number(values.balance),
          description: values.description,
          name: values.name,
          type: values.type,
        };
        await walletService.createWallet(payload);
        setSuccessMessage('Đã tạo ví thành công.');
      }

      await reloadWallets();
      openCreateForm();
    } catch (caughtError) {
      setError(normalizeApiError(caughtError));
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDelete(wallet: Wallet) {
    const shouldDelete = window.confirm(`Xóa ví "${wallet.name}"?`);
    if (!shouldDelete) {
      return;
    }

    setError(null);
    setSuccessMessage(null);

    try {
      await walletService.deleteWallet(wallet.id);
      setSuccessMessage('Đã xóa ví thành công.');
      await reloadWallets();
      if (editingWallet?.id === wallet.id) {
        openCreateForm();
      }
    } catch (caughtError) {
      setError(normalizeApiError(caughtError));
    }
  }

  return (
    <section className="page-stack" aria-labelledby="wallets-title">
      <div className="page-heading">
        <p className="eyebrow">Wallets</p>
        <h2 id="wallets-title">Ví tiền</h2>
        <p className="row-meta">
          Tổng số dư hiện tại: <strong>{formatVnd(totalBalance)}</strong>
        </p>
      </div>

      <section className="panel-grid">
        <article className="panel">
          <div className="wallet-panel-head">
            <h3>{editingWallet ? 'Sửa ví' : 'Tạo ví mới'}</h3>
            <button className="ghost-button" onClick={openCreateForm} type="button">
              Form mới
            </button>
          </div>

          <form className="auth-form" onSubmit={handleSubmit(onSubmit)}>
            <label className="form-field">
              <span>Tên ví</span>
              <input
                aria-invalid={Boolean(errors.name || error?.fieldErrors.name)}
                {...register('name', {
                  maxLength: {
                    message: 'Tên ví không quá 100 ký tự.',
                    value: 100,
                  },
                  required: 'Vui lòng nhập tên ví.',
                })}
              />
              <FormError message={errors.name?.message ?? error?.fieldErrors.name} />
            </label>

            <label className="form-field">
              <span>Loại ví</span>
              <select className="form-select" {...register('type', { required: true })}>
                {Object.entries(walletTypeLabels).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
            </label>

            {!editingWallet ? (
              <label className="form-field">
                <span>Số dư ban đầu</span>
                <input
                  aria-invalid={Boolean(errors.balance || error?.fieldErrors.balance)}
                  step="1000"
                  type="number"
                  {...register('balance', {
                    min: {
                      message: 'Số dư phải lớn hơn hoặc bằng 0.',
                      value: 0,
                    },
                    required: 'Vui lòng nhập số dư ban đầu.',
                    valueAsNumber: true,
                  })}
                />
                <FormError
                  message={errors.balance?.message ?? error?.fieldErrors.balance}
                />
              </label>
            ) : null}

            <label className="form-field">
              <span>Mô tả</span>
              <textarea
                className="form-textarea"
                rows={3}
                {...register('description', {
                  maxLength: {
                    message: 'Mô tả không quá 1000 ký tự.',
                    value: 1000,
                  },
                })}
              />
              <FormError
                message={errors.description?.message ?? error?.fieldErrors.description}
              />
            </label>

            {error ? <p className="form-alert">{error.message}</p> : null}
            {successMessage ? <p className="form-success">{successMessage}</p> : null}

            <button className="primary-button" disabled={isSaving} type="submit">
              {isSaving
                ? 'Đang lưu...'
                : editingWallet
                  ? 'Cập nhật ví'
                  : 'Tạo ví'}
            </button>
          </form>
        </article>

        <article className="panel">
          <h3>Danh sách ví</h3>
          {isLoading ? (
            <p>Đang tải danh sách ví...</p>
          ) : wallets.length === 0 ? (
            <p>Chưa có ví nào. Hãy tạo ví đầu tiên ở panel bên trái.</p>
          ) : (
            <ul className="simple-list">
              {wallets.map((wallet) => (
                <li className="simple-list-row wallet-row" key={wallet.id}>
                  <div>
                    <p className="row-title">{wallet.name}</p>
                    <p className="row-meta">
                      {walletTypeLabels[wallet.type]} ·{' '}
                      {wallet.description || 'Không có mô tả'}
                    </p>
                  </div>
                  <div className="wallet-row-right">
                    <strong className="row-amount transfer">
                      {formatVnd(wallet.balance)}
                    </strong>
                    <div className="wallet-actions">
                      <button
                        className="ghost-button"
                        onClick={() => openEditForm(wallet)}
                        type="button"
                      >
                        Sửa
                      </button>
                      <button
                        className="ghost-button wallet-delete"
                        onClick={() => void handleDelete(wallet)}
                        type="button"
                      >
                        Xóa
                      </button>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </article>
      </section>
    </section>
  );
}

function FormError({ message }: { message?: string }) {
  if (!message) {
    return null;
  }

  return <span className="form-error">{message}</span>;
}

function formatVnd(value: number | string) {
  return new Intl.NumberFormat('vi-VN', {
    currency: 'VND',
    maximumFractionDigits: 0,
    style: 'currency',
  }).format(Number(value));
}
