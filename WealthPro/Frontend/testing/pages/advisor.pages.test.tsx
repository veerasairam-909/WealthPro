import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { useAuth } from '@/auth/store';

// AdvisorGoals and AdvisorRecommendations are re-exports of RM pages
// so they need the same mocks

vi.mock('@/api/clients', () => ({
  getAllClients: vi.fn(),
  getClientById: vi.fn(),
}));

vi.mock('@/api/goals', () => ({
  getGoalsByClientId: vi.fn(),
  createGoal: vi.fn(),
  updateGoal: vi.fn(),
  updateGoalStatus: vi.fn(),
  deleteGoal: vi.fn(),
}));

vi.mock('@/api/recommendations', () => ({
  getRecommendationsByClientId: vi.fn(),
  getAllModelPortfolios: vi.fn(),
  createRecommendation: vi.fn(),
  updateRecommendationStatus: vi.fn(),
  deleteRecommendation: vi.fn(),
}));

vi.mock('@/api/notifications', () => ({
  createNotification: vi.fn(),
}));

vi.mock('@/api/accounts', () => ({
  getAccountsByClientId: vi.fn(),
}));

vi.mock('@/lib/fetchUtils', () => ({
  cachedFetch: vi.fn(),
  parallelLimit: vi.fn(),
  invalidateCache: vi.fn(),
}));

import AdvisorGoals from '@/pages/advisor/AdvisorGoals';
import AdvisorRecommendations from '@/pages/advisor/AdvisorRecommendations';

import { getAllClients } from '@/api/clients';

const rmUser = { username: 'rm1', role: 'RM' as const, userId: 5, token: 'x' };

beforeEach(() => {
  vi.clearAllMocks();
  useAuth.setState({ user: rmUser });
  vi.mocked(getAllClients).mockResolvedValue([]);
});

// ─── AdvisorGoals (re-export of RmGoals) ─────────────────────────────────────

describe('AdvisorGoals page', () => {
  it('renders the Goals heading', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><AdvisorGoals /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /Goals/i })).toBeInTheDocument();
  });

  it('shows loading goals while fetching', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><AdvisorGoals /></MemoryRouter>);
    expect(screen.getByText(/Loading goals/i)).toBeInTheDocument();
  });

  it('shows empty state when no goals', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<MemoryRouter><AdvisorGoals /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No goals set yet/i)).toBeInTheDocument()
    );
  });

  it('renders without crashing', () => {
    expect(() =>
      render(<MemoryRouter><AdvisorGoals /></MemoryRouter>)
    ).not.toThrow();
  });
});

// ─── AdvisorRecommendations (re-export of RmRecommendations) ─────────────────

describe('AdvisorRecommendations page', () => {
  it('renders the Recommendations heading', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><AdvisorRecommendations /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /Recommendations/i })).toBeInTheDocument();
  });

  it('shows loading recommendations while fetching', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><AdvisorRecommendations /></MemoryRouter>);
    expect(screen.getByText(/Loading recommendations/i)).toBeInTheDocument();
  });

  it('shows empty state when no recommendations', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<MemoryRouter><AdvisorRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No recommendations yet/i)).toBeInTheDocument()
    );
  });

  it('renders without crashing', () => {
    expect(() =>
      render(<MemoryRouter><AdvisorRecommendations /></MemoryRouter>)
    ).not.toThrow();
  });
});
