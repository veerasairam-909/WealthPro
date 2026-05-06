import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { useAuth } from '@/auth/store';
import Forbidden from '@/pages/Forbidden';

beforeEach(() => {
  localStorage.clear();
  useAuth.setState({ user: null });
});

function renderForbidden() {
  return render(
    <MemoryRouter>
      <Forbidden />
    </MemoryRouter>
  );
}

describe('Forbidden page', () => {
  it('renders the 403 status code', () => {
    renderForbidden();
    expect(screen.getByText('403')).toBeInTheDocument();
  });

  it('renders the "Access denied" heading', () => {
    renderForbidden();
    expect(screen.getByText('Access denied')).toBeInTheDocument();
  });

  it('renders the permission denied message', () => {
    renderForbidden();
    expect(
      screen.getByText("You don't have permission to view this page.")
    ).toBeInTheDocument();
  });

  it('renders a "Back to dashboard" link', () => {
    renderForbidden();
    expect(screen.getByRole('link', { name: 'Back to dashboard' })).toBeInTheDocument();
  });

  it('links to /login when no user is authenticated', () => {
    renderForbidden();
    expect(screen.getByRole('link', { name: 'Back to dashboard' })).toHaveAttribute(
      'href',
      '/login'
    );
  });

  it('links to /admin/dashboard for ADMIN user', () => {
    useAuth.setState({ user: { username: 'admin1', role: 'ADMIN', token: 'x' } });
    renderForbidden();
    expect(screen.getByRole('link', { name: 'Back to dashboard' })).toHaveAttribute(
      'href',
      '/admin/dashboard'
    );
  });

  it('links to /rm/clients for RM user', () => {
    useAuth.setState({ user: { username: 'rm1', role: 'RM', token: 'x' } });
    renderForbidden();
    expect(screen.getByRole('link', { name: 'Back to dashboard' })).toHaveAttribute(
      'href',
      '/rm/clients'
    );
  });

  it('links to /dealer/orders for DEALER user', () => {
    useAuth.setState({ user: { username: 'dealer1', role: 'DEALER', token: 'x' } });
    renderForbidden();
    expect(screen.getByRole('link', { name: 'Back to dashboard' })).toHaveAttribute(
      'href',
      '/dealer/orders'
    );
  });

  it('links to /compliance/breaches for COMPLIANCE user', () => {
    useAuth.setState({ user: { username: 'comp1', role: 'COMPLIANCE', token: 'x' } });
    renderForbidden();
    expect(screen.getByRole('link', { name: 'Back to dashboard' })).toHaveAttribute(
      'href',
      '/compliance/breaches'
    );
  });

  it('links to /me/dashboard for CLIENT user', () => {
    useAuth.setState({ user: { username: 'client1', role: 'CLIENT', token: 'x' } });
    renderForbidden();
    expect(screen.getByRole('link', { name: 'Back to dashboard' })).toHaveAttribute(
      'href',
      '/me/dashboard'
    );
  });
});
