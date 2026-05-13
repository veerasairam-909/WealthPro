import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

vi.mock('@/api/suitabilityRules', () => ({
  getAllSuitabilityRules: vi.fn(),
  createSuitabilityRule: vi.fn(),
  updateSuitabilityRule: vi.fn(),
  deleteSuitabilityRule: vi.fn(),
}));

vi.mock('@/api/amlFlags', () => ({
  getAllAmlFlags: vi.fn(),
  getAmlFlagsByClient: vi.fn(),
  createAmlFlag: vi.fn(),
  reviewAmlFlag: vi.fn(),
  deleteAmlFlag: vi.fn(),
  requestClosureAmlFlag: vi.fn(),
}));

vi.mock('@/api/notifications', () => ({
  createNotification: vi.fn(),
  getNotificationsByUserId: vi.fn(),
  getUnreadNotifications: vi.fn(),
  markAllAsRead: vi.fn(),
  markNotificationRead: vi.fn(),
}));

vi.mock('@/api/admin', () => ({
  getAllUsers: vi.fn(),
  getUserByUsername: vi.fn(),
  deleteUser: vi.fn(),
  getAuditLogs: vi.fn(),
}));

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

vi.mock('@/api/accounts', () => ({
  getAccountsByClientId: vi.fn(),
  getAllAccounts: vi.fn(),
  getAccountById: vi.fn(),
}));

vi.mock('@/api/holdings', () => ({
  getHoldingsByAccountId: vi.fn(),
  getHoldingById: vi.fn(),
}));

vi.mock('@/api/securities', () => ({
  getAllSecurities: vi.fn(),
  getSecurityById: vi.fn(),
}));

vi.mock('@/api/analytics', () => ({
  getBreachesByAccount: vi.fn(),
  runComplianceScan: vi.fn(),
  acknowledgeBreach: vi.fn(),
  closeBreach: vi.fn(),
  getRiskMeasuresByAccount: vi.fn(),
  getPerformanceByAccount: vi.fn(),
}));

vi.mock('@/api/orders', () => ({
  getAllOrders: vi.fn(),
  getOrdersByStatus: vi.fn(),
  getOrderById: vi.fn(),
}));

vi.mock('@/lib/fetchUtils', () => ({
  cachedFetch: vi.fn(),
  parallelLimit: vi.fn(),
  invalidateCache: vi.fn(),
}));

import { useAuth } from '@/auth/store';
import SuitabilityRules from '@/pages/compliance/SuitabilityRules';
import AmlFlags from '@/pages/compliance/AmlFlags';
import ComplianceAudit from '@/pages/compliance/ComplianceAudit';
import KYCApproval from '@/pages/compliance/KYCApproval';
import RiskMonitor from '@/pages/compliance/RiskMonitor';
import FailedOrders from '@/pages/compliance/FailedOrders';
import Breaches from '@/pages/compliance/Breaches';

import { getAllSuitabilityRules } from '@/api/suitabilityRules';
import { getAllAmlFlags } from '@/api/amlFlags';
import { getAuditLogs, getAllUsers } from '@/api/admin';
import { getAllClients, getKycDocs, getRiskProfile } from '@/api/clients';
import { getOrdersByStatus } from '@/api/orders';
import { cachedFetch, parallelLimit } from '@/lib/fetchUtils';
import { getAllSecurities } from '@/api/securities';

beforeEach(() => {
  vi.clearAllMocks();
  useAuth.setState({ user: null });
  vi.mocked(getAllSuitabilityRules).mockResolvedValue([]);
  vi.mocked(getAllAmlFlags).mockResolvedValue([]);
  vi.mocked(getAuditLogs).mockResolvedValue([]);
  vi.mocked(getAllClients).mockResolvedValue([]);
  vi.mocked(getOrdersByStatus).mockResolvedValue([]);
  vi.mocked(cachedFetch).mockReturnValue(undefined as any);
  vi.mocked(parallelLimit).mockResolvedValue([]);
  vi.mocked(getAllSecurities).mockResolvedValue([]);
});

// ─── SuitabilityRules page ────────────────────────────────────────────────────

