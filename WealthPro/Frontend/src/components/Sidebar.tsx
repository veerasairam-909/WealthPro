import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Users, History, UserPlus, BookMarked, Target,
  Building2, Bell, Layers, ShieldCheck, FileSearch, FileText,
  AlertTriangle, CheckCircle, XCircle, Activity, Scale, Flag,
  PieChart, ShoppingBag, Receipt, CreditCard, ClipboardList,
  BarChart3, TrendingUp,
} from 'lucide-react';
import type { Role } from '@/types/auth';

interface MenuItem {
  to: string;
  label: string;
  Icon: React.ElementType;
}

interface Props {
  role: Role;
  username?: string;
  isMobileOpen?: boolean;
  onClose?: () => void;
}

function buildMenu(role: Role): MenuItem[] {
  if (role === 'ADMIN') {
    return [
      { to: '/admin/dashboard',        label: 'Dashboard',        Icon: LayoutDashboard },
      { to: '/admin/users',            label: 'Users',             Icon: Users },
      { to: '/admin/securities',       label: 'Securities',        Icon: ShieldCheck },
      { to: '/admin/model-portfolios', label: 'Model Portfolios',  Icon: BarChart3 },
      { to: '/admin/audit',            label: 'Audit Log',         Icon: History },
    ];
  }
  if (role === 'RM') {
    return [
      { to: '/rm/clients',           label: 'Clients',            Icon: Users },
      { to: '/rm/onboard',           label: 'Onboard Client',     Icon: UserPlus },
      { to: '/rm/recommendations',   label: 'Recommendations',    Icon: BookMarked },
      { to: '/rm/goals',             label: 'Goals',              Icon: Target },
      { to: '/rm/analytics',         label: 'Analytics',          Icon: TrendingUp },
      { to: '/rm/corporate-actions', label: 'Corporate Actions',  Icon: Building2 },
      { to: '/rm/notifications',     label: 'Notifications',      Icon: Bell },
    ];
  }
  if (role === 'DEALER') {
    return [
      { to: '/dealer/orders',        label: 'Order Blotter',   Icon: Layers },
      { to: '/dealer/securities',    label: 'Securities',      Icon: ShieldCheck },
      { to: '/dealer/research-notes', label: 'Research Notes', Icon: FileSearch },
      { to: '/dealer/product-terms', label: 'Product Terms',   Icon: FileText },
    ];
  }
  if (role === 'COMPLIANCE') {
    return [
      { to: '/compliance/breaches',      label: 'Breaches',          Icon: AlertTriangle },
      { to: '/compliance/kyc-approval',  label: 'KYC Approval',      Icon: CheckCircle },
      { to: '/compliance/failed-orders', label: 'Failed Orders',     Icon: XCircle },
      { to: '/compliance/risk-monitor',  label: 'Risk Monitor',      Icon: Activity },
      { to: '/compliance/rules',         label: 'Suitability Rules', Icon: Scale },
      { to: '/compliance/aml-flags',     label: 'AML Flags',         Icon: Flag },
      { to: '/compliance/audit',         label: 'Audit Log',         Icon: History },
    ];
  }
  // CLIENT
  return [
    { to: '/me/dashboard',    label: 'Dashboard',       Icon: LayoutDashboard },
    { to: '/me/products',     label: 'Browse & Invest', Icon: ShoppingBag },
    { to: '/me/orders',       label: 'My Orders',       Icon: Receipt },
    { to: '/me/holdings',     label: 'Holdings',        Icon: PieChart },
    { to: '/me/kyc',          label: 'My KYC',          Icon: CreditCard },
    { to: '/me/risk-profile', label: 'Risk Profile',    Icon: ShieldCheck },
    { to: '/me/goals',        label: 'Goals',           Icon: Target },
    { to: '/me/notifications', label: 'Notifications',  Icon: Bell },
    { to: '/me/reviews',      label: 'Reviews',         Icon: ClipboardList },
  ];
}

// Initials avatar colours per role
function avatarClasses(role: Role): string {
  if (role === 'ADMIN')      return 'bg-accent-soft text-accent';
  if (role === 'RM')         return 'bg-primary-soft text-primary';
  if (role === 'DEALER')     return 'bg-warn-soft text-warn';
  if (role === 'COMPLIANCE') return 'bg-danger-soft text-danger';
  return 'bg-success-soft text-success';           // CLIENT
}

function pillClasses(role: Role): string {
  if (role === 'ADMIN')      return 'bg-accent-soft text-accent';
  if (role === 'RM')         return 'bg-primary-soft text-primary';
  if (role === 'DEALER')     return 'bg-warn-soft text-warn';
  if (role === 'COMPLIANCE') return 'bg-danger-soft text-danger';
  return 'bg-success-soft text-success';
}

function SidebarPanel({ role, username }: { role: Role; username?: string }) {
  const menu = buildMenu(role);

  return (
    <div className="w-56 bg-white border-r border-border h-full flex flex-col">
      <nav className="flex-1 p-4 overflow-y-auto">
        <p className="text-xs text-text-3 uppercase mb-3 font-semibold tracking-wider">Menu</p>
        {menu.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              'flex items-center gap-2.5 px-3 py-2 rounded text-sm mb-0.5 ' +
              (isActive
                ? 'bg-primary-soft text-primary font-medium'
                : 'text-text-2 hover:bg-surface')
            }
          >
            {({ isActive }) => (
              <>
                <item.Icon
                  size={15}
                  strokeWidth={isActive ? 2.2 : 1.8}
                  className="shrink-0"
                />
                <span className="truncate">{item.label}</span>
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* User profile card */}
      {username && (
        <div className="border-t border-border px-4 py-3 flex items-center gap-2.5">
          <div
            className={
              'w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold shrink-0 uppercase ' +
              avatarClasses(role)
            }
          >
            {username.slice(0, 2)}
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-sm font-medium truncate">{username}</p>
            <span
              className={
                'text-xs px-2 py-0.5 rounded-full font-medium ' + pillClasses(role)
              }
            >
              {role}
            </span>
          </div>
        </div>
      )}
    </div>
  );
}

export default function Sidebar({ role, username, isMobileOpen, onClose }: Props) {
  return (
    <>
      {/* Desktop sidebar — always visible md+ */}
      <div className="hidden md:flex h-full">
        <SidebarPanel role={role} username={username} />
      </div>

      {/* Mobile overlay — only when open */}
      {isMobileOpen && (
        <div className="md:hidden fixed inset-0 z-40">
          {/* Backdrop */}
          <div
            className="absolute inset-0 bg-black/30"
            onClick={onClose}
          />
          {/* Drawer panel */}
          <div className="absolute left-0 top-0 h-full z-50">
            <SidebarPanel role={role} username={username} />
          </div>
        </div>
      )}
    </>
  );
}
