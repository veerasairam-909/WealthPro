import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

// Mock all API modules used by admin pages
vi.mock('@/api/admin', () => ({
  getAllUsers: vi.fn(),
  getUserByUsername: vi.fn(),
  deleteUser: vi.fn(),
  getAuditLogs: vi.fn(),
}));

vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  logoutApi: vi.fn(),
  registerClient: vi.fn(),
  registerStaff: vi.fn(),
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

vi.mock('@/api/recommendations', () => ({
  getAllModelPortfolios: vi.fn(),
  createModelPortfolio: vi.fn(),
  updateModelPortfolio: vi.fn(),
  deleteModelPortfolio: vi.fn(),
  getRecommendationsByClientId: vi.fn(),
  createRecommendation: vi.fn(),
  updateRecommendationStatus: vi.fn(),
  deleteRecommendation: vi.fn(),
}));

vi.mock('@/api/securities', () => ({
  getAllSecurities: vi.fn(),
  getSecurityById: vi.fn(),
  createSecurity: vi.fn(),
  updateSecurity: vi.fn(),
  deleteSecurity: vi.fn(),
}));

vi.mock('@/api/orders', () => ({
  getAllOrders: vi.fn(),
  getOrdersByStatus: vi.fn(),
}));

vi.mock('@/api/analytics', () => ({
  getRiskMeasuresByAccount: vi.fn(),
  getBreachesByAccount: vi.fn(),
  getPerformanceByAccount: vi.fn(),
  getAccountDashboard: vi.fn(),
  runComplianceScan: vi.fn(),
}));

vi.mock('@/api/accounts', () => ({
  getAllAccounts: vi.fn(),
  getAccountsByClientId: vi.fn(),
}));

vi.mock('@/api/clients', () => ({
  getAllClients: vi.fn(),
  getClientById: vi.fn(),
  getKycDocs: vi.fn(),
  getRiskProfile: vi.fn(),
}));

vi.mock('@/api/amlFlags', () => ({
  getAllAmlFlags: vi.fn(),
}));

vi.mock('@/lib/fetchUtils', () => ({
  cachedFetch: vi.fn(),
  parallelLimit: vi.fn(),
  invalidateCache: vi.fn(),
}));

// Mock recharts to avoid ResizeObserver issues
vi.mock('recharts', () => ({
  BarChart: ({ children }: any) => <div data-testid="bar-chart">{children}</div>,
  Bar: () => null,
  XAxis: () => null,
  YAxis: () => null,
  Tooltip: () => null,
  ResponsiveContainer: ({ children }: any) => <div>{children}</div>,
  PieChart: ({ children }: any) => <div data-testid="pie-chart">{children}</div>,
  Pie: () => null,
  Cell: () => null,
  Legend: () => null,
}));

import AdminUsers from '@/pages/admin/Users';
import Audit from '@/pages/admin/Audit';
import RegisterStaff from '@/pages/admin/RegisterStaff';
import ModelPortfolios from '@/pages/admin/ModelPortfolios';
import AdminSecurities from '@/pages/admin/AdminSecurities';
import AdminDashboard from '@/pages/admin/AdminDashboard';

import { getAllUsers, getAuditLogs } from '@/api/admin';
import { getAllModelPortfolios, createModelPortfolio } from '@/api/recommendations';
import { getAllSecurities, createSecurity } from '@/api/securities';

beforeEach(() => {
  vi.clearAllMocks();
  vi.mocked(getAllUsers).mockResolvedValue([]);
  vi.mocked(getAuditLogs).mockResolvedValue([]);
  vi.mocked(getAllModelPortfolios).mockResolvedValue([]);
  vi.mocked(getAllSecurities).mockResolvedValue([]);
  vi.mocked(createModelPortfolio).mockResolvedValue(undefined as any);
  vi.mocked(createSecurity).mockResolvedValue(undefined as any);
});

// ─── Users page ───────────────────────────────────────────────────────────────

describe('Admin Users page', () => {
  it('renders the Users heading', async () => {
    vi.mocked(getAllUsers).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><AdminUsers /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /Users/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAllUsers).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><AdminUsers /></MemoryRouter>);
    expect(screen.getByText(/Loading/i)).toBeInTheDocument();
  });

  it('shows empty state when no users', async () => {
    vi.mocked(getAllUsers).mockResolvedValue([]);
    render(<MemoryRouter><AdminUsers /></MemoryRouter>);
    await waitFor(() => expect(screen.getByText(/No users found/i)).toBeInTheDocument());
  });

  it('renders a user row when data is loaded', async () => {
    vi.mocked(getAllUsers).mockResolvedValue([
      { username: 'admin1', role: 'ADMIN', userId: 1 },
    ]);
    render(<MemoryRouter><AdminUsers /></MemoryRouter>);
    await waitFor(() => expect(screen.getByText('admin1')).toBeInTheDocument());
  });

  it('renders role filter buttons', () => {
    vi.mocked(getAllUsers).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><AdminUsers /></MemoryRouter>);
    expect(screen.getByRole('button', { name: 'ALL' })).toBeInTheDocument();
  });
});

