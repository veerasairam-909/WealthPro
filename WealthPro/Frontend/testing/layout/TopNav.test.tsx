import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { useAuth } from '@/auth/store';
import TopNav from '@/components/TopNav';

const mockNavigate = vi.hoisted(() => vi.fn());
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>();
  return { ...actual, useNavigate: () => mockNavigate };
});

beforeEach(() => {
  localStorage.clear();
  useAuth.setState({ user: null });
  mockNavigate.mockReset();
});

function renderTopNav(onMenuClick?: () => void) {
  return render(
    <MemoryRouter>
      <TopNav onMenuClick={onMenuClick} />
    </MemoryRouter>
  );
}

describe('TopNav', () => {
  it('renders nothing when there is no logged-in user', () => {
    const { container } = renderTopNav();
    expect(container.firstChild).toBeNull();
  });

  it('renders the username when user is logged in', () => {
    useAuth.setState({ user: { username: 'john_doe', role: 'RM', token: 'x' } });
    renderTopNav();
    expect(screen.getByText('john_doe')).toBeInTheDocument();
  });

  it('renders the role badge when user is logged in', () => {
    useAuth.setState({ user: { username: 'admin1', role: 'ADMIN', token: 'x' } });
    renderTopNav();
    expect(screen.getByText('ADMIN')).toBeInTheDocument();
  });

  it('renders the Logout button', () => {
    useAuth.setState({ user: { username: 'admin1', role: 'ADMIN', token: 'x' } });
    renderTopNav();
    expect(screen.getByRole('button', { name: 'Logout' })).toBeInTheDocument();
  });

  it('navigates to /login after clicking Logout', async () => {
    useAuth.setState({ user: { username: 'admin1', role: 'ADMIN', token: 'x' } });
    renderTopNav();
    await userEvent.click(screen.getByRole('button', { name: 'Logout' }));
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  it('clears user state after logout', async () => {
    useAuth.setState({ user: { username: 'admin1', role: 'ADMIN', token: 'x' } });
    renderTopNav();
    await userEvent.click(screen.getByRole('button', { name: 'Logout' }));
    expect(useAuth.getState().user).toBeNull();
  });

  it('calls onMenuClick when the hamburger button is clicked', async () => {
    useAuth.setState({ user: { username: 'rm1', role: 'RM', token: 'x' } });
    const onMenuClick = vi.fn();
    renderTopNav(onMenuClick);
    await userEvent.click(screen.getByLabelText('Open menu'));
    expect(onMenuClick).toHaveBeenCalledTimes(1);
  });

  it('renders the Logo (WealthPro) when user is present', () => {
    useAuth.setState({ user: { username: 'dealer1', role: 'DEALER', token: 'x' } });
    renderTopNav();
    expect(screen.getByText('WealthPro')).toBeInTheDocument();
  });
});
