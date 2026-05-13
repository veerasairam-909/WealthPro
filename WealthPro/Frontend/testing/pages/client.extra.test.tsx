import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { useAuth } from '@/auth/store';

// ── Mocks ──────────────────────────────────────────────────────────────────────

vi.mock('@/api/accounts', () => ({
  getAccountsByClientId: vi.fn(),
  getAllAccounts: vi.fn(),
  getAccountById: vi.fn(),
  createAccount: vi.fn(),
}));

vi.mock('@/api/holdings', () => ({
  getHoldingsByAccountId: vi.fn(),
  getHoldingById: vi.fn(),
}));

vi.mock('@/api/cashLedger', () => ({
  getCashLedgerByAccountId: vi.fn(),
  getBalanceByAccountId: vi.fn(),
  createCashLedgerEntry: vi.fn(),
}));

vi.mock('@/api/orders', () => ({
  getOrdersByClient: vi.fn(),
  getOrderLifecycle: vi.fn(),
  getAllOrders: vi.fn(),
  placeOrder: vi.fn(),
  cancelOrder: vi.fn(),
}));

vi.mock('@/api/securities', () => ({
  getAllSecurities: vi.fn(),
  getSecurityById: vi.fn(),
}));

vi.mock('@/api/notifications', () => ({
  getNotificationsByUserId: vi.fn(),
  getUnreadNotifications: vi.fn(),
  markAllAsRead: vi.fn(),
  markNotificationRead: vi.fn(),
  createNotification: vi.fn(),
}));

vi.mock('@/api/analytics', () => ({
  getAccountDashboard: vi.fn(),
  getPerformanceByAccount: vi.fn(),
  getRiskMeasuresByAccount: vi.fn(),
  getBreachesByAccount: vi.fn(),
}));

vi.mock('@/api/recommendations', () => ({
  getRecommendationsByClientId: vi.fn(),
  getAllModelPortfolios: vi.fn(),
  updateRecommendationStatus: vi.fn(),
}));

vi.mock('@/api/clients', () => ({
  getClientById: vi.fn(),
  getKycDocs: vi.fn(),
  getRiskProfile: vi.fn(),
  updateClient: vi.fn(),
  createRiskProfile: vi.fn(),
  updateRiskProfile: vi.fn(),
  uploadKyc: vi.fn(),
  updateKycStatus: vi.fn(),
  getAllClients: vi.fn(),
}));

vi.mock('@/api/goals', () => ({
  getGoalsByClientId: vi.fn(),
  createGoal: vi.fn(),
  updateGoal: vi.fn(),
  deleteGoal: vi.fn(),
  updateGoalStatus: vi.fn(),
}));

vi.mock('@/lib/fetchUtils', () => ({
  cachedFetch: vi.fn(),
  parallelLimit: vi.fn(),
  invalidateCache: vi.fn(),
}));

// Mock recharts (used by Dashboard and Holdings)
vi.mock('recharts', () => ({
  PieChart: ({ children }: any) => <div data-testid="pie-chart">{children}</div>,
  Pie: () => null,
  Cell: () => null,
  Sector: () => null,
  Tooltip: () => null,
  ResponsiveContainer: ({ children }: any) => <div>{children}</div>,
  BarChart: ({ children }: any) => <div data-testid="bar-chart">{children}</div>,
  Bar: () => null,
  XAxis: () => null,
  YAxis: () => null,
  CartesianGrid: () => null,
  ReferenceLine: () => null,
  LabelList: () => null,
  Legend: () => null,
}));

import Dashboard from '@/pages/client/Dashboard';
import Holdings from '@/pages/client/Holdings';
import ClientRecommendations from '@/pages/client/ClientRecommendations';
import Reviews from '@/pages/client/Reviews';

import { getAccountsByClientId } from '@/api/accounts';
import { getHoldingsByAccountId } from '@/api/holdings';
import { getBalanceByAccountId } from '@/api/cashLedger';
import { getOrdersByClient } from '@/api/orders';
import { getAllSecurities } from '@/api/securities';
import { getUnreadNotifications } from '@/api/notifications';
import { getPerformanceByAccount, getRiskMeasuresByAccount } from '@/api/analytics';
import { getRecommendationsByClientId } from '@/api/recommendations';
import { getGoalsByClientId } from '@/api/goals';
import { getRiskProfile } from '@/api/clients';

