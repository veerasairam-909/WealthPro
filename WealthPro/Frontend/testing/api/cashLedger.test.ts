import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import {
  getCashLedgerByAccountId,
  getBalanceByAccountId,
  createCashLedgerEntry,
} from '@/api/cashLedger';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('cashLedger API', () => {
  beforeEach(() => vi.clearAllMocks());

  // ─── getCashLedgerByAccountId ────────────────────────────────────────────────

  it('getCashLedgerByAccountId calls correct endpoint', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getCashLedgerByAccountId(5);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/cash-ledger/account/5');
  });

  it('getCashLedgerByAccountId returns ledger entries', async () => {
    const entries = [{ entryId: 1, amount: 5000, txnType: 'CREDIT' }];
    vi.mocked(api.get).mockResolvedValue({ data: entries });

    const result = await getCashLedgerByAccountId(5);

    expect(result).toEqual(entries);
  });

  it('getCashLedgerByAccountId returns empty list when no entries', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    const result = await getCashLedgerByAccountId(99);

    expect(result).toEqual([]);
  });

  // ─── getBalanceByAccountId ───────────────────────────────────────────────────

  it('getBalanceByAccountId calls balance endpoint', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { balance: 100000, currency: 'INR' } });

    const result = await getBalanceByAccountId(5);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/cash-ledger/account/5/balance');
    expect(result.balance).toBe(100000);
  });

  it('getBalanceByAccountId returns balance data', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { balance: 250000 } });

    const result = await getBalanceByAccountId(3);

    expect(result.balance).toBe(250000);
  });

  // ─── createCashLedgerEntry ───────────────────────────────────────────────────

  it('createCashLedgerEntry posts to /api/cash-ledger', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { entryId: 1 } });

    await createCashLedgerEntry({ accountId: 1, amount: 5000, txnType: 'CREDIT' });

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/cash-ledger', expect.any(Object));
  });

  it('createCashLedgerEntry defaults currency to INR', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { entryId: 1 } });

    await createCashLedgerEntry({ accountId: 1, amount: 5000, txnType: 'CREDIT' });

    const payload = vi.mocked(api.post).mock.calls[0][1] as any;
    expect(payload.currency).toBe('INR');
  });

  it('createCashLedgerEntry uses provided currency', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { entryId: 2 } });

    await createCashLedgerEntry({ accountId: 1, amount: 5000, txnType: 'CREDIT', currency: 'USD' });

    const payload = vi.mocked(api.post).mock.calls[0][1] as any;
    expect(payload.currency).toBe('USD');
  });

  it('createCashLedgerEntry defaults narrative to Fund deposit', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { entryId: 1 } });

    await createCashLedgerEntry({ accountId: 1, amount: 1000, txnType: 'CREDIT' });

    const payload = vi.mocked(api.post).mock.calls[0][1] as any;
    expect(payload.narrative).toBe('Fund deposit');
  });

  it('createCashLedgerEntry uses provided narrative', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { entryId: 3 } });

    await createCashLedgerEntry({
      accountId: 1,
      amount: 1000,
      txnType: 'DEBIT',
      narrative: 'Custom withdrawal',
    });

    const payload = vi.mocked(api.post).mock.calls[0][1] as any;
    expect(payload.narrative).toBe('Custom withdrawal');
  });

  it('createCashLedgerEntry sets txnDate when not provided', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { entryId: 4 } });

    await createCashLedgerEntry({ accountId: 1, amount: 2000, txnType: 'CREDIT' });

    const payload = vi.mocked(api.post).mock.calls[0][1] as any;
    expect(payload.txnDate).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });

  it('createCashLedgerEntry returns created entry', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { entryId: 5, amount: 3000 } });

    const result = await createCashLedgerEntry({ accountId: 2, amount: 3000, txnType: 'CREDIT' });

    expect(result.entryId).toBe(5);
  });
});
