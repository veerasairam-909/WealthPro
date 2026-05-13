import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { useAuth } from '@/auth/store';

vi.mock('@/api/clients', () => ({
  getAllClients: vi.fn(),
  getClientById: vi.fn(),
  getKycDocs: vi.fn(),
  updateKycStatus: vi.fn(),
  getRiskProfile: vi.fn(),
  updateClient: vi.fn(),
  createRiskProfile: vi.fn(),
  updateRiskProfile: vi.fn(),
  uploadKyc: vi.fn(),
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
  getNotificationsByUserId: vi.fn(),
  getUnreadNotifications: vi.fn(),
  markAllAsRead: vi.fn(),
  markNotificationRead: vi.fn(),
}));

vi.mock('@/api/accounts', () => ({
  getAccountsByClientId: vi.fn(),
  getAllAccounts: vi.fn(),
  getAccountById: vi.fn(),
  createAccount: vi.fn(),
}));

vi.mock('@/api/holdings', () => ({
  getHoldingsByAccountId: vi.fn(),
}));

vi.mock('@/api/analytics', () => ({
  getAccountDashboard: vi.fn(),
  getPerformanceByAccount: vi.fn(),
  getRiskMeasuresByAccount: vi.fn(),
  getBreachesByAccount: vi.fn(),
  runComplianceScan: vi.fn(),
  runRiskAssessment: vi.fn(),
}));

vi.mock('@/api/orders', () => ({
  getAllOrders: vi.fn(),
  placeOrder: vi.fn(),
}));

vi.mock('@/api/cashLedger', () => ({
  getCashLedgerByAccountId: vi.fn(),
  getBalanceByAccountId: vi.fn(),
  createCashLedgerEntry: vi.fn(),
}));

vi.mock('@/api/securities', () => ({
  getAllSecurities: vi.fn(),
}));

vi.mock('@/api/admin', () => ({
  getAllUsers: vi.fn(),
  getUserByUsername: vi.fn(),
}));

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

vi.mock('@/api/amlFlags', () => ({
  getAllAmlFlags: vi.fn(),
  getAmlFlagsByClient: vi.fn(),
  createAmlFlag: vi.fn(),
}));

vi.mock('@/lib/fetchUtils', () => ({
  cachedFetch: vi.fn(),
  parallelLimit: vi.fn(),
  invalidateCache: vi.fn(),
}));

vi.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: any) => <div>{children}</div>,
  BarChart: ({ children }: any) => <div data-testid="bar-chart">{children}</div>,
  Bar: () => null,
  XAxis: () => null,
  YAxis: () => null,
  CartesianGrid: () => null,
  Tooltip: () => null,
  Legend: () => null,
  ReferenceLine: () => null,
  LabelList: () => null,
}));

import RmClients from '@/pages/rm/Clients';
import RmGoals from '@/pages/rm/RmGoals';
import RmRecommendations from '@/pages/rm/RmRecommendations';
import RmNotifications from '@/pages/rm/RmNotifications';
import RmAnalytics from '@/pages/rm/RmAnalytics';
import OnboardClient from '@/pages/rm/OnboardClient';

import { getAllClients } from '@/api/clients';
import { getGoalsByClientId } from '@/api/goals';
import { getRecommendationsByClientId, updateRecommendationStatus } from '@/api/recommendations';
import { getNotificationsByUserId, markAllAsRead } from '@/api/notifications';
import { getAccountsByClientId } from '@/api/accounts';
import { getPerformanceByAccount, getRiskMeasuresByAccount } from '@/api/analytics';
import { cachedFetch, parallelLimit } from '@/lib/fetchUtils';

const rmUser = { username: 'rm1', role: 'RM' as const, userId: 5, token: 'x' };

