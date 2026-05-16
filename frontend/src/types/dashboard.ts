export interface DashboardSummary {
  totalBalance: string;
  totalIncome: string;
  totalExpense: string;
  netCashFlow: string;
  periodStart: string;
  periodEnd: string;
}

export interface DashboardRecentTransaction {
  id: string;
  walletId: string;
  walletName: string | null;
  categoryId: string | null;
  categoryName: string | null;
  type: 'INCOME' | 'EXPENSE' | 'TRANSFER';
  amount: string;
  title: string;
  transactionDate: string;
}

export interface DashboardTopSpendingCategory {
  categoryId: string;
  categoryName: string;
  icon: string | null;
  color: string | null;
  totalAmount: string;
  transactionCount: number;
}

export interface DashboardMonthlyStatistic {
  month: string;
  totalIncome: string;
  totalExpense: string;
  netCashFlow: string;
}
