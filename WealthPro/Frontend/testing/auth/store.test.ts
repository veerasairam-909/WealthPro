import { describe, it, expect, beforeEach } from 'vitest';
import { useAuth, getHomeForRole } from '@/auth/store';

// Creates a valid-format JWT (unsigned) that jwt-decode can parse
function makeJWT(payload: object): string {
  const encode = (obj: object) =>
    btoa(JSON.stringify(obj))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=/g, '');
  return `${encode({ alg: 'HS256', typ: 'JWT' })}.${encode(payload)}.sig`;
}

beforeEach(() => {
  localStorage.clear();
  useAuth.setState({ user: null });
});

// ─── auth store ───────────────────────────────────────────────────────────────

describe('useAuth store', () => {
  it('starts with null user when no token in localStorage', () => {
    expect(useAuth.getState().user).toBeNull();
  });

  it('setToken decodes the JWT and sets user in state', () => {
    const token = makeJWT({ sub: 'admin1', roles: 'ROLE_ADMIN', userId: 1 });
    useAuth.getState().setToken(token);
    const user = useAuth.getState().user;
    expect(user).not.toBeNull();
    expect(user?.role).toBe('ADMIN');
    expect(user?.username).toBe('admin1');
  });

  it('setToken stores the token in localStorage', () => {
    const token = makeJWT({ sub: 'user1', roles: 'ROLE_CLIENT' });
    useAuth.getState().setToken(token);
    expect(localStorage.getItem('token')).toBe(token);
  });

  it('setToken correctly decodes RM role', () => {
    const token = makeJWT({ sub: 'rm1', roles: 'ROLE_RM', userId: 5 });
    useAuth.getState().setToken(token);
    expect(useAuth.getState().user?.role).toBe('RM');
  });

  it('setToken correctly decodes CLIENT role with clientId', () => {
    const token = makeJWT({ sub: 'client1', roles: 'ROLE_CLIENT', clientId: 10 });
    useAuth.getState().setToken(token);
    const user = useAuth.getState().user;
    expect(user?.role).toBe('CLIENT');
    expect(user?.clientId).toBe(10);
  });

  it('logout sets user to null', () => {
    const token = makeJWT({ sub: 'user1', roles: 'ROLE_CLIENT' });
    useAuth.getState().setToken(token);
    useAuth.getState().logout();
    expect(useAuth.getState().user).toBeNull();
  });

  it('logout removes the token from localStorage', () => {
    const token = makeJWT({ sub: 'user1', roles: 'ROLE_CLIENT' });
    useAuth.getState().setToken(token);
    useAuth.getState().logout();
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('user remains null after logout', () => {
    useAuth.getState().logout();
    expect(useAuth.getState().user).toBeNull();
  });
});

// ─── getHomeForRole ───────────────────────────────────────────────────────────

describe('getHomeForRole', () => {
  it('returns /admin/dashboard for ADMIN role', () => {
    expect(getHomeForRole('ADMIN')).toBe('/admin/dashboard');
  });

  it('returns /rm/clients for RM role', () => {
    expect(getHomeForRole('RM')).toBe('/rm/clients');
  });

  it('returns /dealer/orders for DEALER role', () => {
    expect(getHomeForRole('DEALER')).toBe('/dealer/orders');
  });

  it('returns /compliance/breaches for COMPLIANCE role', () => {
    expect(getHomeForRole('COMPLIANCE')).toBe('/compliance/breaches');
  });

  it('returns /me/dashboard for CLIENT role', () => {
    expect(getHomeForRole('CLIENT')).toBe('/me/dashboard');
  });
});