const clientUser = { username: 'client1', role: 'CLIENT' as const, clientId: 5, token: 'x' };
const noIdUser   = { username: 'client1', role: 'CLIENT' as const, token: 'x' };

beforeEach(() => {
  vi.clearAllMocks();
  useAuth.setState({ user: clientUser });
  vi.mocked(getAccountsByClientId).mockResolvedValue([]);
  vi.mocked(getHoldingsByAccountId).mockResolvedValue([]);
  vi.mocked(getBalanceByAccountId).mockResolvedValue(0);
  vi.mocked(getOrdersByClient).mockResolvedValue([]);
  vi.mocked(getAllSecurities).mockResolvedValue([]);
  vi.mocked(getUnreadNotifications).mockResolvedValue([]);
  vi.mocked(getPerformanceByAccount).mockResolvedValue(null);
  vi.mocked(getRiskMeasuresByAccount).mockResolvedValue(null);
  vi.mocked(getRecommendationsByClientId).mockResolvedValue([]);
  vi.mocked(getGoalsByClientId).mockResolvedValue([]);
  vi.mocked(getRiskProfile).mockResolvedValue(null);
});

// ─── Dashboard ────────────────────────────────────────────────────────────────

describe('Client Dashboard page', () => {
  it('shows loading... when no clientId', () => {
    useAuth.setState({ user: noIdUser });
    render(<MemoryRouter><Dashboard /></MemoryRouter>);
    expect(screen.getByText(/Loading\.\.\./i)).toBeInTheDocument();
  });

  it('shows loading dashboard while fetching', () => {
    vi.mocked(getAccountsByClientId).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><Dashboard /></MemoryRouter>);
    expect(screen.getByText(/Loading dashboard/i)).toBeInTheDocument();
  });

  it('renders My Dashboard heading after load', async () => {
    render(<MemoryRouter><Dashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /My Dashboard/i })).toBeInTheDocument()
    );
  });

  it('renders without crashing', () => {
    expect(() =>
      render(<MemoryRouter><Dashboard /></MemoryRouter>)
    ).not.toThrow();
  });

  it('shows No holdings yet after load with empty account', async () => {
    render(<MemoryRouter><Dashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No holdings yet/i)).toBeInTheDocument()
    );
  });

  it('renders Portfolio Value KPI label when account is loaded', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR', status: 'ACTIVE' },
    ]);
    vi.mocked(getHoldingsByAccountId).mockResolvedValue([]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(50000);
    vi.mocked(getPerformanceByAccount).mockResolvedValue([]);
    vi.mocked(getRiskMeasuresByAccount).mockResolvedValue([]);
    render(<MemoryRouter><Dashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/Portfolio Value/i).length).toBeGreaterThan(0)
    );
  });

  it('renders Total Invested KPI when holdings are loaded', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR', status: 'ACTIVE' },
    ]);
    vi.mocked(getHoldingsByAccountId).mockResolvedValue([
      { holdingId: 1, securityId: 1, quantity: 10, avgCost: 100 },
    ]);
    vi.mocked(getAllSecurities).mockResolvedValue([
      { securityId: 1, symbol: 'RELIANCE', assetClass: 'EQUITY', currentPrice: 150 },
    ]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(50000);
    vi.mocked(getPerformanceByAccount).mockResolvedValue([]);
    vi.mocked(getRiskMeasuresByAccount).mockResolvedValue([]);
    render(<MemoryRouter><Dashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/Total Invested/i).length).toBeGreaterThan(0)
    );
  });

  it('renders welcome back greeting after load', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([]);
    render(<MemoryRouter><Dashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Welcome back/i)).toBeInTheDocument()
    );
  });

  it('renders Cash Balance KPI after load', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR', status: 'ACTIVE' },
    ]);
    vi.mocked(getHoldingsByAccountId).mockResolvedValue([]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(50000);
    vi.mocked(getPerformanceByAccount).mockResolvedValue([]);
    vi.mocked(getRiskMeasuresByAccount).mockResolvedValue([]);
    render(<MemoryRouter><Dashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/Cash Balance/i).length).toBeGreaterThan(0)
    );
  });

  it('shows pending recommendation banner when SUBMITTED reco exists', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([]);
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recommendationId: 1, status: 'SUBMITTED', notes: 'Buy RELIANCE' },
    ]);
    render(<MemoryRouter><Dashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/investment recommendation/i)).toBeInTheDocument()
    );
  });

  it('renders Total P&L KPI after load', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR', status: 'ACTIVE' },
    ]);
    vi.mocked(getHoldingsByAccountId).mockResolvedValue([
      { holdingId: 1, securityId: 2, quantity: 5, avgCost: 200 },
    ]);
    vi.mocked(getAllSecurities).mockResolvedValue([
      { securityId: 2, symbol: 'TCS', assetClass: 'EQUITY', currentPrice: 180 },
    ]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(0);
    vi.mocked(getPerformanceByAccount).mockResolvedValue([]);
    vi.mocked(getRiskMeasuresByAccount).mockResolvedValue([]);
    render(<MemoryRouter><Dashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/Total P&L/i).length).toBeGreaterThan(0)
    );
  });
});