describe('SuitabilityRules page', () => {
  it('renders the Suitability Rules heading', () => {
    vi.mocked(getAllSuitabilityRules).mockReturnValue(new Promise(() => {}));
    render(<SuitabilityRules />);
    expect(screen.getByRole('heading', { name: /Suitability Rules/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAllSuitabilityRules).mockReturnValue(new Promise(() => {}));
    render(<SuitabilityRules />);
    // TableSkeleton used during loading — verify empty state not yet shown
    expect(screen.queryByText(/No suitability rules/i)).not.toBeInTheDocument();
  });

  it('shows empty state when no rules', async () => {
    vi.mocked(getAllSuitabilityRules).mockResolvedValue([]);
    render(<SuitabilityRules />);
    await waitFor(() =>
      expect(screen.getByText(/No suitability rules/i)).toBeInTheDocument()
    );
  });

  it('renders a rule when loaded', async () => {
    vi.mocked(getAllSuitabilityRules).mockResolvedValue([
      { ruleId: 1, description: 'Conservative only', expression: 'riskClass == CONSERVATIVE', status: 'ACTIVE' },
    ]);
    render(<SuitabilityRules />);
    await waitFor(() => expect(screen.getByText('Conservative only')).toBeInTheDocument());
  });

  it('renders New Rule button', async () => {
    vi.mocked(getAllSuitabilityRules).mockResolvedValue([]);
    render(<SuitabilityRules />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /New Rule/i })).toBeInTheDocument()
    );
  });
});

// ─── AmlFlags page ────────────────────────────────────────────────────────────

describe('AmlFlags page', () => {
  beforeEach(() => {
    useAuth.setState({ user: { username: 'comp1', role: 'COMPLIANCE', token: 'x' } });
  });

  it('renders the AML Flags heading', () => {
    vi.mocked(getAllAmlFlags).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /AML Flags/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAllAmlFlags).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    expect(screen.getByText(/Loading/i)).toBeInTheDocument();
  });

  it('shows empty state when no flags', async () => {
    vi.mocked(getAllAmlFlags).mockResolvedValue([]);
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/No AML flags found/i)).toBeInTheDocument()
    );
  });

  it('renders status filter buttons', () => {
    vi.mocked(getAllAmlFlags).mockReturnValue(new Promise(() => {}));
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    expect(screen.getByRole('button', { name: 'OPEN' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'All' })).toBeInTheDocument();
  });

  it('renders flag table row when flags are loaded', async () => {
    vi.mocked(getAllAmlFlags).mockResolvedValue([
      { amlFlagId: 1, clientId: 10, flagType: 'SUSPICIOUS_TRANSACTION', status: 'OPEN', description: 'Test desc', flaggedDate: '2024-01-01T00:00:00' },
    ]);
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/Client 10/i)).toBeInTheDocument()
    );
  });

  it('renders SUSPICIOUS TRANSACTION flag type text', async () => {
    vi.mocked(getAllAmlFlags).mockResolvedValue([
      { amlFlagId: 2, clientId: 20, flagType: 'SUSPICIOUS_TRANSACTION', status: 'OPEN', description: 'Unusual', flaggedDate: '2024-02-01T00:00:00' },
    ]);
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/SUSPICIOUS TRANSACTION/i)).toBeInTheDocument()
    );
  });

  it('renders Raise Flag button for compliance user', async () => {
    vi.mocked(getAllAmlFlags).mockResolvedValue([]);
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /\+ Raise Flag/i })).toBeInTheDocument()
    );
  });

  it('opens create flag modal when Raise Flag is clicked', async () => {
    vi.mocked(getAllAmlFlags).mockResolvedValue([]);
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /\+ Raise Flag/i })).toBeInTheDocument()
    );
    fireEvent.click(screen.getByRole('button', { name: /\+ Raise Flag/i }));
    await waitFor(() =>
      expect(screen.getByText(/Raise AML Flag/i)).toBeInTheDocument()
    );
  });

  it('renders summary tiles with counts', async () => {
    vi.mocked(getAllAmlFlags).mockResolvedValue([
      { amlFlagId: 1, clientId: 5, flagType: 'MANUAL', status: 'OPEN', description: 'x' },
    ]);
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText(/Open/i).length).toBeGreaterThan(0)
    );
  });

  it('renders REVIEWED status flag', async () => {
    vi.mocked(getAllAmlFlags).mockResolvedValue([
      { amlFlagId: 3, clientId: 7, flagType: 'MANUAL', status: 'REVIEWED', description: 'reviewed flag' },
    ]);
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/REVIEWED/)).toBeInTheDocument()
    );
  });
});

