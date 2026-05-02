import { api } from './client';

export async function getAllSecurities() {
  const res = await api.get('/api/securities');
  return res.data;
}

export async function getSecurityById(id: number) {
  const res = await api.get('/api/securities/' + id);
  return res.data;
}

export async function createSecurity(data: {
  symbol: string;
  assetClass: string;
  currency: string;
  country: string;
  status: string;
  currentPrice?: number | null;
}) {
  const res = await api.post('/api/securities', data);
  return res.data;
}

export async function updateSecurity(id: number, data: {
  symbol?: string;
  assetClass?: string;
  currency?: string;
  country?: string;
  status?: string;
  currentPrice?: number | null;
}) {
  const res = await api.put('/api/securities/' + id, data);
  return res.data;
}

export async function deleteSecurity(id: number) {
  await api.delete('/api/securities/' + id);
}
