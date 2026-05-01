import { jwtDecode } from 'jwt-decode';
import type { User, Role } from '@/types/auth';

export function decodeJWT(token: string): User {
  const claims: any = jwtDecode(token);

  // get username
  const username = claims.username || claims.sub || '';

  // get role - it can come as string or array
  let roleStr = '';
  if (Array.isArray(claims.roles)) {
    roleStr = claims.roles[0];
  } else if (typeof claims.roles === 'string') {
    roleStr = claims.roles;
  } else if (claims.role) {
    roleStr = claims.role;
  }

  // remove ROLE_ prefix and brackets
  roleStr = String(roleStr).replace('ROLE_', '').replace('[', '').replace(']', '').trim().toUpperCase();

  let role: Role = 'CLIENT';
  if (roleStr === 'ADMIN') role = 'ADMIN';
  else if (roleStr === 'RM') role = 'RM';
  else if (roleStr === 'DEALER') role = 'DEALER';
  else if (roleStr === 'COMPLIANCE') role = 'COMPLIANCE';
  else role = 'CLIENT';

  // userId — try every field name different Spring backends use
  let userId: number | undefined = undefined;

  // 1. Explicit numeric fields
  const rawUserId = claims.userId ?? claims.user_id ?? claims.uid ?? claims.id;
  if (rawUserId !== undefined && rawUserId !== null) {
    const n = Number(rawUserId);
    if (!isNaN(n) && n > 0) userId = n;
  }

  // 2. `sub` claim — Spring's setSubject() often stores the numeric DB primary key
  //    e.g. .setSubject(String.valueOf(user.getId()))
  //    Only use it when it looks like a positive integer, not a username string
  if (!userId && claims.sub !== undefined) {
    const subNum = Number(claims.sub);
    if (!isNaN(subNum) && subNum > 0 && String(subNum) === String(claims.sub).trim()) {
      userId = subNum;
    }
  }

  let clientId: number | undefined = undefined;
  if (claims.clientId) {
    clientId = Number(claims.clientId);
  }

  return { username, role, userId, clientId, token };
}