// ─── ComplianceAudit page ─────────────────────────────────────────────────────

describe('ComplianceAudit page', () => {
  it('renders the Compliance Audit heading', () => {
    vi.mocked(getAuditLogs).mockReturnValue(new Promise(() => {}));
    render(<ComplianceAudit />);
    expect(screen.getByRole('heading', { name: /Compliance Audit/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAuditLogs).mockReturnValue(new Promise(() => {}));
    render(<ComplianceAudit />);
    expect(screen.getByText(/Loading/i)).toBeInTheDocument();
  });

  it('shows empty state when no logs', async () => {
    vi.mocked(getAuditLogs).mockResolvedValue([]);
    render(<ComplianceAudit />);
    await waitFor(() =>
      expect(screen.getByText(/No audit entries/i)).toBeInTheDocument()
    );
  });
});

// ─── KYCApproval page ─────────────────────────────────────────────────────────

describe('KYCApproval page', () => {
  it('renders the KYC Approval heading', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<KYCApproval />);
    expect(screen.getByRole('heading', { name: /KYC Approval/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<KYCApproval />);
    expect(screen.getByText(/Loading KYC documents/i)).toBeInTheDocument();
  });

  it('shows empty state when no clients', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<KYCApproval />);
    await waitFor(() =>
      expect(screen.getByText(/No documents are awaiting review/i)).toBeInTheDocument()
    );
  });

  it('renders status tab filters', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<KYCApproval />);
    expect(screen.getByRole('button', { name: /^All/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /^Pending/i })).toBeInTheDocument();
  });
});

// ─── RiskMonitor page ─────────────────────────────────────────────────────────

describe('RiskMonitor page', () => {
  it('renders the Risk Monitor heading', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<RiskMonitor />);
    expect(screen.getByRole('heading', { name: /Risk Monitor/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<RiskMonitor />);
    expect(screen.getByText(/Analysing portfolios/i)).toBeInTheDocument();
  });

  it('shows empty state when no clients', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<RiskMonitor />);
    await waitFor(() =>
      expect(screen.getByText(/No portfolio data available yet/i)).toBeInTheDocument()
    );
  });
});

// ─── FailedOrders page ────────────────────────────────────────────────────────

describe('FailedOrders page', () => {
  it('renders the Failed Orders heading', () => {
    vi.mocked(getOrdersByStatus).mockReturnValue(new Promise(() => {}));
    render(<FailedOrders />);
    expect(screen.getByRole('heading', { name: /Failed.*Rejected Orders/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getOrdersByStatus).mockReturnValue(new Promise(() => {}));
    render(<FailedOrders />);
    expect(screen.getByText(/Loading rejected orders/i)).toBeInTheDocument();
  });

  it('shows empty state when no failed orders', async () => {
    vi.mocked(getOrdersByStatus).mockResolvedValue([]);
    render(<FailedOrders />);
    await waitFor(() =>
      expect(screen.getByText(/No rejected orders/i)).toBeInTheDocument()
    );
  });
});

// ─── Breaches page ────────────────────────────────────────────────────────────

describe('Breaches page', () => {
  it('renders the Compliance Breaches heading', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<Breaches />);
    expect(screen.getAllByRole('heading', { name: /Compliance Breaches/i }).length).toBeGreaterThan(0);
  });

  it('shows loading state', () => {
    vi.mocked(getAllClients).mockReturnValue(new Promise(() => {}));
    render(<Breaches />);
    expect(screen.getByText(/Scanning clients for compliance issues/i)).toBeInTheDocument();
  });

  it('shows empty state when no breaches', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<Breaches />);
    await waitFor(() =>
      expect(screen.getByText(/No breaches match current filters/i)).toBeInTheDocument()
    );
  });

  it('renders severity filter buttons', async () => {
    vi.mocked(getAllClients).mockResolvedValue([]);
    render(<Breaches />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /HIGH/i })).toBeInTheDocument()
    );
  });

  it('renders without crashing', () => {
    expect(() => render(<Breaches />)).not.toThrow();
  });
});

// ─── KYCApproval with data ────────────────────────────────────────────────────

