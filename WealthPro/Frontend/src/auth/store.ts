import { create } from 'zustand';
import type { User, Role } from '@/types/auth';
import { decodeJWT } from '@/lib/jwt';

interface AuthState {
  user: User | null;
  setToken: (token: string) => void;
  logout: () => void;
}

// load user from localStorage at startup
function loadUser(): User | null {
  const token = localStorage.getItem('token');
  if (!token) return null;
  try {
    return decodeJWT(token);
  } catch (e) {
    return null;
  }
}

export const useAuth = create<AuthState>((set) => ({
  user: loadUser(),
  setToken: (token: string) => {
    localStorage.setItem('token', token);
    const user = decodeJWT(token);
    // If the JWT carried a userId, persist it in the shared map so every role
    // (RM, compliance, etc.) can look up their own or others' IDs without an admin call.
    if (user.userId && user.username) {
      try {
        const map = JSON.parse(localStorage.getItem('wp_user_id_map') || '{}');
        map[user.username] = { userId: user.userId, role: user.role };
        localStorage.setItem('wp_user_id_map', JSON.stringify(map));
      } catch { /* storage unavailable */ }
    }
    set({ user });
  },
  logout: () => {
    localStorage.removeItem('token');
    set({ user: null });
  },
}));

// home page for each role
export function getHomeForRole(role: Role): string {
  if (role === 'ADMIN') return '/admin/dashboard';
  if (role === 'RM') return '/rm/clients';
  if (role === 'DEALER') return '/dealer/orders';
  if (role === 'COMPLIANCE') return '/compliance/breaches';
  return '/me/dashboard';
}
