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
      label: 'Tổng số dư',
      note: 'Từ tất cả ví hiện có',
      tone: 'transfer',
      value: formatVnd(data.summary.totalBalance),
    },
    {
      label: 'Thu nhập',
      note: 'Trong kỳ đã chọn',
      tone: 'income',
      value: formatVnd(data.summary.totalIncome),
    },
    {
      label: 'Chi tiêu',
      note: 'Trong kỳ đã chọn',
      tone: 'expense',
      value: formatVnd(data.summary.totalExpense),
    },
  ] as const;

  return (
    <section className="page-stack" aria-labelledby="dashboard-title">
      <div className="page-heading">
        <p className="eyebrow">Dashboard</p>
        <h2 id="dashboard-title">Tổng quan tháng này</h2>
      </div>

      <div className="metric-grid">
        {metrics.map((metric) => (
          <article className="metric-card" key={metric.label}>
            <span className="metric-label">{metric.label}</span>
            <strong className="metric-value">{metric.value}</strong>
            <span className={`metric-note ${metric.tone}`}>{metric.note}</span>
          </article>
        ))}
      </div>

      <section className="panel-grid">
        <article className="panel">
          <h3>Giao dịch gần đây</h3>
          {data.recentTransactions.length === 0 ? (
            <p>Chưa có giao dịch nào trong tài khoản.</p>
          ) : (
            <ul className="simple-list">
              {data.recentTransactions.map((transaction) => (
                <li className="simple-list-row" key={transaction.id}>
                  <div>
                    <p className="row-title">{transaction.title}</p>
                    <p className="row-meta">
                      {transaction.walletName ?? 'Không rõ ví'} ·{' '}
                      {transaction.categoryName ?? 'Không có danh mục'}
                    </p>
                  </div>
                  <strong className={`row-amount ${toTone(transaction.type)}`}>
                    {formatSignedVnd(transaction.amount, transaction.type)}
                  </strong>
                </li>
              ))}
            </ul>
          )}
        </article>
        <article className="panel">
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

      <article className="panel chart-panel">
        <h3>Thống kê theo tháng</h3>
        {monthStats.length === 0 ? (
          <p>Chưa có dữ liệu thống kê theo tháng.</p>
        ) : (
          <div className="chart-wrap">
            <ResponsiveContainer height={280} width="100%">
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

      <article className="panel">
        <h3>Dòng tiền ròng</h3>
        <p className="row-meta">
          Từ {formatDate(data.summary.periodStart)} đến {formatDate(data.summary.periodEnd)}
        </p>
        <strong className={`row-amount ${Number(data.summary.netCashFlow) >= 0 ? 'income' : 'expense'}`}>
          {formatSignedVnd(data.summary.netCashFlow, Number(data.summary.netCashFlow) >= 0 ? 'INCOME' : 'EXPENSE')}
        </strong>
      </article>
    </section>
  );
}

function toTone(type: DashboardRecentTransaction['type']) {
  if (type === 'INCOME') {
    return 'income';
  }

  if (type === 'EXPENSE') {
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
  const formatted = formatVnd(value);

  if (type === 'INCOME') {
    return `+ ${formatted}`;
  }

  if (type === 'EXPENSE') {
    return `- ${formatted}`;
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
    return `${(value / 1_000_000_000).toFixed(1)}B`;
  }

  if (value >= 1_000_000) {
    return `${(value / 1_000_000).toFixed(1)}M`;
  }

  if (value >= 1_000) {
    return `${(value / 1_000).toFixed(0)}K`;
  }

  return String(value);
}