describe('KYCApproval with data loaded', () => {
  it('renders document row when KYC docs are loaded', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 1, name: 'Vikram Mehta' },
    ]);
    vi.mocked(parallelLimit).mockResolvedValue([
      { status: 'fulfilled', value: [
        { kycId: 1, documentType: 'PAN', status: 'Pending', verifiedDate: null, documentRef: 'REF001' },
      ]},
    ]);
    render(<KYCApproval />);
    await waitFor(() =>
      expect(screen.getByText('Vikram Mehta')).toBeInTheDocument()
    );
  });

  it('renders Approve button for Pending document', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 2, name: 'Sunita Sharma' },
    ]);
    vi.mocked(parallelLimit).mockResolvedValue([
      { status: 'fulfilled', value: [
        { kycId: 2, documentType: 'AADHAAR', status: 'Pending', verifiedDate: null, documentRef: 'REF002' },
      ]},
    ]);
    render(<KYCApproval />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Approve/i })).toBeInTheDocument()
    );
  });

  it('renders Reject button for Pending document', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 3, name: 'Ravi Kumar' },
    ]);
    vi.mocked(parallelLimit).mockResolvedValue([
      { status: 'fulfilled', value: [
        { kycId: 3, documentType: 'PASSPORT', status: 'Pending', verifiedDate: null, documentRef: 'REF003' },
      ]},
    ]);
    render(<KYCApproval />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Reject/i })).toBeInTheDocument()
    );
  });

  it('renders Verified document row', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 4, name: 'Anita Das' },
    ]);
    vi.mocked(parallelLimit).mockResolvedValue([
      { status: 'fulfilled', value: [
        { kycId: 4, documentType: 'PAN', status: 'Verified', verifiedDate: '2024-01-01', documentRef: 'REF004' },
      ]},
    ]);
    render(<KYCApproval />);
    // switch to All tab to see verified docs
    await waitFor(() => screen.getByRole('button', { name: /^All/i }));
    fireEvent.click(screen.getByRole('button', { name: /^All/i }));
    await waitFor(() =>
      expect(screen.getByText('Anita Das')).toBeInTheDocument()
    );
  });

  it('renders KPI cards Total Documents, Pending Review, Verified, Rejected', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([{ clientId: 1, name: 'Test' }]);
    vi.mocked(parallelLimit).mockResolvedValue([{ status: 'fulfilled', value: [] }]);
    render(<KYCApproval />);
    await waitFor(() =>
      expect(screen.getByText(/Total Documents/i)).toBeInTheDocument()
    );
  });

  it('shows All KYC documents are up to date when no pending docs', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([{ clientId: 1, name: 'Clear Client' }]);
    vi.mocked(parallelLimit).mockResolvedValue([
      { status: 'fulfilled', value: [
        { kycId: 5, documentType: 'PAN', status: 'Verified', verifiedDate: '2024-01-01', documentRef: 'V001' },
      ]},
    ]);
    render(<KYCApproval />);
    await waitFor(() =>
      expect(screen.getByText(/All KYC documents are up to date/i)).toBeInTheDocument()
    );
  });
});

// ─── FailedOrders with data ───────────────────────────────────────────────────

describe('FailedOrders with data loaded', () => {
  it('renders rejected order row in table', async () => {
    vi.mocked(getOrdersByStatus).mockResolvedValue([
      { orderId: 42, orderDate: '2024-01-15T10:00:00', clientId: 5, securityId: 1,
        side: 'BUY', quantity: 10, priceType: 'MARKET', status: 'REJECTED' },
    ]);
    render(<FailedOrders />);
    await waitFor(() =>
      expect(screen.getAllByText('REJECTED').length).toBeGreaterThan(0)
    );
  });

  it('renders Notify button in order table row', async () => {
    vi.mocked(getOrdersByStatus).mockResolvedValue([
      { orderId: 43, orderDate: '2024-01-16T10:00:00', clientId: 6, securityId: 2,
        side: 'SELL', quantity: 5, priceType: 'LIMIT', status: 'REJECTED' },
    ]);
    render(<FailedOrders />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Notify/i })).toBeInTheDocument()
    );
  });

  it('renders Total Rejected KPI card value after load', async () => {
    vi.mocked(getOrdersByStatus).mockResolvedValue([
      { orderId: 44, orderDate: '2024-01-17T10:00:00', clientId: 7, securityId: 3,
        side: 'BUY', quantity: 3, priceType: 'MARKET', status: 'REJECTED' },
    ]);
    render(<FailedOrders />);
    await waitFor(() =>
      expect(screen.getByText(/Total Rejected/i)).toBeInTheDocument()
    );
  });

  it('opens notify panel when Notify button is clicked', async () => {
    vi.mocked(getOrdersByStatus).mockResolvedValue([
      { orderId: 45, orderDate: '2024-01-18T10:00:00', clientId: 8, securityId: 4,
        side: 'BUY', quantity: 2, priceType: 'MARKET', status: 'REJECTED' },
    ]);
    render(<FailedOrders />);
    await waitFor(() => screen.getByRole('button', { name: /Notify/i }));
    fireEvent.click(screen.getByRole('button', { name: /Notify/i }));
    await waitFor(() =>
      expect(screen.getByText(/Send Compliance Notification/i)).toBeInTheDocument()
    );
  });

  it('renders cancelled order row', async () => {
    vi.mocked(getOrdersByStatus).mockResolvedValue([
      { orderId: 46, orderDate: '2024-01-19T10:00:00', clientId: 9, securityId: 5,
        side: 'BUY', quantity: 1, priceType: 'MARKET', status: 'CANCELLED' },
    ]);
    render(<FailedOrders />);
    await waitFor(() =>
      expect(screen.getAllByText('CANCELLED').length).toBeGreaterThan(0)
    );
  });
});

