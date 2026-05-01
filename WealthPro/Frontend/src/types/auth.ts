export type Role = 'ADMIN' | 'RM' | 'DEALER' | 'COMPLIANCE' | 'CLIENT';

export interface User {
  username: string;
  role: Role;
  userId?: number;   // gateway user ID — present when the backend includes it in the JWT
  clientId?: number; // only set for CLIENT role
  token: string;
}
