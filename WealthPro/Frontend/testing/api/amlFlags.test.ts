import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import {
  getAllAmlFlags,
  getAmlFlagsByClient,
  createAmlFlag,
  requestClosureAmlFlag,
  reviewAmlFlag,
  deleteAmlFlag,
} from '@/api/amlFlags';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('amlFlags API', () => {
  beforeEach(() => vi.clearAllMocks());

  // ─── getAllAmlFlags ───────────────────────────────────────────────────────────

  it('getAllAmlFlags calls /api/aml-flags without params when no status', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getAllAmlFlags();

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/aml-flags', { params: {} });
  });

  it('getAllAmlFlags passes status param when provided', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getAllAmlFlags('OPEN');

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/aml-flags', { params: { status: 'OPEN' } });
  });

  it('getAllAmlFlags returns list of flags', async () => {
    const flags = [{ amlFlagId: 1, status: 'OPEN' }, { amlFlagId: 2, status: 'CLOSED' }];
    vi.mocked(api.get).mockResolvedValue({ data: flags });

    const result = await getAllAmlFlags();

    expect(result).toHaveLength(2);
  });

  it('getAllAmlFlags with CLOSED status returns only CLOSED flags', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ amlFlagId: 3, status: 'CLOSED' }] });

    const result = await getAllAmlFlags('CLOSED');

    expect(result[0].status).toBe('CLOSED');
  });

  // ─── getAmlFlagsByClient ──────────────────────────────────────────────────────

  it('getAmlFlagsByClient calls correct endpoint', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getAmlFlagsByClient(10);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/aml-flags/client/10');
  });

  it('getAmlFlagsByClient returns flags for client', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ amlFlagId: 5, clientId: 10 }] });

    const result = await getAmlFlagsByClient(10);

    expect(result[0].clientId).toBe(10);
  });

  // ─── createAmlFlag ────────────────────────────────────────────────────────────

  it('createAmlFlag posts to /api/aml-flags', async () => {
    const payload = { clientId: 5, flagType: 'SUSPICIOUS_ACTIVITY', description: 'Large cash deposit' };
    const created = { amlFlagId: 1, ...payload };
    vi.mocked(api.post).mockResolvedValue({ data: created });

    const result = await createAmlFlag(payload);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/aml-flags', payload);
    expect(result.amlFlagId).toBe(1);
  });

  it('createAmlFlag accepts optional notes and raisedByUserId', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { amlFlagId: 2 } });

    await createAmlFlag({
      clientId: 3,
      flagType: 'PEP',
      description: 'PEP match',
      notes: 'Reviewed by compliance',
      raisedByUserId: 7,
    });

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/aml-flags', expect.objectContaining({
      notes: 'Reviewed by compliance',
      raisedByUserId: 7,
    }));
  });

  // ─── requestClosureAmlFlag ───────────────────────────────────────────────────

  it('requestClosureAmlFlag puts to correct endpoint', async () => {
    vi.mocked(api.put).mockResolvedValue({ data: { amlFlagId: 3, status: 'CLOSURE_REQUESTED' } });

    const result = await requestClosureAmlFlag(3);

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/aml-flags/3/request-closure');
    expect(result.status).toBe('CLOSURE_REQUESTED');
  });

  // ─── reviewAmlFlag ────────────────────────────────────────────────────────────

  it('reviewAmlFlag puts review payload to correct endpoint', async () => {
    const payload = { status: 'CLOSED', notes: 'False positive' };
    vi.mocked(api.put).mockResolvedValue({ data: { amlFlagId: 4, ...payload } });

    const result = await reviewAmlFlag(4, payload);

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/aml-flags/4/review', payload);
    expect(result.status).toBe('CLOSED');
  });

  it('reviewAmlFlag accepts review with status only', async () => {
    vi.mocked(api.put).mockResolvedValue({ data: { amlFlagId: 5, status: 'OPEN' } });

    await reviewAmlFlag(5, { status: 'OPEN' });

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/aml-flags/5/review', { status: 'OPEN' });
  });

  // ─── deleteAmlFlag ────────────────────────────────────────────────────────────

  it('deleteAmlFlag sends DELETE to correct endpoint', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: null });

    await deleteAmlFlag(6);

    expect(vi.mocked(api.delete)).toHaveBeenCalledWith('/api/aml-flags/6');
  });

  it('deleteAmlFlag returns response data', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: { deleted: true } });

    const result = await deleteAmlFlag(6);

    expect(result).toEqual({ deleted: true });
  });
});