// ─── Breaches with data loaded ────────────────────────────────────────────────

describe('Breaches with data loaded', () => {
  it('renders breach row for PENDING_KYC client', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 1, name: 'Vikram Sen', segment: 'HNI', status: 'PENDING_KYC' },
    ]);
    vi.mocked(parallelLimit)
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [] }])
      .mockResolvedValueOnce([{ status: 'fulfilled', value: null }]);
    render(<Breaches />);
    await waitFor(() =>
      expect(screen.getByText('Vikram Sen')).toBeInTheDocument()
    );
  });

  it('renders HIGH severity pill for PENDING_KYC client', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 2, name: 'Test Client', segment: 'HNI', status: 'PENDING_KYC' },
    ]);
    vi.mocked(parallelLimit)
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [] }])
      .mockResolvedValueOnce([{ status: 'fulfilled', value: null }]);
    render(<Breaches />);
    await waitFor(() =>
      expect(screen.getAllByText('HIGH').length).toBeGreaterThan(0)
    );
  });

  it('renders Acknowledge button for OPEN breach', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 3, name: 'Priya Mehta', segment: 'RETAIL', status: 'PENDING_KYC' },
    ]);
    vi.mocked(parallelLimit)
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [] }])
      .mockResolvedValueOnce([{ status: 'fulfilled', value: null }]);
    render(<Breaches />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Acknowledge/i })).toBeInTheDocument()
    );
  });

  it('renders Notify RM button for breach row', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 4, name: 'Ramesh Joshi', segment: 'HNI', status: 'PENDING_KYC' },
    ]);
    vi.mocked(parallelLimit)
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [] }])
      .mockResolvedValueOnce([{ status: 'fulfilled', value: null }]);
    render(<Breaches />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Notify RM/i })).toBeInTheDocument()
    );
  });

  it('renders MEDIUM severity breach for Active client with verified KYC but no risk profile', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 5, name: 'Sunita Rao', segment: 'RETAIL', status: 'Active' },
    ]);
    vi.mocked(parallelLimit)
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [{ kycId: 1, documentType: 'PAN', status: 'Verified' }] }])
      .mockResolvedValueOnce([{ status: 'fulfilled', value: null }]);
    render(<Breaches />);
    await waitFor(() =>
      expect(screen.getByText('Sunita Rao')).toBeInTheDocument()
    );
  });

  it('renders MEDIUM severity pill for missing risk profile breach', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 6, name: 'Anand Kumar', segment: 'HNI', status: 'Active' },
    ]);
    vi.mocked(parallelLimit)
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [{ kycId: 1, documentType: 'PAN', status: 'Verified' }] }])
      .mockResolvedValueOnce([{ status: 'fulfilled', value: null }]);
    render(<Breaches />);
    await waitFor(() =>
      expect(screen.getAllByText('MEDIUM').length).toBeGreaterThan(0)
    );
  });

  it('renders Inactive Account breach for Inactive client', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 7, name: 'Inactive Client', segment: 'RETAIL', status: 'Inactive' },
    ]);
    vi.mocked(parallelLimit)
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [] }])
      .mockResolvedValueOnce([{ status: 'fulfilled', value: { riskProfileId: 1 } }]);
    render(<Breaches />);
    await waitFor(() =>
      expect(screen.getByText('Inactive Client')).toBeInTheDocument()
    );
  });

  it('renders Analytics Compliance Breaches section heading', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([]);
    vi.mocked(parallelLimit).mockResolvedValue([]);
    render(<Breaches />);
    await waitFor(() =>
      expect(screen.getByText(/Analytics Compliance Breaches/i)).toBeInTheDocument()
    );
  });

  it('renders Load button for analytics breaches section', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([]);
    vi.mocked(parallelLimit).mockResolvedValue([]);
    render(<Breaches />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /↻ Load/i })).toBeInTheDocument()
    );
  });

  it('renders KYC Unverified breach for Active client with unverified docs', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 8, name: 'Kiran Patel', segment: 'HNI', status: 'Active' },
    ]);
    vi.mocked(parallelLimit)
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [{ kycId: 1, documentType: 'PAN', status: 'Pending' }] }])
      .mockResolvedValueOnce([{ status: 'fulfilled', value: { riskProfileId: 1 } }]);
    render(<Breaches />);
    await waitFor(() =>
      expect(screen.getByText('Kiran Patel')).toBeInTheDocument()
    );
  });

  it('renders Total KPI card showing breach count', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([
      { clientId: 9, name: 'Test', segment: 'HNI', status: 'PENDING_KYC' },
    ]);
    vi.mocked(parallelLimit)
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [] }])
      .mockResolvedValueOnce([{ status: 'fulfilled', value: null }]);
    render(<Breaches />);
    await waitFor(() =>
      expect(screen.getByText(/Total/i)).toBeInTheDocument()
    );
  });
});

