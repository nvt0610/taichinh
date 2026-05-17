import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Pencil, RefreshCw, Trash2, X } from 'lucide-react';

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
  balance: string;
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
  const [pendingDeleteWallet, setPendingDeleteWallet] = useState<Wallet | null>(null);

  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setValue,
  } = useForm<WalletFormValues>({
    defaultValues: {
      balance: formatCurrencyInput(0),
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
      balance: formatCurrencyInput(0),
      description: '',
      name: '',
      type: 'CASH',
    });
    setPendingDeleteWallet(null);
  }

  function openEditForm(wallet: Wallet) {
    setEditingWallet(wallet);
    setSuccessMessage(null);
    setError(null);
    setValue('name', wallet.name);
    setValue('type', wallet.type);
    setValue('description', wallet.description ?? '');
    setValue('balance', formatCurrencyInput(wallet.balance));
    setPendingDeleteWallet(null);
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
          balance: parseCurrencyInput(values.balance),
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

  async function confirmDelete() {
    if (!pendingDeleteWallet) {
      return;
    }

    setError(null);
    setSuccessMessage(null);

    try {
      await walletService.deleteWallet(pendingDeleteWallet.id);
      setSuccessMessage('Đã xóa ví thành công.');
      await reloadWallets();
      if (editingWallet?.id === pendingDeleteWallet.id) {
        openCreateForm();
      }
      setPendingDeleteWallet(null);
    } catch (caughtError) {
      setError(normalizeApiError(caughtError));
    }
  }

  return (
    <section className="page-stack wallet-page" aria-labelledby="wallets-title">
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
            <button
              aria-label={editingWallet ? 'Hủy sửa ví' : 'Làm mới form'}
              className="ghost-button icon-button"
              onClick={openCreateForm}
              title={editingWallet ? 'Hủy sửa' : 'Form mới'}
              type="button"
            >
              {editingWallet ? <X size={17} strokeWidth={2.1} /> : <RefreshCw size={17} strokeWidth={2.1} />}
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
                  inputMode="numeric"
                  {...register('balance', {
                    onChange: (event) => {
                      setValue('balance', formatCurrencyInput(event.target.value), {
                        shouldDirty: true,
                        shouldValidate: true,
                      });
                    },
                    validate: (value) =>
                      parseCurrencyInput(value) >= 0 || 'Số dư phải lớn hơn hoặc bằng 0.',
                    required: 'Vui lòng nhập số dư ban đầu.',
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
            <ul className="simple-list wallet-list-scroll">
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
                    {editingWallet ? (
                      editingWallet.id === wallet.id ? (
                        <span className="status-pill">Đang sửa</span>
                      ) : null
                    ) : (
                      <div className="wallet-actions">
                        <button
                          aria-label={`Sửa ví ${wallet.name}`}
                          className="ghost-button icon-button"
                          onClick={() => openEditForm(wallet)}
                          title="Sửa"
                          type="button"
                        >
                          <Pencil size={16} strokeWidth={2.1} />
                        </button>
                        <button
                          aria-label={`Xóa ví ${wallet.name}`}
                          className="ghost-button icon-button wallet-delete"
                          onClick={() => setPendingDeleteWallet(wallet)}
                          title="Xóa"
                          type="button"
                        >
                          <Trash2 size={16} strokeWidth={2.1} />
                        </button>
                      </div>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </article>
      </section>

      {pendingDeleteWallet ? (
        <div className="confirm-toast" role="alertdialog" aria-labelledby="wallet-delete-title">
          <div>
            <strong id="wallet-delete-title">Xóa ví này?</strong>
            <p>
              Ví "{pendingDeleteWallet.name}" sẽ được chuyển sang trạng thái đã xóa nếu backend cho phép.
            </p>
          </div>
          <div className="confirm-toast-actions">
            <button className="ghost-button" onClick={() => setPendingDeleteWallet(null)} type="button">
              Hủy
            </button>
            <button className="primary-button danger-button" onClick={() => void confirmDelete()} type="button">
              Xóa ví
            </button>
          </div>
        </div>
      ) : null}
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

function parseCurrencyInput(value: string) {
  const digits = value.replace(/\D/g, '');
  return digits ? Number(digits) : 0;
}

function formatCurrencyInput(value: number | string) {
  const rawNumericValue = typeof value === 'number' ? value : Number(value);
  const numericValue = Number.isFinite(rawNumericValue) ? rawNumericValue : parseCurrencyInput(String(value));
  return `${new Intl.NumberFormat('vi-VN', {
    maximumFractionDigits: 0,
  }).format(numericValue)} VNĐ`;
}
