import { api } from './client';

export async function logoutApi(): Promise<void> {
  // Tell the gateway to blocklist the current token so it cannot be reused
  // even if someone captured it. Fire-and-forget — local state is cleared
  // regardless of whether this call succeeds.
  try {
    await api.post('/auth/logout');
  } catch {
    // Ignore errors (network down, already expired, etc.)
    // The frontend clears state either way.
  }
}

export async function login(username: string, password: string) {
  const res = await api.post('/auth/login', { username, password });

  // Backend now returns { token, userId, role } — also handles legacy raw-string JWT.
  const token: string = typeof res.data === 'string' ? res.data : res.data.token;

  // Write userId + role to the shared map so every page can look up IDs
  // without any admin API call. The map is used by RmNotifications and
  // the Compliance breach notification modal.
  if (res.data && typeof res.data === 'object') {
    const uid  = res.data.userId ?? res.data.user_id ?? res.data.id ?? res.data.uid;
    const role = res.data.role   ?? res.data.roles   ?? '';
    if (uid != null && Number(uid) > 0) {
      try {
        const map = JSON.parse(localStorage.getItem('wp_user_id_map') || '{}');
        map[username] = { userId: Number(uid), role: String(role).replace('ROLE_', '').toUpperCase() };
        localStorage.setItem('wp_user_id_map', JSON.stringify(map));
      } catch { /* storage unavailable */ }
    }
  }

  return token;
}

export async function registerClient(data: any) {
  const res = await api.post('/auth/register/client', data);
  return res.data;
}

export async function registerStaff(data: any) {
  const res = await api.post('/auth/users/register', data);
  return res.data;
}