// ─── AmlFlags extra tests ─────────────────────────────────────────────────────

describe('AmlFlags extra tests', () => {
  beforeEach(() => {
    useAuth.setState({ user: { username: 'comp1', role: 'COMPLIANCE', token: 'x' } });
    vi.mocked(getAllUsers).mockResolvedValue([]);
  });

  it('opens Review modal when Review button is clicked on OPEN flag', async () => {
    vi.mocked(getAllAmlFlags).mockResolvedValue([
      { amlFlagId: 10, clientId: 5, flagType: 'MANUAL', status: 'OPEN',
        description: 'Test suspicious activity', flaggedDate: '2024-01-01T00:00:00' },
    ]);
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    await waitFor(() => screen.getByRole('button', { name: /^Review$/i }));
    fireEvent.click(screen.getByRole('button', { name: /^Review$/i }));
    await waitFor(() =>
      expect(screen.getByText(/Review AML Flag 10/i)).toBeInTheDocument()
    );
  });

  it('opens Notify RM modal when Notify RM button is clicked on OPEN flag', async () => {
    vi.mocked(getAllAmlFlags).mockResolvedValue([
      { amlFlagId: 11, clientId: 6, flagType: 'SUSPICIOUS_TRANSACTION', status: 'OPEN',
        description: 'Notify test', flaggedDate: null },
    ]);
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    await waitFor(() => screen.getAllByRole('button', { name: /Notify RM/i }));
    fireEvent.click(screen.getAllByRole('button', { name: /Notify RM/i })[0]);
    await waitFor(() =>
      expect(screen.getByText(/Notify RM — Flag 11/i)).toBeInTheDocument()
    );
  });

  it('renders Delete button for CLOSED flag', async () => {
    vi.mocked(getAllAmlFlags).mockResolvedValue([
      { amlFlagId: 12, clientId: 7, flagType: 'MANUAL', status: 'CLOSED',
        description: 'Closed flag', flaggedDate: '2024-02-01T00:00:00' },
    ]);
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Delete/i })).toBeInTheDocument()
    );
  });

  it('renders CLOSED status pill', async () => {
    vi.mocked(getAllAmlFlags).mockResolvedValue([
      { amlFlagId: 13, clientId: 8, flagType: 'MANUAL', status: 'CLOSED',
        description: 'Closed', flaggedDate: '2024-03-01T00:00:00' },
    ]);
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getAllByText('CLOSED').length).toBeGreaterThan(0)
    );
  });

  it('renders HIGH VALUE TRANSFER flag type', async () => {
    vi.mocked(getAllAmlFlags).mockResolvedValue([
      { amlFlagId: 14, clientId: 9, flagType: 'HIGH_VALUE_TRANSFER', status: 'OPEN',
        description: 'Large transfer', flaggedDate: '2024-04-01T00:00:00' },
    ]);
    render(<MemoryRouter><AmlFlags /></MemoryRouter>);
    await waitFor(() =>
      expect(screen.getByText(/HIGH VALUE TRANSFER/i)).toBeInTheDocument()
    );
  });
});