// ─── Holdings ─────────────────────────────────────────────────────────────────

describe('Client Holdings page', () => {
  it('renders My Holdings heading', async () => {
    render(<MemoryRouter><Holdings /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /My Holdings/i })).toBeInTheDocument()
    );
  });

  it('shows No investment account when accounts empty', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([]);
    render(<MemoryRouter><Holdings /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No investment account yet/i)).toBeInTheDocument()
    );
  });

  it('shows No holdings yet when account has no holdings', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR' },
    ]);
    vi.mocked(getHoldingsByAccountId).mockResolvedValue([]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(50000);
    render(<MemoryRouter><Holdings /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No holdings yet/i)).toBeInTheDocument()
    );
  });

  it('renders without crashing', () => {
    expect(() =>
      render(<MemoryRouter><Holdings /></MemoryRouter>)
    ).not.toThrow();
  });

  it('renders holdings table with security symbol', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR', status: 'ACTIVE' },
    ]);
    vi.mocked(getHoldingsByAccountId).mockResolvedValue([
      { holdingId: 1, securityId: 1, quantity: 10, avgCost: 100 },
    ]);
    vi.mocked(getAllSecurities).mockResolvedValue([
      { securityId: 1, symbol: 'RELIANCE', assetClass: 'EQUITY', currentPrice: 150 },
    ]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(50000);
    render(<MemoryRouter><Holdings /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText('RELIANCE')).toBeInTheDocument()
    );
  });

  it('renders Market Value KPI label when account is loaded', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR', status: 'ACTIVE' },
    ]);
    vi.mocked(getHoldingsByAccountId).mockResolvedValue([]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(50000);
    render(<MemoryRouter><Holdings /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/Market Value/i).length).toBeGreaterThan(0)
    );
  });

  it('renders account type in info strip when account loaded', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR', status: 'ACTIVE' },
    ]);
    vi.mocked(getHoldingsByAccountId).mockResolvedValue([]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(50000);
    render(<MemoryRouter><Holdings /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/INVESTMENT/)).toBeInTheDocument()
    );
  });

  it('renders Sell button for equity holding', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR', status: 'ACTIVE' },
    ]);
    vi.mocked(getHoldingsByAccountId).mockResolvedValue([
      { holdingId: 1, securityId: 1, quantity: 10, avgCost: 100 },
    ]);
    vi.mocked(getAllSecurities).mockResolvedValue([
      { securityId: 1, symbol: 'INFY', assetClass: 'EQUITY', currentPrice: 120 },
    ]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(50000);
    render(<MemoryRouter><Holdings /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Sell/i })).toBeInTheDocument()
    );
  });

  it('renders Asset Allocation chart section heading with holdings', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR', status: 'ACTIVE' },
    ]);
    vi.mocked(getHoldingsByAccountId).mockResolvedValue([
      { holdingId: 1, securityId: 1, quantity: 10, avgCost: 100 },
    ]);
    vi.mocked(getAllSecurities).mockResolvedValue([
      { securityId: 1, symbol: 'RELIANCE', assetClass: 'EQUITY', currentPrice: 150 },
    ]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(50000);
    render(<MemoryRouter><Holdings /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Asset Allocation/i)).toBeInTheDocument()
    );
  });
});

// ─── ClientRecommendations ────────────────────────────────────────────────────