beforeEach(() => {
  vi.clearAllMocks();
  useAuth.setState({ user: rmUser });
  vi.mocked(getAllClients).mockResolvedValue([]);
  vi.mocked(getGoalsByClientId).mockResolvedValue([]);
  vi.mocked(getRecommendationsByClientId).mockResolvedValue([]);
  vi.mocked(getNotificationsByUserId).mockResolvedValue([]);
  vi.mocked(getAccountsByClientId).mockResolvedValue([]);
  vi.mocked(getPerformanceByAccount).mockResolvedValue([]);
  vi.mocked(getRiskMeasuresByAccount).mockResolvedValue([]);
  vi.mocked(cachedFetch).mockReturnValue(undefined as any);
  vi.mocked(parallelLimit).mockResolvedValue([]);
  vi.mocked(markAllAsRead).mockResolvedValue(undefined as any);
  vi.mocked(updateRecommendationStatus).mockResolvedValue(undefined as any);
});

// ─── RM Clients page ──────────────────────────────────────────────────────────

describe('RM Clients page', () => {
  it('renders the Clients heading', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><RmClients /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /Clients/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><RmClients /></MemoryRouter>);
    // TableSkeleton has no text — verify empty state is not yet shown
    expect(screen.queryByText(/No clients found/i)).not.toBeInTheDocument();
  });

  it('shows empty state when no clients', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<MemoryRouter><RmClients /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No clients found/i)).toBeInTheDocument()
    );
  });

  it('renders a client when loaded', async () => {
    vi.mocked(getAllClients).mockResolvedValue([
      { clientId: 1, name: 'John Doe', segment: 'HNI', status: 'Active' },
    ]);
    render(<MemoryRouter><RmClients /></MemoryRouter>);
    await waitFor(() => expect(screen.getByText('John Doe')).toBeInTheDocument());
  });

  it('renders the segment filter select', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><RmClients /></MemoryRouter>);
    expect(screen.getByRole('option', { name: 'HNI' })).toBeInTheDocument();
  });
});

// ─── RM Goals page ────────────────────────────────────────────────────────────

describe('RmGoals page', () => {
  it('renders the Goals heading', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><RmGoals /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /Goals/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><RmGoals /></MemoryRouter>);
    expect(screen.getByText(/Loading goals/i)).toBeInTheDocument();
  });

  it('shows empty state when no goals', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<MemoryRouter><RmGoals /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No goals/i)).toBeInTheDocument()
    );
  });
});

// ─── RM Recommendations page ──────────────────────────────────────────────────

describe('RmRecommendations page', () => {
  it('renders the Recommendations heading', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><RmRecommendations /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /Recommendations/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><RmRecommendations /></MemoryRouter>);
    expect(screen.getByText(/Loading recommendations/i)).toBeInTheDocument();
  });

  it('shows empty state when no recommendations', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<MemoryRouter><RmRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No recommendations yet/i)).toBeInTheDocument()
    );
  });
});

// ─── RM Notifications page ────────────────────────────────────────────────────

describe('RmNotifications page', () => {
  it('renders the Notifications heading', () => {
    vi.mocked(getNotificationsByUserId).mockReturnValue(new Promise(() => {}));
    render(<RmNotifications />);
    expect(screen.getByRole('heading', { name: /Notifications/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getNotificationsByUserId).mockReturnValue(new Promise(() => {}));
    render(<RmNotifications />);
    expect(screen.getByText(/Loading notifications/i)).toBeInTheDocument();
  });

  it('shows empty state when no notifications', async () => {
    vi.mocked(getNotificationsByUserId).mockResolvedValue([]);
    render(<RmNotifications />);
    await waitFor(() =>
      expect(screen.getByText(/No notifications yet/i)).toBeInTheDocument()
    );
  });
});

// ─── RM Analytics page ────────────────────────────────────────────────────────

describe('RmAnalytics page', () => {
  it('renders the Analytics heading', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /Analytics/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    expect(screen.getByText(/Loading clients/i)).toBeInTheDocument();
  });

  it('shows empty state when no clients', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No account selected/i)).toBeInTheDocument()
    );
  });

  it('renders Select Client Account panel heading', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Select Client Account/i)).toBeInTheDocument()
    );
  });

  it('renders client dropdown option when clients are loaded', async () => {
    vi.mocked(getAllClients).mockResolvedValue([
      { clientId: 1, name: 'Alice Kumar', segment: 'HNI', status: 'Active' },
    ]);
    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('option', { name: 'Alice Kumar' })).toBeInTheDocument()
    );
  });

  it('renders default select client option', async () => {
    vi.mocked(getAllClients).mockResolvedValue([
      { clientId: 1, name: 'Bob Singh', segment: 'MASS', status: 'Active' },
    ]);
    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('option', { name: /Select a client/i })).toBeInTheDocument()
    );
  });

  it('renders Performance vs benchmark description', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Performance vs benchmark/i)).toBeInTheDocument()
    );
  });
});