// ─── Audit page ───────────────────────────────────────────────────────────────

describe('Admin Audit page', () => {
  it('renders the Audit Log heading', async () => {
    vi.mocked(getAuditLogs).mockReturnValue(new Promise(() => {}));
    render(<Audit />);
    expect(screen.getByRole('heading', { name: /Audit Log/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAuditLogs).mockReturnValue(new Promise(() => {}));
    render(<Audit />);
    expect(screen.getByText(/Loading/i)).toBeInTheDocument();
  });

  it('shows empty state when no audit logs', async () => {
    vi.mocked(getAuditLogs).mockResolvedValue([]);
    render(<Audit />);
    await waitFor(() => expect(screen.getByText(/No audit events/i)).toBeInTheDocument());
  });

  it('renders method filter buttons', () => {
    vi.mocked(getAuditLogs).mockReturnValue(new Promise(() => {}));
    render(<Audit />);
    expect(screen.getByRole('button', { name: 'GET' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'POST' })).toBeInTheDocument();
  });
});

// ─── RegisterStaff page ───────────────────────────────────────────────────────

describe('RegisterStaff page', () => {
  it('renders the Register Staff heading', () => {
    render(
      <MemoryRouter>
        <RegisterStaff />
      </MemoryRouter>
    );
    expect(screen.getByRole('heading', { name: /Register Staff/i })).toBeInTheDocument();
  });

  it('renders a form with name input', () => {
    render(
      <MemoryRouter>
        <RegisterStaff />
      </MemoryRouter>
    );
    expect(screen.getByPlaceholderText(/Anita Sharma/i)).toBeInTheDocument();
  });

  it('renders role selection buttons', () => {
    render(
      <MemoryRouter>
        <RegisterStaff />
      </MemoryRouter>
    );
    expect(screen.getByRole('button', { name: 'RM' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'DEALER' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'COMPLIANCE' })).toBeInTheDocument();
  });

  it('renders a submit button', () => {
    render(
      <MemoryRouter>
        <RegisterStaff />
      </MemoryRouter>
    );
    expect(screen.getByRole('button', { name: /Create User/i })).toBeInTheDocument();
  });
});

// ─── ModelPortfolios page ─────────────────────────────────────────────────────

describe('ModelPortfolios page', () => {
  it('renders the Model Portfolios heading', async () => {
    vi.mocked(getAllModelPortfolios).mockReturnValue(new Promise(() => {}));
    render(<ModelPortfolios />);
    expect(screen.getByRole('heading', { name: /Model Portfolios/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAllModelPortfolios).mockReturnValue(new Promise(() => {}));
    render(<ModelPortfolios />);
    expect(screen.getByText(/Loading/i)).toBeInTheDocument();
  });

  it('shows empty state when no portfolios', async () => {
    vi.mocked(getAllModelPortfolios).mockResolvedValue([]);
    render(<ModelPortfolios />);
    await waitFor(() =>
      expect(screen.getByText(/No model portfolios/i)).toBeInTheDocument()
    );
  });

  it('renders a portfolio when loaded', async () => {
    vi.mocked(getAllModelPortfolios).mockResolvedValue([
      { portfolioId: 1, name: 'Growth Fund', riskClass: 'AGGRESSIVE', status: 'ACTIVE' },
    ]);
    render(<ModelPortfolios />);
    await waitFor(() => expect(screen.getByText('Growth Fund')).toBeInTheDocument());
  });
});

// ─── AdminSecurities page ─────────────────────────────────────────────────────

describe('AdminSecurities page', () => {
  it('renders the Securities heading', async () => {
    vi.mocked(getAllSecurities).mockReturnValue(new Promise(() => {}));
    render(<AdminSecurities />);
    expect(screen.getByRole('heading', { name: /Securities/i })).toBeInTheDocument();
  });

  it('shows empty state when no securities', async () => {
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    render(<AdminSecurities />);
    await waitFor(() =>
      expect(screen.getByText(/No securities found/i)).toBeInTheDocument()
    );
  });

  it('shows a security when loaded', async () => {
    vi.mocked(getAllSecurities).mockResolvedValue([
      { securityId: 1, symbol: 'INFY', assetClass: 'EQUITY', status: 'ACTIVE', currency: 'INR', country: 'IN', currentPrice: 100 },
    ]);
    render(<AdminSecurities />);
    await waitFor(() => expect(screen.getByText('INFY')).toBeInTheDocument());
  });

  it('renders the Add Security button', async () => {
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    render(<AdminSecurities />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Add Security/i })).toBeInTheDocument()
    );
  });
});

// ─── AdminDashboard page ──────────────────────────────────────────────────────

describe('AdminDashboard page', () => {
  it('renders the dashboard heading', () => {
    render(
      <MemoryRouter>
        <AdminDashboard />
      </MemoryRouter>
    );
    expect(screen.getByRole('heading', { name: /Admin Dashboard/i })).toBeInTheDocument();
  });

  it('renders without crashing', () => {
    expect(() =>
      render(<MemoryRouter><AdminDashboard /></MemoryRouter>)
    ).not.toThrow();
  });

  it('renders Total Users KPI label after data loads', async () => {
    vi.mocked(getAllUsers).mockResolvedValue([
      { username: 'admin1', roles: 'ADMIN', userId: 1 },
      { username: 'client1', roles: 'CLIENT', userId: 2 },
    ]);
    vi.mocked(getAuditLogs).mockResolvedValue([]);
    render(<MemoryRouter><AdminDashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/Total Users/i).length).toBeGreaterThan(0)
    );
  });

  it('renders Manage Users quick action after data loads', async () => {
    vi.mocked(getAllUsers).mockResolvedValue([]);
    vi.mocked(getAuditLogs).mockResolvedValue([]);
    render(<MemoryRouter><AdminDashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Manage Users/i)).toBeInTheDocument()
    );
  });

  it('renders No audit data yet when audit log is empty', async () => {
    vi.mocked(getAllUsers).mockResolvedValue([]);
    vi.mocked(getAuditLogs).mockResolvedValue([]);
    render(<MemoryRouter><AdminDashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No audit data yet/i)).toBeInTheDocument()
    );
  });

  it('renders Clients KPI label after data loads', async () => {
    vi.mocked(getAllUsers).mockResolvedValue([
      { username: 'client1', roles: 'CLIENT', userId: 3 },
    ]);
    vi.mocked(getAuditLogs).mockResolvedValue([]);
    render(<MemoryRouter><AdminDashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/Clients/i).length).toBeGreaterThan(0)
    );
  });

  it('renders recent activity section after load', async () => {
    vi.mocked(getAllUsers).mockResolvedValue([]);
    vi.mocked(getAuditLogs).mockResolvedValue([
      { auditId: 1, username: 'admin1', method: 'POST', path: '/api/clients', timestamp: '2024-01-01T10:00:00', statusCode: 200 },
    ]);
    render(<MemoryRouter><AdminDashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/admin1/i).length).toBeGreaterThan(0)
    );
  });

  it('renders Register Staff link after load', async () => {
    vi.mocked(getAllUsers).mockResolvedValue([]);
    vi.mocked(getAuditLogs).mockResolvedValue([]);
    render(<MemoryRouter><AdminDashboard /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/Register Staff/i).length).toBeGreaterThan(0)
    );
  });
});