// ─── RiskMonitor with data loaded ────────────────────────────────────────────

describe('RiskMonitor with data loaded', () => {
  it('renders client risk row when holdings data is available', async () => {
    vi.mocked(cachedFetch)
      .mockResolvedValueOnce([{ clientId: 1, name: 'Ravi Kumar', segment: 'HNI', status: 'Active' }])
      .mockResolvedValueOnce([{ securityId: 1, symbol: 'INFY', assetClass: 'EQUITY', currentPrice: 100 }]);
    vi.mocked(parallelLimit)
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [{ accountId: 10 }] }])
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [{ holdingId: 1, securityId: 1, quantity: 10, avgCost: 100 }] }]);
    render(<RiskMonitor />);
    await waitFor(() =>
      expect(screen.getByText('Ravi Kumar')).toBeInTheDocument()
    );
  });

  it('renders HIGH Risk KPI card after data loads', async () => {
    vi.mocked(cachedFetch)
      .mockResolvedValueOnce([{ clientId: 2, name: 'Test', segment: 'RETAIL', status: 'Active' }])
      .mockResolvedValueOnce([{ securityId: 1, symbol: 'TCS', assetClass: 'EQUITY', currentPrice: 100 }]);
    vi.mocked(parallelLimit)
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [{ accountId: 20 }] }])
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [{ holdingId: 1, securityId: 1, quantity: 100, avgCost: 100 }] }]);
    render(<RiskMonitor />);
    await waitFor(() =>
      expect(screen.getByText(/HIGH Risk/i)).toBeInTheDocument()
    );
  });

  it('renders concentration risk filter button ALL', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([]);
    vi.mocked(parallelLimit).mockResolvedValue([]);
    render(<RiskMonitor />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /^ALL$/i })).toBeInTheDocument()
    );
  });

  it('renders Clients Monitored KPI label', async () => {
    vi.mocked(cachedFetch).mockResolvedValue([]);
    vi.mocked(parallelLimit).mockResolvedValue([]);
    render(<RiskMonitor />);
    await waitFor(() =>
      expect(screen.getByText(/Clients Monitored/i)).toBeInTheDocument()
    );
  });

  it('renders concentration risk HIGH concentration for concentrated portfolio', async () => {
    vi.mocked(cachedFetch)
      .mockResolvedValueOnce([{ clientId: 3, name: 'Arjun Singh', segment: 'HNI', status: 'Active' }])
      .mockResolvedValueOnce([{ securityId: 1, symbol: 'HDFC', assetClass: 'EQUITY', currentPrice: 1000 }]);
    vi.mocked(parallelLimit)
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [{ accountId: 30 }] }])
      .mockResolvedValueOnce([{ status: 'fulfilled', value: [
        { holdingId: 1, securityId: 1, quantity: 90, avgCost: 1000 },
        { holdingId: 2, securityId: 2, quantity: 10, avgCost: 100 },
      ]}]);
    render(<RiskMonitor />);
    await waitFor(() =>
      expect(screen.getByText('Arjun Singh')).toBeInTheDocument()
    );
  });
});

// ─── SuitabilityRules extra tests ─────────────────────────────────────────────

describe('SuitabilityRules extra tests', () => {
  it('opens new rule form when New Rule is clicked', async () => {
    vi.mocked(getAllSuitabilityRules).mockResolvedValue([]);
    render(<SuitabilityRules />);
    const newRuleBtns = await waitFor(() => screen.getAllByRole('button', { name: /New Rule/i }));
    fireEvent.click(newRuleBtns[0]);
    await waitFor(() =>
      expect(screen.getByText(/New Suitability Rule/i)).toBeInTheDocument()
    );
  });

  it('renders rule status pill when rule loaded', async () => {
    vi.mocked(getAllSuitabilityRules).mockResolvedValue([
      { ruleId: 2, description: 'HNI only', expression: 'segment == HNI', status: 'ACTIVE' },
    ]);
    render(<SuitabilityRules />);
    await waitFor(() =>
      expect(screen.getAllByText(/ACTIVE/i).length).toBeGreaterThan(0)
    );
  });
});