// ─── RM Goals extra tests ─────────────────────────────────────────────────────

describe('RmGoals extra tests', () => {
  it('renders No goals set yet when clients resolve but goals empty', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<MemoryRouter><RmGoals /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No goals/i)).toBeInTheDocument()
    );
  });
});

// ─── RM Recommendations extra tests ──────────────────────────────────────────

describe('RmRecommendations extra tests', () => {
  it('renders empty state text after resolve', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<MemoryRouter><RmRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No recommendations yet/i)).toBeInTheDocument()
    );
  });
});

// ─── OnboardClient page ───────────────────────────────────────────────────────

describe('OnboardClient page', () => {
  it('renders the Onboard Client heading', () => {
    render(
      <MemoryRouter>
        <OnboardClient />
      </MemoryRouter>
    );
    expect(screen.getByRole('heading', { name: /Onboard/i })).toBeInTheDocument();
  });

  it('renders a form with name fields', () => {
    render(
      <MemoryRouter>
        <OnboardClient />
      </MemoryRouter>
    );
    expect(screen.getByPlaceholderText(/Rohan Verma/i)).toBeInTheDocument();
  });

  it('renders email and phone fields', () => {
    render(<MemoryRouter><OnboardClient /></MemoryRouter>);
    expect(screen.getByPlaceholderText(/rohan@example.com/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/9123456789/i)).toBeInTheDocument();
  });

  it('renders segment select with HNI option', () => {
    render(<MemoryRouter><OnboardClient /></MemoryRouter>);
    expect(screen.getByRole('option', { name: 'HNI' })).toBeInTheDocument();
  });

  it('renders the Create client submit button', () => {
    render(<MemoryRouter><OnboardClient /></MemoryRouter>);
    expect(screen.getByRole('button', { name: /Create client/i })).toBeInTheDocument();
  });

  it('renders Back to clients link', () => {
    render(<MemoryRouter><OnboardClient /></MemoryRouter>);
    expect(screen.getAllByRole('link', { name: /Back to clients/i }).length).toBeGreaterThan(0);
  });

  it('renders the Login credentials section', () => {
    render(<MemoryRouter><OnboardClient /></MemoryRouter>);
    expect(screen.getByText(/Login credentials/i)).toBeInTheDocument();
  });

  it('renders the Personal information section', () => {
    render(<MemoryRouter><OnboardClient /></MemoryRouter>);
    expect(screen.getByText(/Personal information/i)).toBeInTheDocument();
  });

  it('renders without crashing', () => {
    expect(() =>
      render(<MemoryRouter><OnboardClient /></MemoryRouter>)
    ).not.toThrow();
  });
});

// ─── RmAnalytics analytics content tests ─────────────────────────────────────