// ─── ModelPortfolios form tests ───────────────────────────────────────────────

describe('ModelPortfolios form tests', () => {
  it('opens New Model Portfolio modal when + New Portfolio is clicked', async () => {
    vi.mocked(getAllModelPortfolios).mockResolvedValue([]);
    render(<ModelPortfolios />);
    await waitFor(() => screen.getByRole('button', { name: /\+ New Portfolio/i }));
    fireEvent.click(screen.getByRole('button', { name: /\+ New Portfolio/i }));
    await waitFor(() =>
      expect(screen.getByText(/New Model Portfolio/i)).toBeInTheDocument()
    );
  });

  it('modal form has Name input field', async () => {
    vi.mocked(getAllModelPortfolios).mockResolvedValue([]);
    render(<ModelPortfolios />);
    await waitFor(() => screen.getByRole('button', { name: /\+ New Portfolio/i }));
    fireEvent.click(screen.getByRole('button', { name: /\+ New Portfolio/i }));
    await waitFor(() =>
      expect(screen.getByPlaceholderText(/Balanced Growth Fund/i)).toBeInTheDocument()
    );
  });

  it('modal form has risk class select', async () => {
    vi.mocked(getAllModelPortfolios).mockResolvedValue([]);
    render(<ModelPortfolios />);
    await waitFor(() => screen.getByRole('button', { name: /\+ New Portfolio/i }));
    fireEvent.click(screen.getByRole('button', { name: /\+ New Portfolio/i }));
    await waitFor(() =>
      expect(screen.getByRole('option', { name: 'CONSERVATIVE' })).toBeInTheDocument()
    );
  });

  it('modal has Create Portfolio submit button', async () => {
    vi.mocked(getAllModelPortfolios).mockResolvedValue([]);
    render(<ModelPortfolios />);
    await waitFor(() => screen.getByRole('button', { name: /\+ New Portfolio/i }));
    fireEvent.click(screen.getByRole('button', { name: /\+ New Portfolio/i }));
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Create Portfolio/i })).toBeInTheDocument()
    );
  });

  it('renders Edit button for each portfolio', async () => {
    vi.mocked(getAllModelPortfolios).mockResolvedValue([
      { portfolioId: 1, name: 'Growth Fund', riskClass: 'AGGRESSIVE', status: 'ACTIVE', weightsJson: '' },
    ]);
    render(<ModelPortfolios />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Edit/i })).toBeInTheDocument()
    );
  });

  it('renders Delete button for each portfolio', async () => {
    vi.mocked(getAllModelPortfolios).mockResolvedValue([
      { portfolioId: 2, name: 'Balanced Fund', riskClass: 'BALANCED', status: 'ACTIVE', weightsJson: '' },
    ]);
    render(<ModelPortfolios />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Delete/i })).toBeInTheDocument()
    );
  });

  it('opens Edit form with portfolio data when Edit is clicked', async () => {
    vi.mocked(getAllModelPortfolios).mockResolvedValue([
      { portfolioId: 3, name: 'Conservative Bundle', riskClass: 'CONSERVATIVE', status: 'ACTIVE', weightsJson: '' },
    ]);
    render(<ModelPortfolios />);
    await waitFor(() => screen.getByRole('button', { name: /Edit/i }));
    fireEvent.click(screen.getByRole('button', { name: /Edit/i }));
    await waitFor(() =>
      expect(screen.getByText(/Edit Model Portfolio/i)).toBeInTheDocument()
    );
  });
});

