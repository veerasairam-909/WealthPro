import { api } from './client';

export async function getAllUsers() {
  const res = await api.get('/auth/users');
  return res.data;
}

/** Fetch a single user by username — may work for own-profile lookup even for non-admin roles */
export async function getUserByUsername(username: string) {
  const res = await api.get('/auth/users/' + username, {
    validateStatus: (s) => s === 200 || s === 403 || s === 404,
  });
  if (res.status === 200) return res.data;
  return null; // forbidden or not found — caller handles gracefully
}

export async function deleteUser(username: string) {
  await api.delete('/auth/users/' + username);
}

export async function getAuditLogs(params?: any) {
  const res = await api.get('/auth/audit', { params });
  return res.data;
}
