import { api } from './client';

export async function getAccountsByClientId(clientId: number) {
  const res = await api.get('/api/accounts/client/' + clientId);
  return res.data;
}

export async function getAccountById(accountId: number) {
  const res = await api.get('/api/accounts/' + accountId);
  return res.data;
}

export async function getAllAccounts() {
  const res = await api.get('/api/accounts');
  return res.data;
}

export async function createAccount(data: {
  clientId: number;
  accountType: string;
  baseCurrency: string;
  status: string;
}) {
  const res = await api.post('/api/accounts', data);
  return res.data;
}
