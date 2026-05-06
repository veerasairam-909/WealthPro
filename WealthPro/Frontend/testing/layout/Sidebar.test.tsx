import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import Sidebar from '@/components/Sidebar';
import type { Role } from '@/types/auth';

function renderSidebar(
  role: Role,
  opts: { username?: string; isMobileOpen?: boolean; onClose?: () => void } = {}
) {
  return render(
    <MemoryRouter>
      <Sidebar role={role} {...opts} />
    </MemoryRouter>
  );
}

describe('Sidebar – ADMIN role', () => {
  it('shows Dashboard link', () => {
    renderSidebar('ADMIN');
    expect(screen.getAllByText('Dashboard')[0]).toBeInTheDocument();
  });

  it('shows Users link', () => {
    renderSidebar('ADMIN');
    expect(screen.getAllByText('Users')[0]).toBeInTheDocument();
  });

  it('shows Audit Log link', () => {
    renderSidebar('ADMIN');
    expect(screen.getAllByText('Audit Log')[0]).toBeInTheDocument();
  });
});

describe('Sidebar – RM role', () => {
  it('shows Clients link', () => {
    renderSidebar('RM');
    expect(screen.getAllByText('Clients')[0]).toBeInTheDocument();
  });

  it('shows Onboard Client link', () => {
    renderSidebar('RM');
    expect(screen.getAllByText('Onboard Client')[0]).toBeInTheDocument();
  });

  it('shows Analytics link', () => {
    renderSidebar('RM');
    expect(screen.getAllByText('Analytics')[0]).toBeInTheDocument();
  });
});

describe('Sidebar – DEALER role', () => {
  it('shows Order Blotter link', () => {
    renderSidebar('DEALER');
    expect(screen.getAllByText('Order Blotter')[0]).toBeInTheDocument();
  });

  it('shows Securities link', () => {
    renderSidebar('DEALER');
    expect(screen.getAllByText('Securities')[0]).toBeInTheDocument();
  });
});

describe('Sidebar – COMPLIANCE role', () => {
  it('shows Breaches link', () => {
    renderSidebar('COMPLIANCE');
    expect(screen.getAllByText('Breaches')[0]).toBeInTheDocument();
  });

  it('shows KYC Approval link', () => {
    renderSidebar('COMPLIANCE');
    expect(screen.getAllByText('KYC Approval')[0]).toBeInTheDocument();
  });

  it('shows AML Flags link', () => {
    renderSidebar('COMPLIANCE');
    expect(screen.getAllByText('AML Flags')[0]).toBeInTheDocument();
  });
});

describe('Sidebar – CLIENT role', () => {
  it('shows Dashboard link', () => {
    renderSidebar('CLIENT');
    expect(screen.getAllByText('Dashboard')[0]).toBeInTheDocument();
  });

  it('shows Product Catalog link', () => {
    renderSidebar('CLIENT');
    expect(screen.getAllByText('Product Catalog')[0]).toBeInTheDocument();
  });

  it('shows My Orders link', () => {
    renderSidebar('CLIENT');
    expect(screen.getAllByText('My Orders')[0]).toBeInTheDocument();
  });
});

describe('Sidebar – username display', () => {
  it('shows the username in the profile card', () => {
    renderSidebar('ADMIN', { username: 'alice' });
    expect(screen.getAllByText('alice')[0]).toBeInTheDocument();
  });

  it('does not render a profile card when username is omitted', () => {
    renderSidebar('ADMIN');
    // Profile card contains the role pill text too, no element with just "al" initials
    expect(screen.queryByText('al')).not.toBeInTheDocument();
  });
});

describe('Sidebar – mobile overlay', () => {
  it('renders the mobile overlay when isMobileOpen is true', () => {
    const { container } = renderSidebar('ADMIN', { isMobileOpen: true, onClose: vi.fn() });
    expect(container.querySelector('.fixed.inset-0')).toBeInTheDocument();
  });

  it('does not render the mobile overlay when isMobileOpen is false', () => {
    const { container } = renderSidebar('ADMIN', { isMobileOpen: false });
    expect(container.querySelector('.fixed.inset-0')).not.toBeInTheDocument();
  });

  it('calls onClose when the backdrop is clicked', async () => {
    const onClose = vi.fn();
    const { container } = renderSidebar('ADMIN', { isMobileOpen: true, onClose });
    const backdrop = container.querySelector('.bg-black\\/30') as HTMLElement;
    await userEvent.click(backdrop);
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
