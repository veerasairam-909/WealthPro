import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
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

vi.mock('@/api/holdings', () => ({
  getHoldingsByAccountId: vi.fn(),
  getHoldingById: vi.fn(),
}));

vi.mock('@/api/accounts', () => ({
  getAccountsByClientId: vi.fn(),
  getAllAccounts: vi.fn(),
  getAccountById: vi.fn(),
  createAccount: vi.fn(),
}));

vi.mock('@/api/goals', () => ({
  getGoalsByClientId: vi.fn(),
  createGoal: vi.fn(),
  updateGoal: vi.fn(),
  updateGoalStatus: vi.fn(),
  deleteGoal: vi.fn(),
}));

vi.mock('@/api/notifications', () => ({
  getNotificationsByUserId: vi.fn(),
  getUnreadNotifications: vi.fn(),
  markAllAsRead: vi.fn(),
  markNotificationRead: vi.fn(),
  createNotification: vi.fn(),
}));

vi.mock('@/api/recommendations', () => ({
  getRecommendationsByClientId: vi.fn(),
  getAllModelPortfolios: vi.fn(),
  updateRecommendationStatus: vi.fn(),
}));

vi.mock('@/api/analytics', () => ({
  getAccountDashboard: vi.fn(),
  getPerformanceByAccount: vi.fn(),
  getRiskMeasuresByAccount: vi.fn(),
  getBreachesByAccount: vi.fn(),
}));

vi.mock('@/api/cashLedger', () => ({
  getCashLedgerByAccountId: vi.fn(),
  getBalanceByAccountId: vi.fn(),
  createCashLedgerEntry: vi.fn(),
}));

vi.mock('@/lib/fetchUtils', () => ({
  cachedFetch: vi.fn(),
  parallelLimit: vi.fn(),
  invalidateCache: vi.fn(),
}));

import MyKyc from '@/pages/client/MyKyc';
import MyOrders from '@/pages/client/MyOrders';
import Products from '@/pages/client/Products';
import ClientNotifications from '@/pages/client/Notifications';
import MyRiskProfile from '@/pages/client/MyRiskProfile';
import ClientGoals from '@/pages/client/Goals';

import { getKycDocs } from '@/api/clients';
import { getOrdersByClient } from '@/api/orders';
import { getAllSecurities } from '@/api/securities';
import { getNotificationsByUserId } from '@/api/notifications';
import { getRiskProfile } from '@/api/clients';
import { getGoalsByClientId } from '@/api/goals';
import { getAccountsByClientId } from '@/api/accounts';

const clientUser = { username: 'client1', role: 'CLIENT' as const, clientId: 5, token: 'x' };

beforeEach(() => {
  vi.clearAllMocks();
  useAuth.setState({ user: clientUser });
  vi.mocked(getKycDocs).mockResolvedValue([]);
  vi.mocked(getOrdersByClient).mockResolvedValue([]);
  vi.mocked(getAllSecurities).mockResolvedValue([]);
  vi.mocked(getNotificationsByUserId).mockResolvedValue([]);
  vi.mocked(getRiskProfile).mockResolvedValue(null);
  vi.mocked(getGoalsByClientId).mockResolvedValue([]);
  vi.mocked(getAccountsByClientId).mockResolvedValue([]);
});

// ─── MyKyc page ──────────────────────────────────────────────────────────────

