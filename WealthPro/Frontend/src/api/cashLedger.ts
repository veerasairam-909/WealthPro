import { api } from './client';

export async function getCashLedgerByAccountId(accountId: number) {
  const res = await api.get('/api/cash-ledger/account/' + accountId);
  return res.data;
}

export async function getBalanceByAccountId(accountId: number) {
  const res = await api.get('/api/cash-ledger/account/' + accountId + '/balance');
  return res.data;
}
