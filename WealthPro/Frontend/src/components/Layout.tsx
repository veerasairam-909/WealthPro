import { Outlet } from 'react-router-dom';
import { useAuth } from '@/auth/store';
import TopNav from './TopNav';
import Sidebar from './Sidebar';

export default function Layout() {
  const user = useAuth((s) => s.user);
  if (!user) return null;

  return (
    <div className="h-full flex flex-col">
      <TopNav />
      <div className="flex-1 flex overflow-hidden">
        <Sidebar role={user.role} />
        <div className="flex-1 overflow-auto p-6">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
