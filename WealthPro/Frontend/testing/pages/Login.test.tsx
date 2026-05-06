import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { useAuth } from '@/auth/store';
import Login from '@/pages/public/Login';

const mockNavigate = vi.hoisted(() => vi.fn());
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>();
  return { ...actual, useNavigate: () => mockNavigate };
});

vi.mock('@/api/auth', () => ({
  login: vi.fn(),
}));

import { login } from '@/api/auth';
const mockLogin = login as ReturnType<typeof vi.fn>;

function makeJWT(payload: object): string {
  const encode = (obj: object) =>
    btoa(JSON.stringify(obj))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=/g, '');
  return `${encode({ alg: 'HS256', typ: 'JWT' })}.${encode(payload)}.sig`;
}

beforeEach(() => {
  localStorage.clear();
  useAuth.setState({ user: null });
  mockNavigate.mockReset();
  mockLogin.mockReset();
});

function renderLogin() {
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/admin/dashboard" element={<div>Admin Dashboard</div>} />
        <Route path="/rm/clients" element={<div>RM Clients</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe('Login page', () => {
  it('renders the welcome heading', () => {
    renderLogin();
    expect(screen.getByText('Welcome back')).toBeInTheDocument();
  });

  it('renders the username input', () => {
    renderLogin();
    expect(screen.getByPlaceholderText('Enter your username')).toBeInTheDocument();
  });

  it('renders the password input', () => {
    renderLogin();
    expect(screen.getByPlaceholderText('Enter your password')).toBeInTheDocument();
  });

  it('renders the Sign in button', () => {
    renderLogin();
    expect(screen.getByRole('button', { name: /Sign in/i })).toBeInTheDocument();
  });

  it('shows validation error when both fields are empty', async () => {
    renderLogin();
    await userEvent.click(screen.getByRole('button', { name: /Sign in/i }));
    expect(screen.getByText('Please enter username and password')).toBeInTheDocument();
  });

  it('shows validation error when password is empty', async () => {
    renderLogin();
    await userEvent.type(screen.getByPlaceholderText('Enter your username'), 'admin1');
    await userEvent.click(screen.getByRole('button', { name: /Sign in/i }));
    expect(screen.getByText('Please enter username and password')).toBeInTheDocument();
  });

  it('shows validation error when username is only whitespace', async () => {
    renderLogin();
    await userEvent.type(screen.getByPlaceholderText('Enter your username'), '   ');
    await userEvent.type(screen.getByPlaceholderText('Enter your password'), 'pass123');
    await userEvent.click(screen.getByRole('button', { name: /Sign in/i }));
    expect(screen.getByText('Please enter username and password')).toBeInTheDocument();
  });

  it('shows loading state while submitting', async () => {
    mockLogin.mockImplementation(() => new Promise(() => {}));
    renderLogin();
    await userEvent.type(screen.getByPlaceholderText('Enter your username'), 'admin1');
    await userEvent.type(screen.getByPlaceholderText('Enter your password'), 'pass123');
    await userEvent.click(screen.getByRole('button', { name: /Sign in/i }));
    expect(screen.getByRole('button', { name: /Signing in/i })).toBeDisabled();
  });

  it('shows 401 error on invalid credentials', async () => {
    const err: any = new Error('Unauthorized');
    err.response = { status: 401 };
    mockLogin.mockRejectedValue(err);

    renderLogin();
    await userEvent.type(screen.getByPlaceholderText('Enter your username'), 'admin1');
    await userEvent.type(screen.getByPlaceholderText('Enter your password'), 'wrongpass');
    await userEvent.click(screen.getByRole('button', { name: /Sign in/i }));

    await waitFor(() =>
      expect(screen.getByText('Invalid username or password')).toBeInTheDocument()
    );
  });

  it('shows generic error on non-401 failure', async () => {
    const err: any = new Error('Server error');
    err.response = { status: 500 };
    mockLogin.mockRejectedValue(err);

    renderLogin();
    await userEvent.type(screen.getByPlaceholderText('Enter your username'), 'admin1');
    await userEvent.type(screen.getByPlaceholderText('Enter your password'), 'pass123');
    await userEvent.click(screen.getByRole('button', { name: /Sign in/i }));

    await waitFor(() =>
      expect(screen.getByText('Login failed. Please try again.')).toBeInTheDocument()
    );
  });

  it('navigates to role home on successful login', async () => {
    const token = makeJWT({ sub: 'admin1', roles: 'ROLE_ADMIN', userId: 1 });
    mockLogin.mockResolvedValue(token);

    renderLogin();
    await userEvent.type(screen.getByPlaceholderText('Enter your username'), 'admin1');
    await userEvent.type(screen.getByPlaceholderText('Enter your password'), 'pass123');
    await userEvent.click(screen.getByRole('button', { name: /Sign in/i }));

    await waitFor(() =>
      expect(mockNavigate).toHaveBeenCalledWith('/admin/dashboard')
    );
  });

  it('navigates to RM home when RM user logs in', async () => {
    const token = makeJWT({ sub: 'rm1', roles: 'ROLE_RM', userId: 5 });
    mockLogin.mockResolvedValue(token);

    renderLogin();
    await userEvent.type(screen.getByPlaceholderText('Enter your username'), 'rm1');
    await userEvent.type(screen.getByPlaceholderText('Enter your password'), 'pass123');
    await userEvent.click(screen.getByRole('button', { name: /Sign in/i }));

    await waitFor(() =>
      expect(mockNavigate).toHaveBeenCalledWith('/rm/clients')
    );
  });

  it('redirects to role home if user is already logged in', () => {
    useAuth.setState({ user: { username: 'admin1', role: 'ADMIN', token: 'x' } });
    render(
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/admin/dashboard" element={<div>Admin Dashboard</div>} />
        </Routes>
      </MemoryRouter>
    );
    expect(screen.getByText('Admin Dashboard')).toBeInTheDocument();
  });
});
