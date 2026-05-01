import { Link } from 'react-router-dom';
import { useAuth, getHomeForRole } from '@/auth/store';

export default function Forbidden() {
  const user = useAuth((s) => s.user);
  const homePath = user ? getHomeForRole(user.role) : '/login';

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="text-center">
        <h1 className="text-5xl font-bold text-primary mb-3">403</h1>
        <h2 className="text-xl font-semibold mb-2">Access denied</h2>
        <p className="text-text-2 mb-5">You don't have permission to view this page.</p>
        <Link to={homePath} className="btn btn-primary">
          Back to dashboard
        </Link>
      </div>
    </div>
  );
}
