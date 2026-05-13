import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import { login, logoutApi, registerClient, registerStaff } from '@/api/auth';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  // ─── login ───────────────────────────────────────────────────────────────────

  it('login posts credentials to /auth/login', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: 'myjwt.token.here' });

    await login('admin', 'secret');

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/auth/login', {
      username: 'admin',
      password: 'secret',
    });
  });

  it('login returns token when server returns raw string', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: 'raw.jwt.token' });

    const token = await login('admin', 'pass');

    expect(token).toBe('raw.jwt.token');
  });

  it('login returns token when server returns object with token field', async () => {
    vi.mocked(api.post).mockResolvedValue({
      data: { token: 'object.jwt.token', userId: 1, role: 'ROLE_ADMIN' },
    });

    const token = await login('admin', 'pass');

    expect(token).toBe('object.jwt.token');
  });

  it('login saves userId to wp_user_id_map in localStorage', async () => {
    vi.mocked(api.post).mockResolvedValue({
      data: { token: 'jwt.tok.en', userId: 42, role: 'ROLE_RM' },
    });

    await login('rm1', 'pass');

    const map = JSON.parse(localStorage.getItem('wp_user_id_map') || '{}');
    expect(map['rm1']).toMatchObject({ userId: 42 });
  });

  it('login works when response data has no userId (does not throw)', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: 'simple.jwt.token' });

    await expect(login('user', 'pass')).resolves.toBe('simple.jwt.token');
  });

  // ─── logoutApi ───────────────────────────────────────────────────────────────

  it('logoutApi calls /auth/logout', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: null });

    await logoutApi();

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/auth/logout');
  });

  it('logoutApi does not throw when server returns an error', async () => {
    vi.mocked(api.post).mockRejectedValue(new Error('Network error'));

    await expect(logoutApi()).resolves.toBeUndefined();
  });

  it('logoutApi resolves to undefined', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: null });

    const result = await logoutApi();

    expect(result).toBeUndefined();
  });

  // ─── registerClient ──────────────────────────────────────────────────────────

  it('registerClient posts to /auth/register/client', async () => {
    const data = { username: 'newclient', password: 'pass', email: 'c@test.com' };
    vi.mocked(api.post).mockResolvedValue({ data: { userId: 5 } });

    const result = await registerClient(data);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/auth/register/client', data);
    expect(result).toEqual({ userId: 5 });
  });

  // ─── registerStaff ───────────────────────────────────────────────────────────

  it('registerStaff posts to /auth/users/register', async () => {
    const data = { username: 'newrm', password: 'pass', role: 'ROLE_RM' };
    vi.mocked(api.post).mockResolvedValue({ data: { userId: 10 } });

    const result = await registerStaff(data);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/auth/users/register', data);
    expect(result).toEqual({ userId: 10 });
  });
});