describe('RmAnalytics analytics content tests', () => {
  it('shows analytics KPI cards after selecting client and account', async () => {
    vi.mocked(getAllClients).mockResolvedValue([
      { clientId: 1, name: 'Alice Kumar', segment: 'HNI', status: 'Active' },
    ]);
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 10, accountType: 'INVESTMENT', baseCurrency: 'INR' },
    ]);
    vi.mocked(getPerformanceByAccount).mockResolvedValue([]);
    vi.mocked(getRiskMeasuresByAccount).mockResolvedValue([]);

    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);

    const clientSelect = await waitFor(() =>
      screen.getByDisplayValue('— Select a client —')
    );
    fireEvent.change(clientSelect, { target: { value: '1' } });

    await waitFor(() =>
      expect(screen.getByText(/Latest Return/i)).toBeInTheDocument()
    );
  });

  it('shows No performance records when no perf data after account selected', async () => {
    vi.mocked(getAllClients).mockResolvedValue([
      { clientId: 2, name: 'Bob Singh', segment: 'MASS', status: 'Active' },
    ]);
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 20, accountType: 'INVESTMENT', baseCurrency: 'INR' },
    ]);
    vi.mocked(getPerformanceByAccount).mockResolvedValue([]);
    vi.mocked(getRiskMeasuresByAccount).mockResolvedValue([]);

    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    const clientSelect = await waitFor(() =>
      screen.getByDisplayValue('— Select a client —')
    );
    fireEvent.change(clientSelect, { target: { value: '2' } });

    await waitFor(() =>
      expect(screen.getByText(/No performance records/i)).toBeInTheDocument()
    );
  });

  it('shows No risk measures yet when risk data empty', async () => {
    vi.mocked(getAllClients).mockResolvedValue([
      { clientId: 3, name: 'Priya Nair', segment: 'HNI', status: 'Active' },
    ]);
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 30, accountType: 'INVESTMENT', baseCurrency: 'INR' },
    ]);
    vi.mocked(getPerformanceByAccount).mockResolvedValue([]);
    vi.mocked(getRiskMeasuresByAccount).mockResolvedValue([]);

    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    const clientSelect = await waitFor(() =>
      screen.getByDisplayValue('— Select a client —')
    );
    fireEvent.change(clientSelect, { target: { value: '3' } });

    await waitFor(() =>
      expect(screen.getByText(/No risk measures yet/i)).toBeInTheDocument()
    );
  });

  it('shows client segment info when client is selected', async () => {
    vi.mocked(getAllClients).mockResolvedValue([
      { clientId: 4, name: 'Raj Patel', segment: 'UHNI', status: 'Active' },
    ]);
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 40, accountType: 'INVESTMENT', baseCurrency: 'INR' },
    ]);
    vi.mocked(getPerformanceByAccount).mockResolvedValue([]);
    vi.mocked(getRiskMeasuresByAccount).mockResolvedValue([]);

    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    const clientSelect = await waitFor(() =>
      screen.getByDisplayValue('— Select a client —')
    );
    fireEvent.change(clientSelect, { target: { value: '4' } });

    await waitFor(() =>
      expect(screen.getByText(/UHNI/i)).toBeInTheDocument()
    );
  });

  it('renders Performance History table when perf records are loaded', async () => {
    vi.mocked(getAllClients).mockResolvedValue([
      { clientId: 5, name: 'Meera Shah', segment: 'HNI', status: 'Active' },
    ]);
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 50, accountType: 'INVESTMENT', baseCurrency: 'INR' },
    ]);
    vi.mocked(getPerformanceByAccount).mockResolvedValue([
      { recordId: 1, period: 'Q1-2024', endDate: '2024-03-31', returnPercentage: 5.2, benchmarkReturnPercentage: 4.1 },
    ]);
    vi.mocked(getRiskMeasuresByAccount).mockResolvedValue([]);

    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    const clientSelect = await waitFor(() =>
      screen.getByDisplayValue('— Select a client —')
    );
    fireEvent.change(clientSelect, { target: { value: '5' } });

    await waitFor(() =>
      expect(screen.getByText(/Performance History/i)).toBeInTheDocument()
    );
  });

  it('renders Avg Return KPI after account selected', async () => {
    vi.mocked(getAllClients).mockResolvedValue([
      { clientId: 6, name: 'Dev Kumar', segment: 'HNI', status: 'Active' },
    ]);
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 60, accountType: 'INVESTMENT', baseCurrency: 'INR' },
    ]);
    vi.mocked(getPerformanceByAccount).mockResolvedValue([]);
    vi.mocked(getRiskMeasuresByAccount).mockResolvedValue([]);

    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    const clientSelect = await waitFor(() =>
      screen.getByDisplayValue('— Select a client —')
    );
    fireEvent.change(clientSelect, { target: { value: '6' } });

    await waitFor(() =>
      expect(screen.getByText(/Avg Return/i)).toBeInTheDocument()
    );
  });

  it('renders risk measure card when risk data loaded', async () => {
    vi.mocked(getAllClients).mockResolvedValue([
      { clientId: 7, name: 'Sita Rao', segment: 'HNI', status: 'Active' },
    ]);
    vi.mocked(getAccountsByClientId).mockResolvedValue([
      { accountId: 70, accountType: 'INVESTMENT', baseCurrency: 'INR' },
    ]);
    vi.mocked(getPerformanceByAccount).mockResolvedValue([]);
    vi.mocked(getRiskMeasuresByAccount).mockResolvedValue([
      { measureId: 1, measureType: 'VOLATILITY', measureValue: 0.125, calculatedAt: '2024-01-01T00:00:00' },
    ]);

    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    const clientSelect = await waitFor(() =>
      screen.getByDisplayValue('— Select a client —')
    );
    fireEvent.change(clientSelect, { target: { value: '7' } });

    await waitFor(() =>
      expect(screen.getByText(/Volatility/i)).toBeInTheDocument()
    );
  });

  it('shows No investment account found when client has no accounts', async () => {
    vi.mocked(getAllClients).mockResolvedValue([
      { clientId: 8, name: 'No Account Client', segment: 'Retail', status: 'Active' },
    ]);
    vi.mocked(getAccountsByClientId).mockResolvedValue([]);

    render(<MemoryRouter><RmAnalytics /></MemoryRouter>);
    const clientSelect = await waitFor(() =>
      screen.getByDisplayValue('— Select a client —')
    );
    fireEvent.change(clientSelect, { target: { value: '8' } });

    await waitFor(() =>
      expect(screen.getByText(/No investment account found/i)).toBeInTheDocument()
    );
  });
});