// ─── AdminSecurities extra tests ──────────────────────────────────────────────

describe('AdminSecurities extra tests', () => {
  it('opens Add Security modal when Add Security is clicked', async () => {
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    render(<AdminSecurities />);
    const addBtns = await waitFor(() => screen.getAllByRole('button', { name: /Add Security/i }));
    fireEvent.click(addBtns[0]);
    await waitFor(() =>
      expect(screen.getByPlaceholderText(/HDFCBANK/i)).toBeInTheDocument()
    );
  });

  it('renders Edit button for each security', async () => {
    vi.mocked(getAllSecurities).mockResolvedValue([
      { securityId: 1, symbol: 'RELIANCE', assetClass: 'EQUITY', status: 'ACTIVE', currency: 'INR', country: 'IN', currentPrice: 2500 },
    ]);
    render(<AdminSecurities />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Edit/i })).toBeInTheDocument()
    );
  });
});

// ─── Admin Users extra tests ──────────────────────────────────────────────────

describe('Admin Users extra tests', () => {
  it('renders search input in Users page', async () => {
    vi.mocked(getAllUsers).mockResolvedValue([]);
    render(<MemoryRouter><AdminUsers /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByPlaceholderText(/Search/i)).toBeInTheDocument()
    );
  });

  it('renders RM role filter button', async () => {
    vi.mocked(getAllUsers).mockResolvedValue([]);
    render(<MemoryRouter><AdminUsers /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: 'RM' })).toBeInTheDocument()
    );
  });

  it('renders CLIENT role filter button', async () => {
    vi.mocked(getAllUsers).mockResolvedValue([]);
    render(<MemoryRouter><AdminUsers /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: 'CLIENT' })).toBeInTheDocument()
    );
  });
});

// ─── RegisterStaff extra tests ────────────────────────────────────────────────

describe('RegisterStaff extra tests', () => {
  it('renders all required form fields', () => {
    render(<MemoryRouter><RegisterStaff /></MemoryRouter>);
    expect(screen.getByPlaceholderText(/Anita Sharma/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'RM' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'DEALER' })).toBeInTheDocument();
  });

  it('renders COMPLIANCE role button', () => {
    render(<MemoryRouter><RegisterStaff /></MemoryRouter>);
    expect(screen.getByRole('button', { name: 'COMPLIANCE' })).toBeInTheDocument();
  });

  it('renders without crashing', () => {
    expect(() =>
      render(<MemoryRouter><RegisterStaff /></MemoryRouter>)
    ).not.toThrow();
  });
});
