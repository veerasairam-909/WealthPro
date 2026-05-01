import { api } from './client';

export async function getAllSecurities() {
  const res = await api.get('/api/securities');
  return res.data;
}

export async function getSecurityById(id: number) {
  const res = await api.get('/api/securities/' + id);
  return res.data;
}
