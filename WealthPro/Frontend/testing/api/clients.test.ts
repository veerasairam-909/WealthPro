import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import {
  getAllClients,
  getClientById,
  updateClient,
  getKycDocs,
  uploadKyc,
  updateKycStatus,
  getRiskProfile,
  createRiskProfile,
  updateRiskProfile,
} from '@/api/clients';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('clients API', () => {
  beforeEach(() => vi.clearAllMocks());

  // ─── getAllClients ────────────────────────────────────────────────────────────

  it('getAllClients calls /api/clients', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getAllClients();

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/clients');
  });

  it('getAllClients returns list of clients', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ clientId: 1 }, { clientId: 2 }] });

    const result = await getAllClients();

    expect(result).toHaveLength(2);
  });

  // ─── getClientById ────────────────────────────────────────────────────────────

  it('getClientById calls correct endpoint', async () => {
    const client = { clientId: 5, name: 'John Doe' };
    vi.mocked(api.get).mockResolvedValue({ data: client });

    const result = await getClientById(5);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/clients/5');
    expect(result).toEqual(client);
  });

  // ─── updateClient ─────────────────────────────────────────────────────────────

  it('updateClient puts to correct endpoint', async () => {
    const update = { phoneNumber: '9999999999' };
    vi.mocked(api.put).mockResolvedValue({ data: { clientId: 3, ...update } });

    const result = await updateClient(3, update);

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/clients/3', update);
    expect(result.phoneNumber).toBe('9999999999');
  });

  // ─── getKycDocs ───────────────────────────────────────────────────────────────

  it('getKycDocs calls correct endpoint', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getKycDocs(10);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/clients/10/kyc');
  });

  it('getKycDocs returns list of KYC documents', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ kycId: 1, documentType: 'PAN' }] });

    const result = await getKycDocs(10);

    expect(result[0].documentType).toBe('PAN');
  });

  // ─── uploadKyc ────────────────────────────────────────────────────────────────

  it('uploadKyc posts FormData to correct endpoint', async () => {
    const file = new File(['content'], 'pan.pdf', { type: 'application/pdf' });
    vi.mocked(api.post).mockResolvedValue({ data: { kycId: 5 } });

    const result = await uploadKyc(10, 'PAN', file);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/clients/10/kyc', expect.any(FormData));
    expect(result.kycId).toBe(5);
  });

  it('uploadKyc includes documentType and document in FormData', async () => {
    const file = new File(['content'], 'aadhaar.pdf', { type: 'application/pdf' });
    vi.mocked(api.post).mockResolvedValue({ data: {} });

    await uploadKyc(10, 'AADHAAR', file);

    const formData = (vi.mocked(api.post).mock.calls[0][1] as FormData);
    expect(formData.get('documentType')).toBe('AADHAAR');
    expect(formData.get('document')).toBe(file);
  });

  // ─── updateKycStatus ──────────────────────────────────────────────────────────

  it('updateKycStatus puts status to correct endpoint', async () => {
    vi.mocked(api.put).mockResolvedValue({ data: { kycId: 3, status: 'APPROVED' } });

    const result = await updateKycStatus(3, 'APPROVED');

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/clients/kyc/3/status', { status: 'APPROVED' });
    expect(result.status).toBe('APPROVED');
  });

  // ─── getRiskProfile ───────────────────────────────────────────────────────────

  it('getRiskProfile calls correct endpoint with validateStatus', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { riskClass: 'MODERATE' }, status: 200 });

    await getRiskProfile(5);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith(
      '/api/clients/5/risk-profile',
      expect.objectContaining({ validateStatus: expect.any(Function) })
    );
  });

  it('getRiskProfile returns profile data on 200', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { riskClass: 'HIGH' }, status: 200 });

    const result = await getRiskProfile(5);

    expect(result).toEqual({ riskClass: 'HIGH' });
  });

  it('getRiskProfile returns null on 404', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: null, status: 404 });

    const result = await getRiskProfile(99);

    expect(result).toBeNull();
  });

  it('validateStatus function accepts 200 and 404', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: null, status: 200 });
    await getRiskProfile(1);
    const options = vi.mocked(api.get).mock.calls[0][1] as { validateStatus: (s: number) => boolean };
    expect(options.validateStatus(200)).toBe(true);
    expect(options.validateStatus(404)).toBe(true);
    expect(options.validateStatus(500)).toBe(false);
  });

  // ─── createRiskProfile ────────────────────────────────────────────────────────

  it('createRiskProfile posts to correct endpoint', async () => {
    const data = { riskClass: 'LOW', score: 30 };
    vi.mocked(api.post).mockResolvedValue({ data: { clientId: 5, ...data } });

    const result = await createRiskProfile(5, data);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/clients/5/risk-profile', data);
    expect(result.riskClass).toBe('LOW');
  });

  // ─── updateRiskProfile ────────────────────────────────────────────────────────

  it('updateRiskProfile puts to correct endpoint', async () => {
    const data = { riskClass: 'MODERATE' };
    vi.mocked(api.put).mockResolvedValue({ data: { clientId: 5, riskClass: 'MODERATE' } });

    const result = await updateRiskProfile(5, data);

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/clients/5/risk-profile', data);
    expect(result.riskClass).toBe('MODERATE');
  });
});
