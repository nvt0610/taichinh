export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER';

export interface Transaction {
  id: string;
  walletId: string;
  categoryId: string | null;
  type: TransactionType;
  amount: string;
  title: string;
  note: string | null;
  transactionDate: string;
  referenceTransactionId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface TransferTransactionResponse {
  sourceTransaction: Transaction;
  destinationTransaction: Transaction;
}

export interface Category {
  id: string;
  name: string;
  type: 'INCOME' | 'EXPENSE';
  icon: string | null;
  color: string | null;
}

export interface ListTransactionParams {
  page?: number;
  size?: number;
  q?: string;
  walletId?: string;
  type?: TransactionType | '';
}
