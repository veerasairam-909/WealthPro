import { Navigate } from 'react-router-dom';
import { useAuth } from './store';
import type { Role } from '@/types/auth';

interface Props {
  allow: Role | Role[];
  children: React.ReactNode;
}

export default function ProtectedRoute(props: Props) {
  const user = useAuth((s) => s.user);

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  const allowedRoles = Array.isArray(props.allow) ? props.allow : [props.allow];
  if (!allowedRoles.includes(user.role)) {
    return <Navigate to="/403" replace />;
  }

  return <>{props.children}</>;
}
