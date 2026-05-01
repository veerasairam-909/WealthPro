import { api } from './client';

export async function getHoldingsByAccountId(accountId: number) {
  const res = await api.get('/api/holdings/account/' + accountId);
  return res.data;
}

export async function getHoldingById(holdingId: number) {
  const res = await api.get('/api/holdings/' + holdingId);
  return res.data;
}
