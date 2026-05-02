import { api } from './client';

export async function getAllSuitabilityRules() {
  const res = await api.get('/api/suitability-rules');
  return res.data;
}

export async function createSuitabilityRule(data: {
  description: string;
  expression: string;
  status: string;
}) {
  const res = await api.post('/api/suitability-rules', data);
  return res.data;
}

export async function updateSuitabilityRule(id: number, data: {
  description?: string;
  expression?: string;
  status?: string;
}) {
  const res = await api.put('/api/suitability-rules/' + id, data);
  return res.data;
}

export async function deleteSuitabilityRule(id: number) {
  await api.delete('/api/suitability-rules/' + id);
}