// ─── RmNotifications extra tests ─────────────────────────────────────────────

describe('RmNotifications extra tests', () => {
  it('renders notification card when notifications are loaded', async () => {
    vi.mocked(getNotificationsByUserId).mockResolvedValue([
      { notificationId: 1, message: 'KYC document rejected for client', category: 'KYC', status: 'UNREAD', createdDate: '2024-01-15T10:00:00' },
    ]);
    render(<MemoryRouter><RmNotifications /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/KYC document rejected for client/i)).toBeInTheDocument()
    );
  });

  it('shows Mark all as read button when unread notifications exist', async () => {
    vi.mocked(getNotificationsByUserId).mockResolvedValue([
      { notificationId: 2, message: 'Compliance alert raised', category: 'Compliance', status: 'UNREAD', createdDate: '2024-01-16T10:00:00' },
    ]);
    render(<MemoryRouter><RmNotifications /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Mark all as read/i })).toBeInTheDocument()
    );
  });

  it('renders notification category pill for Order', async () => {
    vi.mocked(getNotificationsByUserId).mockResolvedValue([
      { notificationId: 3, message: 'Order placed for client', category: 'Order', status: 'UNREAD', createdDate: '2024-01-17T00:00:00' },
    ]);
    render(<MemoryRouter><RmNotifications /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText('Order')).toBeInTheDocument()
    );
  });

  it('renders filter ALL and UNREAD buttons', async () => {
    vi.mocked(getNotificationsByUserId).mockResolvedValue([]);
    render(<RmNotifications />);
    await waitFor(() =>
      expect(screen.getAllByRole('button').some((b) => b.textContent?.includes('All'))).toBe(true)
    );
  });

  it('renders notification with READ status', async () => {
    vi.mocked(getNotificationsByUserId).mockResolvedValue([
      { notificationId: 4, message: 'Account created', category: 'Order', status: 'READ', createdDate: '2024-01-18T00:00:00' },
    ]);
    render(<MemoryRouter><RmNotifications /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Account created/i)).toBeInTheDocument()
    );
  });
});

