import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import {
  getAccountsByClientId,
  getAccountById,
  getAllAccounts,
  createAccount,
} from '@/api/accounts';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('accounts API', () => {
  beforeEach(() => vi.clearAllMocks());

  // ─── getAccountsByClientId ───────────────────────────────────────────────────

  it('getAccountsByClientId calls the correct endpoint', async () => {
    const accounts = [{ accountId: 1, clientId: 5 }];
    vi.mocked(api.get).mockResolvedValue({ data: accounts });

    const result = await getAccountsByClientId(5);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/accounts/client/5');
    expect(result).toEqual(accounts);
  });

  it('getAccountsByClientId returns empty array when no accounts', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    const result = await getAccountsByClientId(99);

    expect(result).toEqual([]);
  });

  // ─── getAccountById ──────────────────────────────────────────────────────────

  it('getAccountById calls the correct endpoint', async () => {
    const account = { accountId: 10, accountType: 'INDIVIDUAL' };
    vi.mocked(api.get).mockResolvedValue({ data: account });

    const result = await getAccountById(10);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/accounts/10');
    expect(result).toEqual(account);
  });

  it('getAccountById returns the response data', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { accountId: 7 } });

    const result = await getAccountById(7);

    expect(result).toEqual({ accountId: 7 });
  });

  // ─── getAllAccounts ──────────────────────────────────────────────────────────

  it('getAllAccounts calls /api/accounts', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getAllAccounts();

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/accounts');
  });

  it('getAllAccounts returns list of accounts', async () => {
    const accounts = [{ accountId: 1 }, { accountId: 2 }];
    vi.mocked(api.get).mockResolvedValue({ data: accounts });

    const result = await getAllAccounts();

    expect(result).toHaveLength(2);
  });

  // ─── createAccount ───────────────────────────────────────────────────────────

  it('createAccount posts to /api/accounts with data', async () => {
    const payload = { clientId: 5, accountType: 'INDIVIDUAL', baseCurrency: 'INR', status: 'ACTIVE' };
    const created = { accountId: 1, ...payload };
    vi.mocked(api.post).mockResolvedValue({ data: created });

    const result = await createAccount(payload);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/accounts', payload);
    expect(result).toEqual(created);
  });

  it('createAccount returns created account object', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { accountId: 20, clientId: 3 } });

    const result = await createAccount({ clientId: 3, accountType: 'JOINT', baseCurrency: 'USD', status: 'ACTIVE' });

    expect(result.accountId).toBe(20);
  });
});
