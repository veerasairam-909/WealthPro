import { api } from './client';

export async function getCashLedgerByAccountId(accountId: number) {
  const res = await api.get('/api/cash-ledger/account/' + accountId);
  return res.data;
}

export async function getBalanceByAccountId(accountId: number) {
  const res = await api.get('/api/cash-ledger/account/' + accountId + '/balance');
  return res.data;
}

export async function createCashLedgerEntry(data: {
  accountId: number;
  amount: number;
  txnType: string;
  currency?: string;
  txnDate?: string;
  narrative?: string;
}) {
  const payload = {
    ...data,
    currency: data.currency || 'INR',
    txnDate: data.txnDate || new Date().toISOString().slice(0, 10),
    narrative: data.narrative || 'Fund deposit',
  };
  const res = await api.post('/api/cash-ledger', payload);
  return res.data;
}