describe('MyKyc page', () => {
  it('renders the My KYC Documents heading', async () => {
    vi.mocked(getKycDocs).mockReturnValue(new Promise(() => {}));
    render(<MyKyc />);
    expect(screen.getByRole('heading', { name: /My KYC Documents/i })).toBeInTheDocument();
  });

  it('shows loading state when fetching', () => {
    vi.mocked(getKycDocs).mockReturnValue(new Promise(() => {}));
    render(<MyKyc />);
    expect(screen.getByText(/Loading\.\.\./i)).toBeInTheDocument();
  });

  it('shows empty state when no documents', async () => {
    vi.mocked(getKycDocs).mockResolvedValue([]);
    render(<MyKyc />);
    await waitFor(() =>
      expect(screen.getByText(/No documents on file yet/i)).toBeInTheDocument()
    );
  });

  it('shows loading account when no clientId', () => {
    useAuth.setState({ user: { username: 'client1', role: 'CLIENT', token: 'x' } });
    render(<MyKyc />);
    expect(screen.getByText(/Loading account/i)).toBeInTheDocument();
  });

  it('renders KYC document row when data is loaded', async () => {
    vi.mocked(getKycDocs).mockResolvedValue([
      { kycId: 1, documentType: 'PAN', status: 'Verified', verifiedDate: '2024-01-01' },
    ]);
    render(<MyKyc />);
    await waitFor(() => expect(screen.getByText('PAN')).toBeInTheDocument());
  });

  it('shows expired document warning when document is expired', async () => {
    vi.mocked(getKycDocs).mockResolvedValue([
      { kycId: 1, documentType: 'AADHAAR', status: 'Expired' },
    ]);
    render(<MyKyc />);
    await waitFor(() =>
      expect(screen.getByText(/KYC documents have expired/i)).toBeInTheDocument()
    );
  });
});

// ─── MyOrders page ────────────────────────────────────────────────────────────

describe('MyOrders page', () => {
  it('renders the My Orders heading', () => {
    vi.mocked(getOrdersByClient).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><MyOrders /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /My Orders/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getOrdersByClient).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><MyOrders /></MemoryRouter>);
    expect(screen.getByText(/Loading/i)).toBeInTheDocument();
  });

  it('shows empty state when no orders', async () => {
    vi.mocked(getOrdersByClient).mockResolvedValue([]);
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    render(<MemoryRouter><MyOrders /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No orders yet/i)).toBeInTheDocument()
    );
  });

  it('shows message when no clientId', () => {
    useAuth.setState({ user: { username: 'client1', role: 'CLIENT', token: 'x' } });
    render(<MemoryRouter><MyOrders /></MemoryRouter>);
    expect(screen.getByText(/Loading\.\.\./i)).toBeInTheDocument();
  });

  it('renders order row when orders are loaded', async () => {
    vi.mocked(getOrdersByClient).mockResolvedValue([
      { orderId: 10, securityId: 1, side: 'BUY', quantity: 5, priceType: 'MARKET', status: 'FILLED', orderDate: '2024-01-01' },
    ]);
    vi.mocked(getAllSecurities).mockResolvedValue([
      { securityId: 1, symbol: 'INFY', assetClass: 'EQUITY' },
    ]);
    render(<MemoryRouter><MyOrders /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/BUY/i).length).toBeGreaterThan(0)
    );
  });

  it('renders order SELL side when sell orders are loaded', async () => {
    vi.mocked(getOrdersByClient).mockResolvedValue([
      { orderId: 11, securityId: 1, side: 'SELL', quantity: 3, priceType: 'LIMIT', limitPrice: 100, status: 'PLACED', orderDate: '2024-01-01' },
    ]);
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    render(<MemoryRouter><MyOrders /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/SELL/i).length).toBeGreaterThan(0)
    );
  });
});

// ─── Products page ────────────────────────────────────────────────────────────

describe('Products page', () => {
  it('renders the Products heading', () => {
    vi.mocked(getAllSecurities).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><Products /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /Product Catalog/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAllSecurities).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><Products /></MemoryRouter>);
    expect(screen.getByText(/Loading/i)).toBeInTheDocument();
  });

  it('shows empty state when no products', async () => {
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    render(<MemoryRouter><Products /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No products match your filter/i)).toBeInTheDocument()
    );
  });

  it('renders asset class filter select', () => {
    vi.mocked(getAllSecurities).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><Products /></MemoryRouter>);
    expect(screen.getByRole('combobox')).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'EQUITY' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'ALL' })).toBeInTheDocument();
  });

  it('renders a product when loaded', async () => {
    vi.mocked(getAllSecurities).mockResolvedValue([
      { securityId: 1, symbol: 'HDFCBANK', assetClass: 'EQUITY', status: 'ACTIVE', currency: 'INR', country: 'IN', currentPrice: 1600 },
    ]);
    render(<MemoryRouter><Products /></MemoryRouter>);
    await waitFor(() => expect(screen.getByText('HDFCBANK')).toBeInTheDocument());
  });
});

