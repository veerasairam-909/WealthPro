import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import { getAllUsers, getUserByUsername, deleteUser, getAuditLogs } from '@/api/admin';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('admin API', () => {
  beforeEach(() => vi.clearAllMocks());

  // ─── getAllUsers ─────────────────────────────────────────────────────────────

  it('getAllUsers calls /auth/users', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getAllUsers();

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/auth/users');
  });

  it('getAllUsers returns list of users', async () => {
    const users = [{ username: 'admin' }, { username: 'rm1' }];
    vi.mocked(api.get).mockResolvedValue({ data: users });

    const result = await getAllUsers();

    expect(result).toHaveLength(2);
  });

  // ─── getUserByUsername ───────────────────────────────────────────────────────

  it('getUserByUsername calls correct endpoint', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { username: 'admin' }, status: 200 });

    await getUserByUsername('admin');

    expect(vi.mocked(api.get)).toHaveBeenCalledWith(
      '/auth/users/admin',
      expect.objectContaining({ validateStatus: expect.any(Function) })
    );
  });

  it('getUserByUsername returns user data on 200', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { username: 'admin', role: 'ADMIN' }, status: 200 });

    const result = await getUserByUsername('admin');

    expect(result).toEqual({ username: 'admin', role: 'ADMIN' });
  });

  it('getUserByUsername returns null on 403', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: null, status: 403 });

    const result = await getUserByUsername('admin');

    expect(result).toBeNull();
  });

  it('getUserByUsername returns null on 404', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: null, status: 404 });

    const result = await getUserByUsername('nobody');

    expect(result).toBeNull();
  });

  // ─── deleteUser ──────────────────────────────────────────────────────────────

  it('deleteUser sends DELETE to correct endpoint', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: null });

    await deleteUser('rm1');

    expect(vi.mocked(api.delete)).toHaveBeenCalledWith('/auth/users/rm1');
  });

  it('deleteUser resolves without returning data', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: null });

    await expect(deleteUser('rm1')).resolves.toBeUndefined();
  });

  // ─── getAuditLogs ────────────────────────────────────────────────────────────

  it('getAuditLogs calls /auth/audit', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getAuditLogs();

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/auth/audit', { params: undefined });
  });

  it('getAuditLogs passes params when provided', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getAuditLogs({ username: 'admin', limit: 10 });

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/auth/audit', {
      params: { username: 'admin', limit: 10 },
    });
  });

  it('getAuditLogs returns audit log entries', async () => {
    const logs = [{ auditId: 1, username: 'admin' }, { auditId: 2, username: 'rm1' }];
    vi.mocked(api.get).mockResolvedValue({ data: logs });

    const result = await getAuditLogs();

    expect(result).toHaveLength(2);
    expect(result[0].auditId).toBe(1);
  });
});