describe('Client Recommendations page', () => {
  it('shows loading when no clientId', () => {
    useAuth.setState({ user: noIdUser });
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    expect(screen.getByText(/Loading\.\.\./i)).toBeInTheDocument();
  });

  it('renders My Recommendations heading', async () => {
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /My Recommendations/i })).toBeInTheDocument()
    );
  });

  it('shows empty state when no recommendations', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No recommendations yet/i)).toBeInTheDocument()
    );
  });

  it('renders without crashing', () => {
    expect(() =>
      render(<MemoryRouter><ClientRecommendations /></MemoryRouter>)
    ).not.toThrow();
  });

  it('renders recommendation row when data is loaded', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recommendationId: 1, status: 'SUBMITTED', notes: 'Buy HDFC', modelPortfolioId: null, proposedDate: '2024-01-01' },
    ]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/Pending Your Action/i).length).toBeGreaterThan(0)
    );
  });

  it('renders status filter tab buttons', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByRole('button').some((b) => b.textContent?.includes('All'))).toBe(true)
    );
  });

  it('renders APPROVED recommendation as Accepted', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recoId: 2, status: 'APPROVED', riskClass: 'BALANCED', notes: 'Approved', modelPortfolioId: null, proposedDate: '2024-01-02' },
    ]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/✓ Accepted/i).length).toBeGreaterThan(0)
    );
  });

  it('renders REJECTED recommendation as Declined', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recoId: 3, status: 'REJECTED', riskClass: 'CONSERVATIVE', notes: 'Rejected', modelPortfolioId: null, proposedDate: '2024-01-03' },
    ]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/✗ Declined/i).length).toBeGreaterThan(0)
    );
  });

  it('renders total count of recommendations', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recommendationId: 4, status: 'SUBMITTED', notes: 'Note', modelPortfolioId: null, proposedDate: '2024-01-04' },
      { recommendationId: 5, status: 'APPROVED', notes: 'Note2', modelPortfolioId: null, proposedDate: '2024-01-05' },
    ]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/Pending Your Action/i).length).toBeGreaterThan(0)
    );
  });

  it('opens Accept & Place Orders modal when button is clicked on SUBMITTED reco', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recoId: 10, status: 'SUBMITTED', riskClass: 'MODERATE',
        proposalJson: '{}', proposedDate: '2024-01-05' },
    ]);
    vi.mocked(getAccountsByClientId).mockResolvedValue([{ accountId: 10, accountType: 'INVESTMENT' }]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(100000);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    const acceptBtn = await waitFor(() =>
      screen.getByRole('button', { name: /Accept & Place Orders/i })
    );
    fireEvent.click(acceptBtn);
    await waitFor(() =>
      expect(screen.getByText(/RM Proposal/i)).toBeInTheDocument()
    );
  });

  it('shows pending action banner when SUBMITTED reco exists', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recoId: 11, status: 'SUBMITTED', riskClass: 'CONSERVATIVE',
        proposalJson: '{"text":"Buy bonds"}', proposedDate: '2024-01-06' },
    ]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/awaiting your decision/i)).toBeInTheDocument()
    );
  });

  it('shows advisory flow banner for SUBMITTED reco', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recoId: 12, status: 'SUBMITTED', riskClass: 'AGGRESSIVE',
        proposalJson: '{}', proposedDate: '2024-01-07' },
    ]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/advisory flow works/i)).toBeInTheDocument()
    );
  });

  it('renders Decline button for SUBMITTED reco', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recoId: 13, status: 'SUBMITTED', riskClass: 'MODERATE',
        proposalJson: '{}', proposedDate: '2024-01-08' },
    ]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /✗ Decline/i })).toBeInTheDocument()
    );
  });

  it('renders View My Orders link for APPROVED reco', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recoId: 14, status: 'APPROVED', riskClass: 'BALANCED',
        proposalJson: '{}', proposedDate: '2024-01-09' },
    ]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/View My Orders/i)).toBeInTheDocument()
    );
  });

  it('renders accepted orders text for APPROVED reco', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recoId: 15, status: 'APPROVED', riskClass: 'BALANCED',
        proposalJson: '{"summary":"Balanced approach"}', proposedDate: '2024-01-10' },
    ]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Accepted — orders placed/i)).toBeInTheDocument()
    );
  });

  it('renders declined text for REJECTED reco', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recoId: 16, status: 'REJECTED', riskClass: 'CONSERVATIVE',
        proposalJson: '{}', proposedDate: '2024-01-11' },
    ]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/declined this recommendation/i)).toBeInTheDocument()
    );
  });

  it('renders proposal text when proposalJson has text field', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recoId: 17, status: 'SUBMITTED', riskClass: 'MODERATE',
        proposalJson: '{"text":"Invest in diversified portfolio"}', proposedDate: '2024-01-12' },
    ]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Invest in diversified portfolio/i)).toBeInTheDocument()
    );
  });

  it('renders proposal with allocation when proposalJson has allocation field', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recoId: 18, status: 'SUBMITTED', riskClass: 'MODERATE',
        proposalJson: '{"allocation":{"EQUITY":60,"BOND":40},"summary":"Balanced"}',
        proposedDate: '2024-01-13' },
    ]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Suggested Allocation/i)).toBeInTheDocument()
    );
  });

  it('renders Refresh button', async () => {
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([]);
    render(<MemoryRouter><ClientRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Refresh/i })).toBeInTheDocument()
    );
  });
});

