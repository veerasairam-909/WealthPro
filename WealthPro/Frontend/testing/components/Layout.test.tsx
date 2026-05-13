import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { useAuth } from '@/auth/store';
import Layout from '@/components/Layout';

// Mock child components so we don't pull in their deep dependency trees
vi.mock('@/components/TopNav', () => ({
  default: ({ onMenuClick }: { onMenuClick: () => void }) => (
    <header data-testid="top-nav">
      <button onClick={onMenuClick} data-testid="menu-btn">menu</button>
    </header>
  ),
}));

vi.mock('@/components/Sidebar', () => ({
  default: ({ role, username, isMobileOpen, onClose }: any) => (
    <nav
      data-testid="sidebar"
      data-role={role}
      data-username={username}
      data-mobile={String(isMobileOpen)}
    >
      <button onClick={onClose} data-testid="close-btn">close</button>
    </nav>
  ),
}));

// react-router-dom Outlet just renders nothing by default in tests
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>();
  return {
    ...actual,
    Outlet: () => <main data-testid="outlet" />,
  };
});

beforeEach(() => {
  localStorage.clear();
  useAuth.setState({ user: null });
});

function renderLayout() {
  return render(
    <MemoryRouter>
      <Layout />
    </MemoryRouter>
  );
}

describe('Layout component', () => {
  it('returns null when no user is authenticated', () => {
    const { container } = renderLayout();
    expect(container.firstChild).toBeNull();
  });

  it('renders TopNav when user is authenticated', () => {
    useAuth.setState({ user: { username: 'admin1', role: 'ADMIN', token: 'x' } });
    renderLayout();
    expect(screen.getByTestId('top-nav')).toBeInTheDocument();
  });

  it('renders Sidebar when user is authenticated', () => {
    useAuth.setState({ user: { username: 'rm1', role: 'RM', token: 'x' } });
    renderLayout();
    expect(screen.getByTestId('sidebar')).toBeInTheDocument();
  });

  it('renders Outlet when user is authenticated', () => {
    useAuth.setState({ user: { username: 'client1', role: 'CLIENT', token: 'x' } });
    renderLayout();
    expect(screen.getByTestId('outlet')).toBeInTheDocument();
  });

  it('passes the correct role to Sidebar', () => {
    useAuth.setState({ user: { username: 'dealer1', role: 'DEALER', token: 'x' } });
    renderLayout();
    expect(screen.getByTestId('sidebar')).toHaveAttribute('data-role', 'DEALER');
  });

  it('passes the correct username to Sidebar', () => {
    useAuth.setState({ user: { username: 'comp1', role: 'COMPLIANCE', token: 'x' } });
    renderLayout();
    expect(screen.getByTestId('sidebar')).toHaveAttribute('data-username', 'comp1');
  });

  it('sidebar starts with isMobileOpen false', () => {
    useAuth.setState({ user: { username: 'rm1', role: 'RM', token: 'x' } });
    renderLayout();
    expect(screen.getByTestId('sidebar')).toHaveAttribute('data-mobile', 'false');
  });

  it('clicking menu button toggles isMobileOpen to true', () => {
    useAuth.setState({ user: { username: 'rm1', role: 'RM', token: 'x' } });
    renderLayout();
    fireEvent.click(screen.getByTestId('menu-btn'));
    expect(screen.getByTestId('sidebar')).toHaveAttribute('data-mobile', 'true');
  });

  it('clicking menu button again toggles isMobileOpen back to false', () => {
    useAuth.setState({ user: { username: 'rm1', role: 'RM', token: 'x' } });
    renderLayout();
    fireEvent.click(screen.getByTestId('menu-btn'));
    fireEvent.click(screen.getByTestId('menu-btn'));
    expect(screen.getByTestId('sidebar')).toHaveAttribute('data-mobile', 'false');
  });

  it('clicking close button in Sidebar sets isMobileOpen to false', () => {
    useAuth.setState({ user: { username: 'rm1', role: 'RM', token: 'x' } });
    renderLayout();
    // Open it first
    fireEvent.click(screen.getByTestId('menu-btn'));
    expect(screen.getByTestId('sidebar')).toHaveAttribute('data-mobile', 'true');
    // Close it
    fireEvent.click(screen.getByTestId('close-btn'));
    expect(screen.getByTestId('sidebar')).toHaveAttribute('data-mobile', 'false');
  });
});
