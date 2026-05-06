import { describe, it, expect } from 'vitest';
import { decodeJWT } from '@/lib/jwt';

// Creates a valid-format JWT (unsigned) that jwt-decode can parse
function makeJWT(payload: object): string {
  const encode = (obj: object) =>
    btoa(JSON.stringify(obj))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=/g, '');
  const header = encode({ alg: 'HS256', typ: 'JWT' });
  const body = encode(payload);
  return `${header}.${body}.fakesignature`;
}

describe('decodeJWT', () => {
  it('extracts username from username field', () => {
    const token = makeJWT({ username: 'john', sub: 'john', roles: 'ROLE_RM' });
    const user = decodeJWT(token);
    expect(user.username).toBe('john');
  });

  it('falls back to sub for username when username field is absent', () => {
    const token = makeJWT({ sub: 'admin1', roles: 'ROLE_ADMIN' });
    const user = decodeJWT(token);
    expect(user.username).toBe('admin1');
  });

  it('parses ADMIN role from ROLE_ prefixed string', () => {
    const token = makeJWT({ sub: 'admin1', roles: 'ROLE_ADMIN' });
    const user = decodeJWT(token);
    expect(user.role).toBe('ADMIN');
  });

  it('parses RM role from ROLE_ prefixed string', () => {
    const token = makeJWT({ sub: 'rm1', roles: 'ROLE_RM' });
    const user = decodeJWT(token);
    expect(user.role).toBe('RM');
  });

  it('parses DEALER role correctly', () => {
    const token = makeJWT({ sub: 'dealer1', roles: 'ROLE_DEALER' });
    const user = decodeJWT(token);
    expect(user.role).toBe('DEALER');
  });

  it('parses COMPLIANCE role correctly', () => {
    const token = makeJWT({ sub: 'comp1', roles: 'ROLE_COMPLIANCE' });
    const user = decodeJWT(token);
    expect(user.role).toBe('COMPLIANCE');
  });

  it('parses role from roles array', () => {
    const token = makeJWT({ sub: 'user1', roles: ['ROLE_RM'] });
    const user = decodeJWT(token);
    expect(user.role).toBe('RM');
  });

  it('defaults to CLIENT for unknown role', () => {
    const token = makeJWT({ sub: 'user1', roles: 'ROLE_UNKNOWN' });
    const user = decodeJWT(token);
    expect(user.role).toBe('CLIENT');
  });

  it('extracts userId from explicit userId field', () => {
    const token = makeJWT({ sub: 'user1', roles: 'ROLE_CLIENT', userId: 42 });
    const user = decodeJWT(token);
    expect(user.userId).toBe(42);
  });

  it('extracts userId from numeric sub field when no explicit userId', () => {
    const token = makeJWT({ sub: '7', roles: 'ROLE_DEALER' });
    const user = decodeJWT(token);
    expect(user.userId).toBe(7);
  });

  it('does not use non-numeric sub as userId', () => {
    const token = makeJWT({ sub: 'adminuser', roles: 'ROLE_ADMIN' });
    const user = decodeJWT(token);
    expect(user.userId).toBeUndefined();
  });

  it('extracts clientId for CLIENT role', () => {
    const token = makeJWT({ sub: 'client1', roles: 'ROLE_CLIENT', clientId: 99 });
    const user = decodeJWT(token);
    expect(user.clientId).toBe(99);
  });

  it('attaches the original token string to returned user', () => {
    const token = makeJWT({ sub: 'admin', roles: 'ROLE_ADMIN' });
    const user = decodeJWT(token);
    expect(user.token).toBe(token);
  });
});
