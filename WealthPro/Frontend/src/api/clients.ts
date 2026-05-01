import { api } from './client';

// ─── Clients ───
export async function getAllClients() {
  const res = await api.get('/api/clients');
  return res.data;
}

export async function getClientById(id: number) {
  const res = await api.get('/api/clients/' + id);
  return res.data;
}

export async function createClient(data: any) {
  const res = await api.post('/api/clients', data);
  return res.data;
}

export async function updateClient(id: number, data: any) {
  const res = await api.put('/api/clients/' + id, data);
  return res.data;
}

// ─── KYC ───
export async function getKycDocs(clientId: number) {
  const res = await api.get('/api/clients/' + clientId + '/kyc');
  return res.data;
}

export async function uploadKyc(clientId: number, documentType: string, file: File) {
  const formData = new FormData();
  formData.append('documentType', documentType);
  formData.append('document', file);
  const res = await api.post('/api/clients/' + clientId + '/kyc', formData);
  return res.data;
}

export async function updateKycStatus(kycId: number, status: string) {
  const res = await api.put('/api/clients/kyc/' + kycId + '/status', { status });
  return res.data;
}

// ─── Risk Profile ───
export async function getRiskProfile(clientId: number) {
  // 404 means the client has no risk profile yet — return null instead of throwing
  const res = await api.get('/api/clients/' + clientId + '/risk-profile', {
    validateStatus: (s) => s === 200 || s === 404,
  });
  return res.status === 404 ? null : res.data;
}

export async function createRiskProfile(clientId: number, data: any) {
  const res = await api.post('/api/clients/' + clientId + '/risk-profile', data);
  return res.data;
}

export async function updateRiskProfile(clientId: number, data: any) {
  const res = await api.put('/api/clients/' + clientId + '/risk-profile', data);
  return res.data;
}
