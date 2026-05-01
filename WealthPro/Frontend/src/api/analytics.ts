import { api } from './client';

// ─── Performance ──────────────────────────────────────────────────────────────

export async function getPerformanceByAccount(accountId: number) {
  const res = await api.get(`/api/analytics/accounts/${accountId}/performance`);
  return res.data;
}

// ─── Risk Measures ────────────────────────────────────────────────────────────

export async function getRiskMeasuresByAccount(accountId: number) {
  const res = await api.get(`/api/analytics/accounts/${accountId}/risk-measures`);
  return res.data;
}

// ─── Compliance Breaches (per account) ───────────────────────────────────────

export async function getBreachesByAccount(accountId: number) {
  const res = await api.get(`/api/analytics/accounts/${accountId}/breaches`);
  return res.data;
}

export async function runComplianceScan(accountId: number) {
  const res = await api.post(`/api/analytics/accounts/${accountId}/compliance-scan`);
  return res.data;
}

export async function acknowledgeBreach(breachId: number) {
  const res = await api.patch(`/api/compliance-breaches/${breachId}/acknowledge`);
  return res.data;
}

export async function closeBreach(breachId: number) {
  const res = await api.patch(`/api/compliance-breaches/${breachId}/close`);
  return res.data;
}

// ─── Account Dashboard ────────────────────────────────────────────────────────

export async function getAccountDashboard(accountId: number) {
  const res = await api.get(`/api/analytics/accounts/${accountId}/dashboard`);
  return res.data;
}