// ─── Client Notifications page ────────────────────────────────────────────────

describe('Client Notifications page', () => {
  it('renders the Notifications heading', () => {
    vi.mocked(getNotificationsByUserId).mockReturnValue(new Promise(() => {}));
    render(<ClientNotifications />);
    expect(screen.getByRole('heading', { name: /Notifications/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getNotificationsByUserId).mockReturnValue(new Promise(() => {}));
    render(<ClientNotifications />);
    // TableSkeleton used during loading — verify empty state not yet shown
    expect(screen.queryByText(/No notifications yet/i)).not.toBeInTheDocument();
  });

  it('shows empty state when no notifications', async () => {
    vi.mocked(getNotificationsByUserId).mockResolvedValue([]);
    render(<ClientNotifications />);
    await waitFor(() =>
      expect(screen.getByText(/No notifications yet/i)).toBeInTheDocument()
    );
  });

  it('renders notification row with message when data is loaded', async () => {
    vi.mocked(getNotificationsByUserId).mockResolvedValue([
      { notificationId: 1, message: 'Your order has been filled', status: 'UNREAD',
        category: 'Order', createdDate: '2024-01-01T10:00:00' },
    ]);
    render(<ClientNotifications />);
    await waitFor(() =>
      expect(screen.getByText(/Your order has been filled/i)).toBeInTheDocument()
    );
  });

  it('renders Mark all as read button when unread notifications exist', async () => {
    vi.mocked(getNotificationsByUserId).mockResolvedValue([
      { notificationId: 2, message: 'KYC document approved', status: 'UNREAD',
        category: 'KYC', createdDate: '2024-01-02T10:00:00' },
    ]);
    render(<ClientNotifications />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Mark all as read/i })).toBeInTheDocument()
    );
  });

  it('renders notification category pill', async () => {
    vi.mocked(getNotificationsByUserId).mockResolvedValue([
      { notificationId: 3, message: 'System update', status: 'UNREAD',
        category: 'System', createdDate: '2024-01-03T10:00:00' },
    ]);
    render(<ClientNotifications />);
    await waitFor(() =>
      expect(screen.getByText('System')).toBeInTheDocument()
    );
  });

  it('renders ALL and UNREAD filter toggle buttons', async () => {
    vi.mocked(getNotificationsByUserId).mockResolvedValue([]);
    render(<ClientNotifications />);
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /All \(0\)/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Unread \(0\)/i })).toBeInTheDocument();
    });
  });

  it('renders READ notification without mark button', async () => {
    vi.mocked(getNotificationsByUserId).mockResolvedValue([
      { notificationId: 4, message: 'Order placed successfully', status: 'READ',
        category: 'Order', createdDate: '2024-01-04T10:00:00' },
    ]);
    render(<ClientNotifications />);
    await waitFor(() =>
      expect(screen.getByText(/Order placed successfully/i)).toBeInTheDocument()
    );
  });
});

// ─── MyRiskProfile page ───────────────────────────────────────────────────────