// ─── Reviews ──────────────────────────────────────────────────────────────────

describe('Client Reviews page', () => {
  it('shows loading when no clientId', () => {
    useAuth.setState({ user: noIdUser });
    render(<MemoryRouter><Reviews /></MemoryRouter>);
    expect(screen.getByText(/Loading\.\.\./i)).toBeInTheDocument();
  });

  it('renders Portfolio Reviews heading', async () => {
    render(<MemoryRouter><Reviews /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /Portfolio Reviews/i })).toBeInTheDocument()
    );
  });

  it('shows loading data while fetching', async () => {
    vi.mocked(getAccountsByClientId).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><Reviews /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Loading data/i)).toBeInTheDocument()
    );
  });

  it('renders without crashing', () => {
    expect(() =>
      render(<MemoryRouter><Reviews /></MemoryRouter>)
    ).not.toThrow();
  });

  it('renders Portfolio Snapshot section after data loaded', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR' },
    ]);
    vi.mocked(getHoldingsByAccountId).mockResolvedValue([]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(50000);
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    vi.mocked(getGoalsByClientId).mockResolvedValue([]);
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([]);
    render(<MemoryRouter><Reviews /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Portfolio Snapshot/i)).toBeInTheDocument()
    );
  });

  it('renders Net worth KPI after load', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR' },
    ]);
    vi.mocked(getHoldingsByAccountId).mockResolvedValue([]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(75000);
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    vi.mocked(getGoalsByClientId).mockResolvedValue([]);
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([]);
    render(<MemoryRouter><Reviews /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Net worth/i)).toBeInTheDocument()
    );
  });

  it('renders Unrealised P&L KPI after load', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR' },
    ]);
    vi.mocked(getHoldingsByAccountId).mockResolvedValue([
      { holdingId: 1, securityId: 1, quantity: 5, avgCost: 100 },
    ]);
    vi.mocked(getBalanceByAccountId).mockResolvedValue(10000);
    vi.mocked(getAllSecurities).mockResolvedValue([
      { securityId: 1, symbol: 'INFY', assetClass: 'EQUITY', currentPrice: 120 },
    ]);
    vi.mocked(getGoalsByClientId).mockResolvedValue([]);
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([]);
    render(<MemoryRouter><Reviews /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/Unrealised P&L/i).length).toBeGreaterThan(0)
    );
  });

  it('renders goals progress section when goals are loaded', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([]);
    vi.mocked(getGoalsByClientId).mockResolvedValue([
      { goalId: 1, goalType: 'RETIREMENT', status: 'ACTIVE', targetAmount: 1000000, targetDate: '2035-01-01' },
    ]);
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([]);
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    render(<MemoryRouter><Reviews /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Goals Progress/i)).toBeInTheDocument()
    );
  });

  it('renders recommendations section when recommendations are loaded', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([]);
    vi.mocked(getGoalsByClientId).mockResolvedValue([]);
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([
      { recoId: 1, status: 'SUBMITTED', riskClass: 'MODERATE', proposalJson: '{}', proposedDate: '2024-01-01' },
    ]);
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    render(<MemoryRouter><Reviews /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Recommendations from your RM/i)).toBeInTheDocument()
    );
  });

  it('renders Download Statement button after load', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([]);
    vi.mocked(getGoalsByClientId).mockResolvedValue([]);
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([]);
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    render(<MemoryRouter><Reviews /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Download Statement/i })).toBeInTheDocument()
    );
  });

  it('shows No recommendations yet when reco list is empty', async () => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([]);
    vi.mocked(getGoalsByClientId).mockResolvedValue([]);
    vi.mocked(getRecommendationsByClientId).mockResolvedValue([]);
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    render(<MemoryRouter><Reviews /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No recommendations yet/i)).toBeInTheDocument()
    );
  });
});
