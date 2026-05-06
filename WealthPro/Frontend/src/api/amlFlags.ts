import { api } from './client';

export async function getAllAmlFlags(status?: string) {
  const params = status ? { status } : {};
  const res = await api.get('/api/aml-flags', { params });
  return res.data;
}

export async function getAmlFlagsByClient(clientId: number) {
  const res = await api.get(`/api/aml-flags/client/${clientId}`);
  return res.data;
}

export async function createAmlFlag(payload: {
  clientId: number;
  flagType: string;
  description: string;
  notes?: string;
  raisedByUserId?: number;
}) {
  const res = await api.post('/api/aml-flags', payload);
  return res.data;
}

export async function requestClosureAmlFlag(amlFlagId: number) {
  const res = await api.put(`/api/aml-flags/${amlFlagId}/request-closure`);
  return res.data;
}

export async function reviewAmlFlag(
  amlFlagId: number,
  payload: { status: string; notes?: string }
) {
  const res = await api.put(`/api/aml-flags/${amlFlagId}/review`, payload);
  return res.data;
}

export async function deleteAmlFlag(amlFlagId: number) {
  const res = await api.delete(`/api/aml-flags/${amlFlagId}`);
  return res.data;
}
