import { NavLink } from 'react-router-dom';
import type { Role } from '@/types/auth';

interface Props {
  role: Role;
}

export default function Sidebar({ role }: Props) {
  // build menu based on role
  let menu: { to: string; label: string }[] = [];

  if (role === 'ADMIN') {
    menu = [
      { to: '/admin/dashboard',        label: 'Dashboard' },
      { to: '/admin/users',            label: 'Users' },
      { to: '/admin/model-portfolios', label: 'Model Portfolios' },
      { to: '/admin/audit',            label: 'Audit log' },
    ];
  } else if (role === 'RM') {
    menu = [
      { to: '/rm/clients',            label: 'Clients' },
      { to: '/rm/onboard',            label: 'Onboard new client' },
      { to: '/rm/recommendations',    label: 'Recommendations' },
      { to: '/rm/goals',              label: 'Goals' },
      { to: '/rm/corporate-actions',  label: 'Corporate Actions' },
      { to: '/rm/notifications',      label: 'Notifications' },
    ];
  } else if (role === 'DEALER') {
    menu = [
      { to: '/dealer/orders',        label: 'Order blotter' },
      { to: '/dealer/securities',    label: 'Securities' },
      { to: '/dealer/research-notes', label: 'Research Notes' },
      { to: '/dealer/product-terms', label: 'Product Terms' },
    ];
  } else if (role === 'COMPLIANCE') {
    menu = [
      { to: '/compliance/breaches',     label: 'Breaches' },
      { to: '/compliance/kyc-approval', label: 'KYC Approval' },
      { to: '/compliance/failed-orders', label: 'Failed Orders' },
      { to: '/compliance/risk-monitor', label: 'Risk Monitor' },
      { to: '/compliance/rules',        label: 'Suitability Rules' },
      { to: '/compliance/aml-flags',    label: 'AML Flags' },
      { to: '/compliance/audit',        label: 'Audit Log' },
    ];
  } else {
    menu = [
      { to: '/me/dashboard', label: 'Dashboard' },
      { to: '/me/products', label: 'Browse & Invest' },
      { to: '/me/orders', label: 'My Orders' },
      { to: '/me/holdings', label: 'Holdings' },
      { to: '/me/kyc', label: 'My KYC' },
      { to: '/me/risk-profile', label: 'Risk Profile' },
      { to: '/me/goals', label: 'Goals' },
      { to: '/me/notifications', label: 'Notifications' },
      { to: '/me/reviews', label: 'Reviews' },
    ];
  }

  return (
    <div className="w-56 bg-white border-r border-border h-full p-4">
      <p className="text-xs text-text-3 uppercase mb-3 font-semibold">Menu</p>
      {menu.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          className={({ isActive }) =>
            'block px-3 py-2 rounded text-sm mb-1 ' +
            (isActive ? 'bg-primary-soft text-primary font-medium' : 'text-text-2 hover:bg-surface')
          }
        >
          {item.label}
        </NavLink>
      ))}
    </div>
  );
}
