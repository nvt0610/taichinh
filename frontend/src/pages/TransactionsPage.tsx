import { useCallback, useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Pencil, RefreshCw, Trash2, X } from 'lucide-react';

import { normalizeApiError } from '@/services/apiClient';
import * as categoryService from '@/services/categoryService';
import * as transactionService from '@/services/transactionService';
import * as walletService from '@/services/walletService';
import type { NormalizedApiError } from '@/types/api';
import type { Wallet } from '@/types/wallet';
import type { Category, Transaction, TransactionType } from '@/types/transaction';

type FormMode = 'INCOME' | 'EXPENSE' | 'TRANSFER';

interface FilterFormValues {
  q: string;
  walletId: string;
  type: TransactionType | '';
}

interface TransactionFormValues {
  mode: FormMode;
  walletId: string;
  destinationWalletId: string;
  categoryId: string;
  amount: string;
  title: string;
  note: string;
  transactionDate: string;
}

export function TransactionsPage() {
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [incomeCategories, setIncomeCategories] = useState<Category[]>([]);
  const [expenseCategories, setExpenseCategories] = useState<Category[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<NormalizedApiError | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [editingTransactionId, setEditingTransactionId] = useState<string | null>(null);
  const [editingMode, setEditingMode] = useState<FormMode | null>(null);
  const [pendingDeleteTransaction, setPendingDeleteTransaction] = useState<Transaction | null>(null);

  const {
    handleSubmit: handleSubmitFilter,
    register: registerFilter,
    watch: watchFilter,
  } = useForm<FilterFormValues>({
    defaultValues: {
      q: '',
      type: '',
      walletId: '',
    },
  });

  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setValue,
    watch,
  } = useForm<TransactionFormValues>({
    defaultValues: {
      amount: formatCurrencyInput(0),
      categoryId: '',
      destinationWalletId: '',
      mode: 'EXPENSE',
      note: '',
      title: '',
      transactionDate: new Date().toISOString().slice(0, 16),
      walletId: '',
    },
  });

  const mode = watch('mode');
  const filterValues = watchFilter();
  const visibleCategories = mode === 'INCOME' ? incomeCategories : expenseCategories;

  const walletMap = useMemo(
    () => new Map(wallets.map((wallet) => [wallet.id, wallet])),
    [wallets],
  );
  const categoryMap = useMemo(
    () =>
      new Map(
        [...incomeCategories, ...expenseCategories].map((category) => [
          category.id,
          category,
        ]),
      ),
    [expenseCategories, incomeCategories],
  );

  const loadBootstrapData = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const [walletData, incomeData, expenseData] = await Promise.all([
        walletService.listWallets(),
        categoryService.listCategories('INCOME'),
        categoryService.listCategories('EXPENSE'),
      ]);

      setWallets(walletData);
      setIncomeCategories(incomeData);
      setExpenseCategories(expenseData);

      if (walletData.length > 0) {
        reset((oldValues) => ({
          ...oldValues,
          destinationWalletId: oldValues.destinationWalletId || walletData[0].id,
          walletId: oldValues.walletId || walletData[0].id,
        }));
      }
    } catch (caughtError) {
      setError(normalizeApiError(caughtError));
    } finally {
      setIsLoading(false);
    }
  }, [reset]);

  const loadTransactions = useCallback(async () => {
    try {
      const data = await transactionService.listTransactions({
        q: filterValues.q?.trim(),
        type: filterValues.type,
        walletId: filterValues.walletId,
      });
      setTransactions(data);
    } catch (caughtError) {
      setError(normalizeApiError(caughtError));
    }
  }, [filterValues.q, filterValues.type, filterValues.walletId]);

  useEffect(() => {
    void loadBootstrapData();
  }, [loadBootstrapData]);

  useEffect(() => {
    void loadTransactions();
  }, [loadTransactions]);

  async function onSubmit(values: TransactionFormValues) {
    setIsSaving(true);
    setError(null);
    setSuccessMessage(null);

    try {
      if (values.mode === 'TRANSFER') {
        const payload = {
          amount: parseCurrencyInput(values.amount),
          destinationWalletId: values.destinationWalletId,
          note: values.note,
          sourceWalletId: values.walletId,
          title: values.title,
          transactionDate: toDateTime(values.transactionDate),
        };

        if (editingTransactionId) {
          await transactionService.updateTransfer(editingTransactionId, payload);
        } else {
          await transactionService.createTransfer(payload);
        }
      } else if (values.mode === 'INCOME') {
        const payload = {
          amount: parseCurrencyInput(values.amount),
          categoryId: values.categoryId || undefined,
          note: values.note,
          title: values.title,
          transactionDate: toDateTime(values.transactionDate),
          walletId: values.walletId,
        };

        if (editingTransactionId) {
          await transactionService.updateIncome(editingTransactionId, payload);
        } else {
          await transactionService.createIncome(payload);
        }
      } else {
        const payload = {
          amount: parseCurrencyInput(values.amount),
          categoryId: values.categoryId || undefined,
          note: values.note,
          title: values.title,
          transactionDate: toDateTime(values.transactionDate),
          walletId: values.walletId,
        };

        if (editingTransactionId) {
          await transactionService.updateExpense(editingTransactionId, payload);
        } else {
          await transactionService.createExpense(payload);
        }
      }

      setSuccessMessage(editingTransactionId ? 'Đã cập nhật giao dịch.' : 'Đã tạo giao dịch thành công.');
      await Promise.all([loadTransactions(), loadBootstrapData()]);
      setEditingTransactionId(null);
      setEditingMode(null);
      reset((oldValues) => ({
        ...oldValues,
        amount: formatCurrencyInput(0),
        categoryId: '',
        note: '',
        title: '',
      }));
    } catch (caughtError) {
      setError(normalizeApiError(caughtError));
    } finally {
      setIsSaving(false);
    }
  }

  function handleEdit(transaction: Transaction) {
    setError(null);
    setSuccessMessage(null);
    setPendingDeleteTransaction(null);

    if (transaction.type === 'TRANSFER') {
      const counterpart = transactions.find((item) => item.id === transaction.referenceTransactionId);
      const isSource = Number(transaction.amount) < 0;
      const source = isSource ? transaction : counterpart;
      const destination = isSource ? counterpart : transaction;

      if (!source || !destination) {
        setError({
          fieldErrors: {},
          message: 'Không tìm được cặp giao dịch transfer để sửa.',
        });
        return;
      }

      setEditingTransactionId(transaction.id);
      setEditingMode('TRANSFER');
      reset({
        amount: formatCurrencyInput(Math.abs(Number(source.amount))),
        categoryId: '',
        destinationWalletId: destination.walletId,
        mode: 'TRANSFER',
        note: source.note ?? '',
        title: source.title,
        transactionDate: toInputDateTime(source.transactionDate),
        walletId: source.walletId,
      });
      return;
    }

    setEditingTransactionId(transaction.id);
    setEditingMode(transaction.type);
    reset({
      amount: formatCurrencyInput(transaction.amount),
      categoryId: transaction.categoryId ?? '',
      destinationWalletId: '',
      mode: transaction.type,
      note: transaction.note ?? '',
      title: transaction.title,
      transactionDate: toInputDateTime(transaction.transactionDate),
      walletId: transaction.walletId,
    });
  }

  function cancelEdit() {
    setEditingTransactionId(null);
    setEditingMode(null);
    setError(null);
    setSuccessMessage(null);
    setPendingDeleteTransaction(null);
    reset({
      amount: formatCurrencyInput(0),
      categoryId: '',
      destinationWalletId: wallets[0]?.id ?? '',
      mode: 'EXPENSE',
      note: '',
      title: '',
      transactionDate: new Date().toISOString().slice(0, 16),
      walletId: wallets[0]?.id ?? '',
    });
  }

  async function confirmDelete() {
    if (!pendingDeleteTransaction) {
      return;
    }

    setError(null);
    setSuccessMessage(null);

    try {
      await transactionService.deleteTransaction(pendingDeleteTransaction.id);
      setSuccessMessage('Đã xóa giao dịch.');
      await Promise.all([loadTransactions(), loadBootstrapData()]);
      if (editingTransactionId === pendingDeleteTransaction.id) {
        cancelEdit();
      }
      setPendingDeleteTransaction(null);
    } catch (caughtError) {
      setError(normalizeApiError(caughtError));
    }
  }

  return (
    <section className="page-stack transaction-page" aria-labelledby="transactions-title">
      <div className="page-heading">
        <p className="eyebrow">Transactions</p>
        <h2 id="transactions-title">Giao dịch</h2>
      </div>

      <section className="panel-grid">
        <article className="panel">
          <div className="wallet-panel-head">
            <h3>{editingTransactionId ? 'Sửa giao dịch' : 'Tạo giao dịch'}</h3>
            <button
              aria-label={editingTransactionId ? 'Hủy sửa giao dịch' : 'Làm mới form'}
              className="ghost-button icon-button"
              onClick={cancelEdit}
              title={editingTransactionId ? 'Hủy sửa' : 'Form mới'}
              type="button"
            >
              {editingTransactionId ? <X size={17} strokeWidth={2.1} /> : <RefreshCw size={17} strokeWidth={2.1} />}
            </button>
          </div>
          {wallets.length === 0 && !isLoading ? (
            <p>Chưa có ví để tạo giao dịch. Hãy tạo ví trước ở trang Wallets.</p>
          ) : null}

          <form className="auth-form" onSubmit={handleSubmit(onSubmit)}>
            <label className="form-field">
              <span>Loại giao dịch</span>
              <select
                className="form-select"
                disabled={Boolean(editingTransactionId)}
                {...register('mode')}
              >
                <option value="INCOME">Thu nhập</option>
                <option value="EXPENSE">Chi tiêu</option>
                <option value="TRANSFER">Chuyển khoản</option>
              </select>
              {editingTransactionId ? (
                <span className="row-meta">
                  Đang sửa giao dịch {labelForMode(editingMode)}. Muốn đổi loại, hãy tạo giao dịch mới.
                </span>
              ) : null}
            </label>

            <label className="form-field">
              <span>{mode === 'TRANSFER' ? 'Ví nguồn' : 'Ví'}</span>
              <select className="form-select" {...register('walletId', { required: true })}>
                {wallets.map((wallet) => (
                  <option key={wallet.id} value={wallet.id}>
                    {wallet.name}
                  </option>
                ))}
              </select>
            </label>

            {mode === 'TRANSFER' ? (
              <label className="form-field">
                <span>Ví đích</span>
                <select
                  className="form-select"
                  {...register('destinationWalletId', { required: true })}
                >
                  {wallets.map((wallet) => (
                    <option key={wallet.id} value={wallet.id}>
                      {wallet.name}
                    </option>
                  ))}
                </select>
              </label>
            ) : (
              <label className="form-field">
                <span>Danh mục</span>
                <select className="form-select" {...register('categoryId')}>
                  <option value="">Không chọn</option>
                  {visibleCategories.map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </label>
            )}

            <label className="form-field">
              <span>Số tiền</span>
              <input
                inputMode="numeric"
                {...register('amount', {
                  onChange: (event) => {
                    setValue('amount', formatCurrencyInput(event.target.value), {
                      shouldDirty: true,
                      shouldValidate: true,
                    });
                  },
                  validate: (value) =>
                    parseCurrencyInput(value) > 0 || 'Số tiền phải lớn hơn 0.',
                  required: 'Vui lòng nhập số tiền.',
                })}
              />
              <FormError message={errors.amount?.message} />
            </label>

            <label className="form-field">
              <span>Tiêu đề</span>
              <input
                {...register('title', {
                  maxLength: {
                    message: 'Tiêu đề không quá 255 ký tự.',
                    value: 255,
                  },
                  required: 'Vui lòng nhập tiêu đề.',
                })}
              />
              <FormError message={errors.title?.message} />
            </label>

            <label className="form-field">
              <span>Ngày giao dịch</span>
              <input
                type="datetime-local"
                {...register('transactionDate', { required: true })}
              />
            </label>

            <label className="form-field">
              <span>Ghi chú</span>
              <textarea className="form-textarea" rows={3} {...register('note')} />
            </label>

            {error ? <p className="form-alert">{error.message}</p> : null}
            {successMessage ? <p className="form-success">{successMessage}</p> : null}

            <button className="primary-button" disabled={isSaving || wallets.length === 0} type="submit">
              {isSaving
                ? 'Đang lưu...'
                : editingTransactionId
                  ? 'Cập nhật giao dịch'
                  : 'Tạo giao dịch'}
            </button>
          </form>
        </article>

        <article className="panel">
          <h3>Lịch sử giao dịch</h3>
          <form className="filter-row" onSubmit={handleSubmitFilter(() => undefined)}>
            <input placeholder="Tìm theo tiêu đề..." {...registerFilter('q')} />
            <select className="form-select" {...registerFilter('walletId')}>
              <option value="">Tất cả ví</option>
              {wallets.map((wallet) => (
                <option key={wallet.id} value={wallet.id}>
                  {wallet.name}
                </option>
              ))}
            </select>
            <select className="form-select" {...registerFilter('type')}>
              <option value="">Tất cả loại</option>
              <option value="INCOME">Thu nhập</option>
              <option value="EXPENSE">Chi tiêu</option>
              <option value="TRANSFER">Chuyển khoản</option>
            </select>
          </form>

          {isLoading ? (
            <p>Đang tải giao dịch...</p>
          ) : transactions.length === 0 ? (
            <p>Chưa có giao dịch phù hợp với bộ lọc hiện tại.</p>
          ) : (
            <ul className="simple-list transaction-list-scroll">
              {transactions.map((transaction) => (
                <li className="simple-list-row wallet-row" key={transaction.id}>
                  <div>
                    <p className="row-title">{transaction.title}</p>
                    <p className="row-meta">
                      {walletMap.get(transaction.walletId)?.name ?? 'Không rõ ví'} ·{' '}
                      {categoryMap.get(transaction.categoryId ?? '')?.name ?? 'Không có danh mục'}
                    </p>
                  </div>
                  <div className="wallet-row-right">
                    <strong className={`row-amount ${toTone(transaction.type)}`}>
                      {formatSignedVnd(transaction.amount, transaction.type)}
                    </strong>
                    <span className="row-meta">{formatDateTime(transaction.transactionDate)}</span>
                    {editingTransactionId ? (
                      editingTransactionId === transaction.id ? (
                        <span className="status-pill">Đang sửa</span>
                      ) : null
                    ) : (
                      <div className="wallet-actions">
                        <button
                          aria-label={`Sửa giao dịch ${transaction.title}`}
                          className="ghost-button icon-button"
                          onClick={() => handleEdit(transaction)}
                          title="Sửa"
                          type="button"
                        >
                          <Pencil size={16} strokeWidth={2.1} />
                        </button>
                        <button
                          aria-label={`Xóa giao dịch ${transaction.title}`}
                          className="ghost-button icon-button wallet-delete"
                          onClick={() => setPendingDeleteTransaction(transaction)}
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

      {pendingDeleteTransaction ? (
        <div className="confirm-toast" role="alertdialog" aria-labelledby="transaction-delete-title">
          <div>
            <strong id="transaction-delete-title">Xóa giao dịch này?</strong>
            <p>
              Giao dịch "{pendingDeleteTransaction.title}" sẽ được xóa mềm và số dư sẽ được backend xử lý lại.
            </p>
          </div>
          <div className="confirm-toast-actions">
            <button className="ghost-button" onClick={() => setPendingDeleteTransaction(null)} type="button">
              Hủy
            </button>
            <button className="primary-button danger-button" onClick={() => void confirmDelete()} type="button">
              Xóa giao dịch
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

function toDateTime(value: string) {
  return new Date(value).toISOString().slice(0, 19);
}

function toInputDateTime(value: string) {
  return new Date(value).toISOString().slice(0, 16);
}

function toTone(type: TransactionType) {
  if (type === 'INCOME') return 'income';
  if (type === 'EXPENSE') return 'expense';
  return 'transfer';
}

function formatSignedVnd(value: string | number, type: TransactionType) {
  const numericValue = Number(value);
  const formatted = formatVnd(Math.abs(numericValue));

  if (type === 'INCOME') return `+ ${formatted}`;
  if (type === 'EXPENSE') return `- ${formatted}`;
  if (numericValue < 0) return `- ${formatted}`;
  if (numericValue > 0) return `+ ${formatted}`;
  return formatted;
}

function formatVnd(value: string | number) {
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
  }).format(Math.abs(numericValue))} VNĐ`;
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(new Date(value));
}

function labelForMode(mode: FormMode | null) {
  if (mode === 'INCOME') return 'Thu nhập';
  if (mode === 'EXPENSE') return 'Chi tiêu';
  if (mode === 'TRANSFER') return 'Chuyển khoản';
  return '';
}
