import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import {
  getAllSecurities,
  getSecurityById,
  createSecurity,
  updateSecurity,
  deleteSecurity,
} from '@/api/securities';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('securities API', () => {
  beforeEach(() => vi.clearAllMocks());

  // ─── getAllSecurities ─────────────────────────────────────────────────────────

  it('getAllSecurities calls /api/securities', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getAllSecurities();

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/securities');
  });

  it('getAllSecurities returns list of securities', async () => {
    const securities = [{ securityId: 1, symbol: 'RELIANCE' }, { securityId: 2, symbol: 'TCS' }];
    vi.mocked(api.get).mockResolvedValue({ data: securities });

    const result = await getAllSecurities();

    expect(result).toHaveLength(2);
    expect(result[0].symbol).toBe('RELIANCE');
  });

  // ─── getSecurityById ──────────────────────────────────────────────────────────

  it('getSecurityById calls correct endpoint', async () => {
    const security = { securityId: 5, symbol: 'INFY' };
    vi.mocked(api.get).mockResolvedValue({ data: security });

    const result = await getSecurityById(5);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/securities/5');
    expect(result).toEqual(security);
  });

  it('getSecurityById returns security data', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { securityId: 3, assetClass: 'EQUITY' } });

    const result = await getSecurityById(3);

    expect(result.assetClass).toBe('EQUITY');
  });

  // ─── createSecurity ───────────────────────────────────────────────────────────

  it('createSecurity posts to /api/securities', async () => {
    const payload = { symbol: 'HDFC', assetClass: 'EQUITY', currency: 'INR', country: 'IN', status: 'ACTIVE' };
    const created = { securityId: 10, ...payload };
    vi.mocked(api.post).mockResolvedValue({ data: created });

    const result = await createSecurity(payload);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/securities', payload);
    expect(result.securityId).toBe(10);
  });

  it('createSecurity accepts optional currentPrice', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { securityId: 11 } });

    await createSecurity({
      symbol: 'WIPRO',
      assetClass: 'EQUITY',
      currency: 'INR',
      country: 'IN',
      status: 'ACTIVE',
      currentPrice: 450.0,
    });

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/securities', expect.objectContaining({
      currentPrice: 450.0,
    }));
  });

  // ─── updateSecurity ───────────────────────────────────────────────────────────

  it('updateSecurity puts to correct endpoint', async () => {
    const update = { status: 'INACTIVE' };
    vi.mocked(api.put).mockResolvedValue({ data: { securityId: 5, ...update } });

    const result = await updateSecurity(5, update);

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/securities/5', update);
    expect(result.status).toBe('INACTIVE');
  });

  it('updateSecurity can update currentPrice', async () => {
    vi.mocked(api.put).mockResolvedValue({ data: { securityId: 5, currentPrice: 500.0 } });

    await updateSecurity(5, { currentPrice: 500.0 });

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/securities/5', { currentPrice: 500.0 });
  });

  // ─── deleteSecurity ───────────────────────────────────────────────────────────

  it('deleteSecurity sends DELETE to correct endpoint', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: null });

    await deleteSecurity(7);

    expect(vi.mocked(api.delete)).toHaveBeenCalledWith('/api/securities/7');
  });

  it('deleteSecurity resolves without returning data', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: null });

    await expect(deleteSecurity(7)).resolves.toBeUndefined();
  });
});
