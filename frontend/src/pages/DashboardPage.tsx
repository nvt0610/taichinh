import { useEffect, useMemo, useState } from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { ArrowDownLeft, ArrowLeftRight, ArrowUpRight, WalletCards } from 'lucide-react';

import * as dashboardService from '@/services/dashboardService';
import { normalizeApiError } from '@/services/apiClient';
import type { NormalizedApiError } from '@/types/api';
import type {
  DashboardMonthlyStatistic,
  DashboardRecentTransaction,
  DashboardSummary,
  DashboardTopSpendingCategory,
} from '@/types/dashboard';

interface DashboardData {
  summary: DashboardSummary;
  recentTransactions: DashboardRecentTransaction[];
  topSpendingCategories: DashboardTopSpendingCategory[];
  monthlyStatistics: DashboardMonthlyStatistic[];
}

export function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<NormalizedApiError | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function fetchDashboard() {
      setIsLoading(true);
      setError(null);

      try {
        const [summary, recentTransactions, topSpendingCategories, monthlyStatistics] =
          await Promise.all([
            dashboardService.getDashboardSummary(),
            dashboardService.getRecentTransactions(5),
            dashboardService.getTopSpendingCategories(),
            dashboardService.getMonthlyStatistics(6),
          ]);

        if (!isMounted) {
          return;
        }

        setData({
          summary,
          recentTransactions,
          topSpendingCategories,
          monthlyStatistics,
        });
      } catch (caughtError) {
        if (!isMounted) {
          return;
        }

        setError(normalizeApiError(caughtError));
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    void fetchDashboard();

    return () => {
      isMounted = false;
    };
  }, []);

  const monthStats = useMemo(
    () =>
      (data?.monthlyStatistics ?? []).map((item) => ({
        month: item.month,
        income: Number(item.totalIncome),
        expense: Number(item.totalExpense),
      })),
    [data?.monthlyStatistics],
  );

  if (isLoading) {
    return (
      <section className="page-stack" aria-busy="true" aria-labelledby="dashboard-title">
        <div className="page-heading">
          <p className="eyebrow">Dashboard</p>
          <h2 id="dashboard-title">Đang tải dữ liệu...</h2>
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section className="page-stack" aria-labelledby="dashboard-title">
        <div className="page-heading">
          <p className="eyebrow">Dashboard</p>
          <h2 id="dashboard-title">Không tải được dashboard</h2>
        </div>
        <article className="panel">
          <p>{error.message}</p>
        </article>
      </section>
    );
  }

  if (!data) {
    return (
      <section className="page-stack" aria-labelledby="dashboard-title">
        <div className="page-heading">
          <p className="eyebrow">Dashboard</p>
          <h2 id="dashboard-title">Chưa có dữ liệu</h2>
        </div>
      </section>
    );
  }

  const metrics = [
    {
      Icon: WalletCards,
      label: 'Tổng số dư',
      note: 'Từ tất cả ví hiện có',
      tone: 'transfer',
      value: formatVnd(data.summary.totalBalance),
    },
    {
      Icon: ArrowDownLeft,
      label: 'Thu nhập',
      note: 'Trong kỳ đã chọn',
      tone: 'income',
      value: formatVnd(data.summary.totalIncome),
    },
    {
      Icon: ArrowUpRight,
      label: 'Chi tiêu',
      note: 'Trong kỳ đã chọn',
      tone: 'expense',
      value: formatVnd(data.summary.totalExpense),
    },
    {
      Icon: ArrowLeftRight,
      label: 'Dòng tiền ròng',
      note: `${formatDate(data.summary.periodStart)} - ${formatDate(data.summary.periodEnd)}`,
      tone: Number(data.summary.netCashFlow) >= 0 ? 'income' : 'expense',
      value: formatSignedVnd(data.summary.netCashFlow, Number(data.summary.netCashFlow) >= 0 ? 'INCOME' : 'EXPENSE'),
    },
  ] as const;

  return (
    <section className="page-stack dashboard-page" aria-labelledby="dashboard-title">
      <div className="page-heading">
        <p className="eyebrow">Dashboard</p>
        <h2 id="dashboard-title">Tổng quan tháng này</h2>
      </div>

      <div className="dashboard-metric-grid">
        {metrics.map((metric) => {
          const Icon = metric.Icon;

          return (
            <article className={`metric-card metric-card-${metric.tone}`} key={metric.label}>
              <div className="metric-card-head">
                <span className="metric-label">{metric.label}</span>
                <span className={`metric-icon ${metric.tone}`} aria-hidden="true">
                  <Icon size={18} strokeWidth={2} />
                </span>
              </div>
              <strong className={`metric-value ${metric.tone}`}>{metric.value}</strong>
              <span className={`metric-note ${metric.tone}`}>{metric.note}</span>
            </article>
          );
        })}
      </div>

      <section className="dashboard-overview-grid">
        <article className="panel chart-panel dashboard-chart-panel">
          <h3>Thống kê theo tháng</h3>
          {monthStats.length === 0 ? (
            <p>Chưa có dữ liệu thống kê theo tháng.</p>
          ) : (
            <div className="chart-wrap">
              <ResponsiveContainer height={340} width="100%">
                <BarChart data={monthStats}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                  <XAxis dataKey="month" stroke="var(--text-muted)" />
                  <YAxis stroke="var(--text-muted)" tickFormatter={(value) => shortCurrency(value)} />
                  <Tooltip formatter={(value: number) => formatVnd(value)} />
                  <Legend />
                  <Bar dataKey="income" name="Thu nhập" radius={[6, 6, 0, 0]}>
                    {monthStats.map((item) => (
                      <Cell fill="var(--chart-income)" key={`income-${item.month}`} />
                    ))}
                  </Bar>
                  <Bar dataKey="expense" name="Chi tiêu" radius={[6, 6, 0, 0]}>
                    {monthStats.map((item) => (
                      <Cell fill="var(--chart-expense)" key={`expense-${item.month}`} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </article>

        <article className="panel dashboard-recent-panel">
          <h3>Giao dịch gần đây</h3>
          {data.recentTransactions.length === 0 ? (
            <p>Chưa có giao dịch nào trong tài khoản.</p>
          ) : (
            <ul className="simple-list dashboard-recent-list">
              {data.recentTransactions.map((transaction) => (
                <li className="simple-list-row" key={transaction.id}>
                  <div>
                    <p className="row-title">{transaction.title}</p>
                    <p className="row-meta">
                      {transaction.walletName ?? 'Không rõ ví'} ·{' '}
                      {transaction.categoryName ?? 'Không có danh mục'}
                    </p>
                  </div>
                  <strong className={`row-amount ${toTone(transaction)}`}>
                    {formatSignedVnd(transaction.amount, transaction.type)}
                  </strong>
                </li>
              ))}
            </ul>
          )}
        </article>
      </section>

      <section className="dashboard-secondary-grid">
        <article className="panel dashboard-category-panel">
          <h3>Top danh mục</h3>
          {data.topSpendingCategories.length === 0 ? (
            <p>Chưa có chi tiêu theo danh mục trong kỳ.</p>
          ) : (
            <ul className="simple-list">
              {data.topSpendingCategories.map((category) => (
                <li className="simple-list-row" key={category.categoryId}>
                  <div>
                    <p className="row-title">{category.categoryName}</p>
                    <p className="row-meta">{category.transactionCount} giao dịch</p>
                  </div>
                  <strong className="row-amount expense">
                    {formatVnd(category.totalAmount)}
                  </strong>
                </li>
              ))}
            </ul>
          )}
        </article>
      </section>
    </section>
  );
}

function toTone(transaction: DashboardRecentTransaction) {
  if (transaction.type === 'INCOME') {
    return 'income';
  }

  if (transaction.type === 'EXPENSE') {
    return 'expense';
  }

  return 'transfer';
}

function formatVnd(value: string | number) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(Number(value));
}

function formatSignedVnd(value: string | number, type: 'INCOME' | 'EXPENSE' | 'TRANSFER') {
  const numericValue = Number(value);
  const formatted = formatVnd(Math.abs(numericValue));

  if (type === 'INCOME') {
    return `+ ${formatted}`;
  }

  if (type === 'EXPENSE') {
    return `- ${formatted}`;
  }

  if (numericValue < 0) {
    return `- ${formatted}`;
  }

  if (numericValue > 0) {
    return `+ ${formatted}`;
  }

  return formatted;
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(new Date(value));
}

function shortCurrency(value: number) {
  if (value >= 1_000_000_000) {
    return `${formatCompactNumber(value / 1_000_000_000)} tỷ`;
  }

  if (value >= 1_000_000) {
    return `${formatCompactNumber(value / 1_000_000)} tr`;
  }

  if (value >= 1_000) {
    return `${formatCompactNumber(value / 1_000)} nghìn`;
  }

  return `${value} VNĐ`;
}

function formatCompactNumber(value: number) {
  return new Intl.NumberFormat('vi-VN', {
    maximumFractionDigits: value >= 10 ? 0 : 1,
  }).format(value);
}
