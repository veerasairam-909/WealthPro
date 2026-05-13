import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import {
  getRecommendationsByClientId,
  getAllModelPortfolios,
  createModelPortfolio,
  updateModelPortfolio,
  deleteModelPortfolio,
  createRecommendation,
  updateRecommendationStatus,
  deleteRecommendation,
} from '@/api/recommendations';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('recommendations API', () => {
  beforeEach(() => vi.clearAllMocks());

  // ─── getRecommendationsByClientId ────────────────────────────────────────────

  it('getRecommendationsByClientId calls correct endpoint', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getRecommendationsByClientId(10);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/recommendations/client/10');
  });

  it('getRecommendationsByClientId returns list', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ recoId: 1 }, { recoId: 2 }] });

    const result = await getRecommendationsByClientId(10);

    expect(result).toHaveLength(2);
  });

  // ─── getAllModelPortfolios ────────────────────────────────────────────────────

  it('getAllModelPortfolios calls /api/model-portfolios', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getAllModelPortfolios();

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/model-portfolios');
  });

  it('getAllModelPortfolios returns portfolios', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ portfolioId: 1, name: 'Growth' }] });

    const result = await getAllModelPortfolios();

    expect(result[0].name).toBe('Growth');
  });

  // ─── createModelPortfolio ─────────────────────────────────────────────────────

  it('createModelPortfolio posts to /api/model-portfolios', async () => {
    const payload = { name: 'Conservative', riskClass: 'LOW' };
    vi.mocked(api.post).mockResolvedValue({ data: { portfolioId: 3, ...payload } });

    const result = await createModelPortfolio(payload);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/model-portfolios', payload);
    expect(result.portfolioId).toBe(3);
  });

  // ─── updateModelPortfolio ─────────────────────────────────────────────────────

  it('updateModelPortfolio puts to correct endpoint', async () => {
    const update = { name: 'Aggressive' };
    vi.mocked(api.put).mockResolvedValue({ data: { portfolioId: 2, ...update } });

    const result = await updateModelPortfolio(2, update);

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/model-portfolios/2', update);
    expect(result.name).toBe('Aggressive');
  });

  // ─── deleteModelPortfolio ─────────────────────────────────────────────────────

  it('deleteModelPortfolio sends DELETE to correct endpoint', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: null });

    await deleteModelPortfolio(4);

    expect(vi.mocked(api.delete)).toHaveBeenCalledWith('/api/model-portfolios/4');
  });

  it('deleteModelPortfolio returns response data', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: { deleted: true } });

    const result = await deleteModelPortfolio(4);

    expect(result).toEqual({ deleted: true });
  });

  // ─── createRecommendation ────────────────────────────────────────────────────

  it('createRecommendation posts to /api/recommendations', async () => {
    const payload = {
      clientId: 5,
      riskClass: 'MODERATE',
      proposalJson: '{}',
      proposedDate: '2025-01-01',
      status: 'PENDING',
    };
    vi.mocked(api.post).mockResolvedValue({ data: { recoId: 10, ...payload } });

    const result = await createRecommendation(payload);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/recommendations', payload);
    expect(result.recoId).toBe(10);
  });

  // ─── updateRecommendationStatus ──────────────────────────────────────────────

  it('updateRecommendationStatus patches status endpoint', async () => {
    vi.mocked(api.patch).mockResolvedValue({ data: { recoId: 7, status: 'APPROVED' } });

    const result = await updateRecommendationStatus(7, 'APPROVED');

    expect(vi.mocked(api.patch)).toHaveBeenCalledWith(
      '/api/recommendations/7/status',
      'APPROVED',
      { headers: { 'Content-Type': 'application/json' } }
    );
    expect(result.status).toBe('APPROVED');
  });

  // ─── deleteRecommendation ────────────────────────────────────────────────────

  it('deleteRecommendation sends DELETE to correct endpoint', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: null });

    await deleteRecommendation(8);

    expect(vi.mocked(api.delete)).toHaveBeenCalledWith('/api/recommendations/8');
  });

  it('deleteRecommendation returns response data', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: { deleted: true } });

    const result = await deleteRecommendation(8);

    expect(result).toEqual({ deleted: true });
  });
});