// ─── RmRecommendations data tests ────────────────────────────────────────────

describe('RmRecommendations data tests', () => {
  it('renders recommendation row when data loaded via cachedFetch + parallelLimit', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 1, name: 'Test Client', segment: 'HNI' },
    ]);
    vi.mocked(parallelLimit).mockResolvedValue([
      { status: 'fulfilled', value: [
        { recoId: 1, riskClass: 'MODERATE', status: 'SUBMITTED', proposalJson: '{}', proposedDate: '2024-01-01' },
      ]},
    ]);
    render(<MemoryRouter><RmRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText('Test Client')).toBeInTheDocument()
    );
  });

  it('renders Approve and Reject buttons for SUBMITTED recommendation', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 2, name: 'Alice Gupta', segment: 'UHNI' },
    ]);
    vi.mocked(parallelLimit).mockResolvedValue([
      { status: 'fulfilled', value: [
        { recoId: 2, riskClass: 'AGGRESSIVE', status: 'SUBMITTED', proposalJson: '{"targetReturn": 15}', proposedDate: '2024-02-01' },
      ]},
    ]);
    render(<MemoryRouter><RmRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Approve/i })).toBeInTheDocument()
    );
  });

  it('renders View client link for each recommendation', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 3, name: 'Mohan Das', segment: 'HNI' },
    ]);
    vi.mocked(parallelLimit).mockResolvedValue([
      { status: 'fulfilled', value: [
        { recoId: 3, riskClass: 'BALANCED', status: 'APPROVED', proposalJson: '{}', proposedDate: '2024-03-01' },
      ]},
    ]);
    render(<MemoryRouter><RmRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('link', { name: /View client/i })).toBeInTheDocument()
    );
  });

  it('renders pending review badge when SUBMITTED recos exist', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 1, name: 'Test Client', segment: 'HNI' },
    ]);
    vi.mocked(parallelLimit).mockResolvedValue([
      { status: 'fulfilled', value: [
        { recoId: 4, riskClass: 'BALANCED', status: 'SUBMITTED', proposalJson: '{}', proposedDate: '2024-03-01' },
      ]},
    ]);
    render(<MemoryRouter><RmRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/pending review/i)).toBeInTheDocument()
    );
  });

  it('renders status filter buttons after load', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([]);
    render(<MemoryRouter><RmRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByRole('button').some((b) => b.textContent?.includes('SUBMITTED'))).toBe(true)
    );
  });

  it('renders recommendation with proposal JSON targetReturn', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 5, name: 'Rich Client', segment: 'UHNI' },
    ]);
    vi.mocked(parallelLimit).mockResolvedValue([
      { status: 'fulfilled', value: [
        { recoId: 5, riskClass: 'AGGRESSIVE', status: 'SUBMITTED',
          proposalJson: '{"targetReturn": 18, "timeHorizon": "5 years", "SIP": 50000, "allocation": {"EQUITY": 70, "BOND": 30}}',
          proposedDate: '2024-04-01' },
      ]},
    ]);
    render(<MemoryRouter><RmRecommendations /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/Target Return/i).length).toBeGreaterThan(0)
    );
  });
});