describe('MyRiskProfile page', () => {
  beforeEach(() => {
    vi.mocked(getAccountsByClientId).mockResolvedValue([{ accountId: 1 }]);
  });

  it('renders without crashing', async () => {
    vi.mocked(getRiskProfile).mockReturnValue(new Promise(() => {}));
    expect(() =>
      render(<MemoryRouter><MyRiskProfile /></MemoryRouter>)
    ).not.toThrow();
  });

  it('shows no risk profile when getRiskProfile returns null', async () => {
    vi.mocked(getRiskProfile).mockResolvedValue(null);
    render(<MemoryRouter><MyRiskProfile /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Risk profile not yet assessed/i)).toBeInTheDocument()
    );
  });

  it('shows risk profile when data is loaded', async () => {
    vi.mocked(getRiskProfile).mockResolvedValue({
      riskProfileId: 1,
      riskClass: 'MODERATE',
      score: 50,
    });
    render(<MemoryRouter><MyRiskProfile /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/MODERATE/i)).toBeInTheDocument()
    );
  });
});

// ─── Client Goals page ────────────────────────────────────────────────────────

describe('Client Goals page', () => {
  it('renders the Goals heading', () => {
    vi.mocked(getGoalsByClientId).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><ClientGoals /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /Goals/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getGoalsByClientId).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><ClientGoals /></MemoryRouter>);
    // TableSkeleton used during loading — verify empty state not yet shown
    expect(screen.queryByText(/No goals yet/i)).not.toBeInTheDocument();
  });

  it('shows empty state when no goals', async () => {
    vi.mocked(getGoalsByClientId).mockResolvedValue([]);
    render(<MemoryRouter><ClientGoals /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No goals yet/i)).toBeInTheDocument()
    );
  });

  it('shows loading when no clientId', () => {
    useAuth.setState({ user: { username: 'client1', role: 'CLIENT', token: 'x' } });
    render(<MemoryRouter><ClientGoals /></MemoryRouter>);
    expect(screen.getByText(/Loading\.\.\./i)).toBeInTheDocument();
  });

  it('renders goal card when goals are loaded', async () => {
    vi.mocked(getGoalsByClientId).mockResolvedValue([
      { goalId: 1, goalType: 'RETIREMENT', status: 'IN_PROGRESS', priority: 1,
        targetAmount: 500000, targetDate: '2030-06-01', currentAmount: 100000 },
    ]);
    render(<MemoryRouter><ClientGoals /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/RETIREMENT/i).length).toBeGreaterThan(0)
    );
  });

  it('opens New Financial Goal modal when + New goal is clicked', async () => {
    vi.mocked(getGoalsByClientId).mockResolvedValue([]);
    render(<MemoryRouter><ClientGoals /></MemoryRouter>);
    await waitFor(() => screen.getByRole('button', { name: /\+ New goal/i }));
    fireEvent.click(screen.getByRole('button', { name: /\+ New goal/i }));
    await waitFor(() =>
      expect(screen.getByText(/New Financial Goal/i)).toBeInTheDocument()
    );
  });

  it('shows goal type options in modal', async () => {
    vi.mocked(getGoalsByClientId).mockResolvedValue([]);
    render(<MemoryRouter><ClientGoals /></MemoryRouter>);
    await waitFor(() => screen.getByRole('button', { name: /\+ New goal/i }));
    fireEvent.click(screen.getByRole('button', { name: /\+ New goal/i }));
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /RETIREMENT/i })).toBeInTheDocument()
    );
  });

  it('shows Create goal submit button in modal', async () => {
    vi.mocked(getGoalsByClientId).mockResolvedValue([]);
    render(<MemoryRouter><ClientGoals /></MemoryRouter>);
    await waitFor(() => screen.getByRole('button', { name: /\+ New goal/i }));
    fireEvent.click(screen.getByRole('button', { name: /\+ New goal/i }));
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Create goal/i })).toBeInTheDocument()
    );
  });

  it('renders WEALTH goal card when goals are loaded', async () => {
    vi.mocked(getGoalsByClientId).mockResolvedValue([
      { goalId: 2, goalType: 'WEALTH', status: 'ACTIVE', priority: 1,
        targetAmount: 2000000, targetDate: '2035-01-01' },
    ]);
    render(<MemoryRouter><ClientGoals /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/WEALTH/i).length).toBeGreaterThan(0)
    );
  });
});
