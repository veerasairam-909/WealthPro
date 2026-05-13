import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import {
  getPerformanceByAccount,
  getRiskMeasuresByAccount,
  getBreachesByAccount,
  runComplianceScan,
  acknowledgeBreach,
  closeBreach,
  getAccountDashboard,
} from '@/api/analytics';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('analytics API', () => {
  beforeEach(() => vi.clearAllMocks());

  // ─── getPerformanceByAccount ─────────────────────────────────────────────────

  it('getPerformanceByAccount calls correct endpoint', async () => {
    const records = [{ returnPercentage: 5.5, period: 'MONTHLY' }];
    vi.mocked(api.get).mockResolvedValue({ data: records });

    const result = await getPerformanceByAccount(1);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/analytics/accounts/1/performance');
    expect(result).toEqual(records);
  });

  it('getPerformanceByAccount returns empty list', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    const result = await getPerformanceByAccount(99);

    expect(result).toEqual([]);
  });

  // ─── getRiskMeasuresByAccount ────────────────────────────────────────────────

  it('getRiskMeasuresByAccount calls risk-measures endpoint', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ measureType: 'VAR_95' }] });

    const result = await getRiskMeasuresByAccount(2);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/analytics/accounts/2/risk-measures');
    expect(result[0].measureType).toBe('VAR_95');
  });

  // ─── getBreachesByAccount ────────────────────────────────────────────────────

  it('getBreachesByAccount calls breaches endpoint', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ status: 'OPEN' }] });

    const result = await getBreachesByAccount(3);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/analytics/accounts/3/breaches');
    expect(result[0].status).toBe('OPEN');
  });

  it('getBreachesByAccount returns empty list when no breaches', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    const result = await getBreachesByAccount(3);

    expect(result).toEqual([]);
  });

  // ─── runComplianceScan ───────────────────────────────────────────────────────

  it('runComplianceScan posts to compliance-scan endpoint', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: [{ breachId: 1, status: 'OPEN' }] });

    const result = await runComplianceScan(4);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/analytics/accounts/4/compliance-scan');
    expect(result).toHaveLength(1);
  });

  // ─── acknowledgeBreach ───────────────────────────────────────────────────────

  it('acknowledgeBreach patches acknowledge endpoint', async () => {
    vi.mocked(api.patch).mockResolvedValue({ data: { breachId: 5, status: 'ACKNOWLEDGED' } });

    const result = await acknowledgeBreach(5);

    expect(vi.mocked(api.patch)).toHaveBeenCalledWith('/api/compliance-breaches/5/acknowledge');
    expect(result.status).toBe('ACKNOWLEDGED');
  });

  // ─── closeBreach ─────────────────────────────────────────────────────────────

  it('closeBreach patches close endpoint', async () => {
    vi.mocked(api.patch).mockResolvedValue({ data: { breachId: 6, status: 'CLOSED' } });

    const result = await closeBreach(6);

    expect(vi.mocked(api.patch)).toHaveBeenCalledWith('/api/compliance-breaches/6/close');
    expect(result.status).toBe('CLOSED');
  });

  // ─── getAccountDashboard ─────────────────────────────────────────────────────

  it('getAccountDashboard calls dashboard endpoint', async () => {
    const dashboard = { accountId: 1, performanceRecords: [], riskMeasures: [], complianceBreaches: [] };
    vi.mocked(api.get).mockResolvedValue({ data: dashboard });

    const result = await getAccountDashboard(1);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/analytics/accounts/1/dashboard');
    expect(result).toEqual(dashboard);
  });

  it('getAccountDashboard returns dashboard data', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: { accountId: 2, performanceRecords: [{ period: 'DAILY' }] },
    });

    const result = await getAccountDashboard(2);

    expect(result.accountId).toBe(2);
    expect(result.performanceRecords).toHaveLength(1);
  });
});
