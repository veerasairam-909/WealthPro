import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import { getHoldingsByAccountId, getHoldingById } from '@/api/holdings';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('holdings API', () => {
  beforeEach(() => vi.clearAllMocks());

  // ─── getHoldingsByAccountId ──────────────────────────────────────────────────

  it('getHoldingsByAccountId calls correct endpoint', async () => {
    const holdings = [{ holdingId: 1, securityId: 100 }];
    vi.mocked(api.get).mockResolvedValue({ data: holdings });

    const result = await getHoldingsByAccountId(5);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/holdings/account/5');
    expect(result).toEqual(holdings);
  });

  it('getHoldingsByAccountId returns empty list when no holdings', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    const result = await getHoldingsByAccountId(99);

    expect(result).toEqual([]);
  });

  it('getHoldingsByAccountId returns data from response', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ holdingId: 3 }, { holdingId: 4 }] });

    const result = await getHoldingsByAccountId(2);

    expect(result).toHaveLength(2);
  });

  // ─── getHoldingById ──────────────────────────────────────────────────────────

  it('getHoldingById calls correct endpoint', async () => {
    const holding = { holdingId: 7, securityId: 200 };
    vi.mocked(api.get).mockResolvedValue({ data: holding });

    const result = await getHoldingById(7);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/holdings/7');
    expect(result).toEqual(holding);
  });

  it('getHoldingById returns the holding data', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { holdingId: 12, quantity: 50 } });

    const result = await getHoldingById(12);

    expect(result.quantity).toBe(50);
  });
});
