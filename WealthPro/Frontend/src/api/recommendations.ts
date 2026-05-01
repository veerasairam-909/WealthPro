import { api } from './client';

export async function getRecommendationsByClientId(clientId: number) {
  const res = await api.get('/api/recommendations/client/' + clientId);
  return res.data;
}

export async function getAllModelPortfolios() {
  const res = await api.get('/api/model-portfolios');
  return res.data;
}

export async function createModelPortfolio(data: any) {
  const res = await api.post('/api/model-portfolios', data);
  return res.data;
}

export async function updateModelPortfolio(id: number, data: any) {
  const res = await api.put('/api/model-portfolios/' + id, data);
  return res.data;
}

export async function deleteModelPortfolio(id: number) {
  const res = await api.delete('/api/model-portfolios/' + id);
  return res.data;
}

export async function createRecommendation(data: {
  clientId: number;
  riskClass: string;
  proposalJson: string;
  proposedDate: string;
  status: string;
}) {
  const res = await api.post('/api/recommendations', data);
  return res.data;
}

export async function updateRecommendationStatus(recoId: number, status: string) {
  const res = await api.patch('/api/recommendations/' + recoId + '/status', status, {
    headers: { 'Content-Type': 'application/json' },
  });
  return res.data;
}

export async function deleteRecommendation(recoId: number) {
  const res = await api.delete('/api/recommendations/' + recoId);
  return res.data;
}
