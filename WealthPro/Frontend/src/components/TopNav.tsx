import { useNavigate } from 'react-router-dom';
import { Menu } from 'lucide-react';
import { useAuth } from '@/auth/store';
import Logo from './Logo';

interface Props {
  onMenuClick?: () => void;
}

export default function TopNav({ onMenuClick }: Props) {
  const user = useAuth((s) => s.user);
  const logout = useAuth((s) => s.logout);
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  if (!user) return null;

  return (
    <div className="bg-white border-b border-border h-14 px-4 md:px-6 flex items-center justify-between">
      <div className="flex items-center gap-3">
        {/* Hamburger — mobile only */}
        <button
          onClick={onMenuClick}
          className="md:hidden p-1.5 rounded text-text-2 hover:bg-surface"
          aria-label="Open menu"
        >
          <Menu size={20} />
        </button>
        <Logo />
      </div>

      <div className="flex items-center gap-3">
        <span className="hidden sm:inline pill pill-info">{user.role}</span>
        <span className="hidden sm:inline text-sm font-medium">{user.username}</span>
        <button onClick={handleLogout} className="btn btn-ghost btn-sm">
          Logout
        </button>
      </div>
    </div>
  );
}
