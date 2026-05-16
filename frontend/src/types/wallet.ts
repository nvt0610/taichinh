export type WalletType = 'CASH' | 'BANK' | 'EWALLET' | 'SAVINGS';

export interface Wallet {
  id: string;
  name: string;
  type: WalletType;
  balance: string;
  description: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateWalletPayload {
  name: string;
  type: WalletType;
  balance: number;
  description: string;
}

export interface UpdateWalletPayload {
  name: string;
  type: WalletType;
  description: string;
}
