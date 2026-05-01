import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/auth/store';
import Logo from './Logo';

export default function TopNav() {
  const user = useAuth((s) => s.user);
  const logout = useAuth((s) => s.logout);
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  if (!user) return null;

  return (
    <div className="bg-white border-b border-border h-14 px-6 flex items-center justify-between">
      <Logo />

      <div className="flex items-center gap-3">
        <span className="pill pill-info">{user.role}</span>
        <span className="text-sm font-medium">{user.username}</span>
        <button onClick={handleLogout} className="btn btn-ghost btn-sm">
          Logout
        </button>
      </div>
    </div>
  );
}
