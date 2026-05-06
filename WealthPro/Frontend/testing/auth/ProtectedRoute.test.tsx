import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { useAuth } from '@/auth/store';
import ProtectedRoute from '@/auth/ProtectedRoute';
import type { Role } from '@/types/auth';

beforeEach(() => {
  localStorage.clear();
  useAuth.setState({ user: null });
});

function renderRoute(allow: Role | Role[], initialPath = '/protected') {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <Routes>
        <Route
          path="/protected"
          element={
            <ProtectedRoute allow={allow}>
              <div>Secret Content</div>
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<div>Login Page</div>} />
        <Route path="/403" element={<div>Forbidden Page</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe('ProtectedRoute', () => {
  it('redirects to /login when user is not authenticated', () => {
    renderRoute('ADMIN');
    expect(screen.getByText('Login Page')).toBeInTheDocument();
    expect(screen.queryByText('Secret Content')).not.toBeInTheDocument();
  });

  it('redirects to /403 when user has wrong role', () => {
    useAuth.setState({ user: { username: 'rm1', role: 'RM', token: 'x' } });
    renderRoute('ADMIN');
    expect(screen.getByText('Forbidden Page')).toBeInTheDocument();
    expect(screen.queryByText('Secret Content')).not.toBeInTheDocument();
  });

  it('renders children when user has correct single role', () => {
    useAuth.setState({ user: { username: 'admin1', role: 'ADMIN', token: 'x' } });
    renderRoute('ADMIN');
    expect(screen.getByText('Secret Content')).toBeInTheDocument();
  });

  it('renders children when role is in allowed array', () => {
    useAuth.setState({ user: { username: 'rm1', role: 'RM', token: 'x' } });
    renderRoute(['ADMIN', 'RM', 'COMPLIANCE']);
    expect(screen.getByText('Secret Content')).toBeInTheDocument();
  });

  it('redirects to /403 when role is not in allowed array', () => {
    useAuth.setState({ user: { username: 'client1', role: 'CLIENT', token: 'x' } });
    renderRoute(['ADMIN', 'RM']);
    expect(screen.getByText('Forbidden Page')).toBeInTheDocument();
    expect(screen.queryByText('Secret Content')).not.toBeInTheDocument();
  });

  it('renders children for CLIENT role when allowed', () => {
    useAuth.setState({ user: { username: 'client1', role: 'CLIENT', token: 'x' } });
    renderRoute('CLIENT');
    expect(screen.getByText('Secret Content')).toBeInTheDocument();
  });

  it('renders children for DEALER when DEALER is in allowed array', () => {
    useAuth.setState({ user: { username: 'dealer1', role: 'DEALER', token: 'x' } });
    renderRoute(['DEALER', 'ADMIN']);
    expect(screen.getByText('Secret Content')).toBeInTheDocument();
  });

  it('redirects to /login when user is null regardless of allow', () => {
    renderRoute(['ADMIN', 'RM', 'DEALER', 'COMPLIANCE', 'CLIENT']);
    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });
});
