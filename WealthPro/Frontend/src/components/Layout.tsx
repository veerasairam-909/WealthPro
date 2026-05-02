import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { useAuth } from '@/auth/store';
import TopNav from './TopNav';
import Sidebar from './Sidebar';

export default function Layout() {
  const user = useAuth((s) => s.user);
  const [mobileOpen, setMobileOpen] = useState(false);

  if (!user) return null;

  return (
    <div className="h-full flex flex-col">
      <TopNav onMenuClick={() => setMobileOpen((v) => !v)} />
      <div className="flex-1 flex overflow-hidden">
        <Sidebar
          role={user.role}
          username={user.username}
          isMobileOpen={mobileOpen}
          onClose={() => setMobileOpen(false)}
        />
        <div className="flex-1 overflow-auto p-4 md:p-6">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
