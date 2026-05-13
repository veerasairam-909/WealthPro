import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { useAuth } from '@/auth/store';

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

vi.mock('@/api/recommendations', () => ({
  getRecommendationsByClientId: vi.fn(),
  getAllModelPortfolios: vi.fn(),
  createRecommendation: vi.fn(),
  updateRecommendationStatus: vi.fn(),
  deleteRecommendation: vi.fn(),
}));

vi.mock('@/api/accounts', () => ({
  getAccountsByClientId: vi.fn(),
  getAllAccounts: vi.fn(),
  getAccountById: vi.fn(),
  createAccount: vi.fn(),
}));

vi.mock('@/api/cashLedger', () => ({
  getCashLedgerByAccountId: vi.fn(),
  getBalanceByAccountId: vi.fn(),
  createCashLedgerEntry: vi.fn(),
}));

vi.mock('@/api/notifications', () => ({
  createNotification: vi.fn(),
  getNotificationsByUserId: vi.fn(),
}));

vi.mock('@/lib/fetchUtils', () => ({
  cachedFetch: vi.fn(),
  parallelLimit: vi.fn(),
  invalidateCache: vi.fn(),
}));

import ClientDetail from '@/pages/rm/ClientDetail';

import { getClientById } from '@/api/clients';
import { getGoalsByClientId } from '@/api/goals';
import { getRecommendationsByClientId } from '@/api/recommendations';
import { getAccountsByClientId } from '@/api/accounts';
import { getBalanceByAccountId, getCashLedgerByAccountId } from '@/api/cashLedger';
import { getRiskProfile } from '@/api/clients';
import { getKycDocs } from '@/api/clients';
import { getAllModelPortfolios } from '@/api/recommendations';

const rmUser = { username: 'rm1', role: 'RM' as const, userId: 5, token: 'x' };

function renderClientDetail(clientId = '1') {
  return render(
    <MemoryRouter initialEntries={[`/rm/clients/${clientId}`]}>
      <Routes>
        <Route path="/rm/clients/:id" element={<ClientDetail />} />
      </Routes>
    </MemoryRouter>
  );
}

beforeEach(() => {
  vi.clearAllMocks();
  useAuth.setState({ user: rmUser });
  vi.mocked(getClientById).mockResolvedValue({
    clientId: 1,
    name: 'Jane Smith',
    firstName: 'Jane',
    lastName: 'Smith',
    segment: 'HNI',
    status: 'Active',
  });
  vi.mocked(getKycDocs).mockResolvedValue([]);
  vi.mocked(getRiskProfile).mockResolvedValue(null);
  vi.mocked(getGoalsByClientId).mockResolvedValue([]);
  vi.mocked(getRecommendationsByClientId).mockResolvedValue([]);
  vi.mocked(getAccountsByClientId).mockResolvedValue([]);
  vi.mocked(getBalanceByAccountId).mockResolvedValue(0);
  vi.mocked(getCashLedgerByAccountId).mockResolvedValue([]);
  vi.mocked(getAllModelPortfolios).mockResolvedValue([]);
});

// ─── ClientDetail page ────────────────────────────────────────────────────────

describe('ClientDetail page', () => {
  it('shows loading client while fetching', () => {
    vi.mocked(getClientById).mockReturnValue(new Promise(() => {}));
    renderClientDetail();
    expect(screen.getByText(/Loading client/i)).toBeInTheDocument();
  });

  it('renders client name after load', async () => {
    renderClientDetail();
    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /Jane Smith/i })).toBeInTheDocument()
    );
  });

  it('renders Profile tab by default', async () => {
    renderClientDetail();
    await waitFor(() =>
      expect(screen.getAllByRole('button', { name: /Profile/i }).length).toBeGreaterThan(0)
    );
  });

  it('renders tab navigation buttons', async () => {
    renderClientDetail();
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /KYC/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Goals/i })).toBeInTheDocument();
    });
  });

  it('renders without crashing', () => {
    expect(() => renderClientDetail()).not.toThrow();
  });

  it('shows client segment and status', async () => {
    renderClientDetail();
    await waitFor(() =>
      expect(screen.getAllByText(/HNI/i).length).toBeGreaterThan(0)
    );
  });

  it('switches to KYC tab when KYC button is clicked', async () => {
    renderClientDetail();
    await waitFor(() => screen.getByRole('button', { name: /^KYC$/ }));
    fireEvent.click(screen.getByRole('button', { name: /^KYC$/ }));
    await waitFor(() =>
      expect(screen.getByText(/Upload KYC document/i)).toBeInTheDocument()
    );
  });

  it('switches to Goals tab and shows no goals', async () => {
    renderClientDetail();
    await waitFor(() => screen.getByRole('button', { name: /^Goals$/ }));
    fireEvent.click(screen.getByRole('button', { name: /^Goals$/ }));
    await waitFor(() =>
      expect(screen.getByText(/No goals set yet/i)).toBeInTheDocument()
    );
  });

  it('switches to Risk Profile tab', async () => {
    renderClientDetail();
    await waitFor(() => screen.getByRole('button', { name: /Risk Profile/i }));
    fireEvent.click(screen.getByRole('button', { name: /Risk Profile/i }));
    await waitFor(() =>
      expect(screen.getByText(/Risk Profile/i)).toBeInTheDocument()
    );
  });

  it('switches to Recommendations tab and shows no recommendations', async () => {
    renderClientDetail();
    await waitFor(() => screen.getByRole('button', { name: /^Recommendations$/ }));
    fireEvent.click(screen.getByRole('button', { name: /^Recommendations$/ }));
    await waitFor(() =>
      expect(screen.getByText(/No recommendations sent yet/i)).toBeInTheDocument()
    );
  });

  it('switches to Account & Funds tab', async () => {
    renderClientDetail();
    await waitFor(() => screen.getByRole('button', { name: /Account & Funds/i }));
    fireEvent.click(screen.getByRole('button', { name: /Account & Funds/i }));
    // account & funds tab renders "No investment account" or "Add Funds" section
    await waitFor(() =>
      expect(screen.getAllByText(/Account/i).length).toBeGreaterThan(0)
    );
  });

  it('shows client-not-found state when getClientById returns null', async () => {
    vi.mocked(getClientById).mockResolvedValue(null);
    renderClientDetail();
    await waitFor(() =>
      expect(screen.getByText(/Client not found/i)).toBeInTheDocument()
    );
  });

  it('shows Profile information panel in Profile tab', async () => {
    renderClientDetail();
    await waitFor(() =>
      expect(screen.getByText(/Profile information/i)).toBeInTheDocument()
    );
  });

  it('shows Edit button in Profile tab for RM', async () => {
    renderClientDetail();
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /^Edit$/i })).toBeInTheDocument()
    );
  });

  it('Account & Funds tab shows No PBOR account found when no account', async () => {
    renderClientDetail();
    await waitFor(() => screen.getByRole('button', { name: /Account & Funds/i }));
    fireEvent.click(screen.getByRole('button', { name: /Account & Funds/i }));
    await waitFor(() =>
      expect(screen.getByText(/No PBOR account found/i)).toBeInTheDocument()
    );
  });

  it('Account & Funds tab shows Create Portfolio Account button', async () => {
    renderClientDetail();
    await waitFor(() => screen.getByRole('button', { name: /Account & Funds/i }));
    fireEvent.click(screen.getByRole('button', { name: /Account & Funds/i }));
    await waitFor(() =>
      expect(screen.getByRole('button', { name: /Create Portfolio Account/i })).toBeInTheDocument()
    );
  });

  it('Risk Profile tab shows questionnaire when no risk profile', async () => {
    renderClientDetail();
    await waitFor(() => screen.getByRole('button', { name: /Risk Profile/i }));
    fireEvent.click(screen.getByRole('button', { name: /Risk Profile/i }));
    await waitFor(() =>
      expect(screen.getByText(/investment time horizon/i)).toBeInTheDocument()
    );
  });

  it('Risk Profile tab shows risk class when risk profile exists', async () => {
    vi.mocked(getRiskProfile).mockResolvedValue({
      riskProfileId: 1,
      riskClass: 'MODERATE',
      riskScore: 50,
      assessedDate: '2024-01-01',
    });
    renderClientDetail();
    await waitFor(() => screen.getByRole('button', { name: /Risk Profile/i }));
    fireEvent.click(screen.getByRole('button', { name: /Risk Profile/i }));
    await waitFor(() =>
      expect(screen.getByText(/MODERATE/i)).toBeInTheDocument()
    );
  });

  it('Goals tab renders goal card when goals are present', async () => {
    vi.mocked(getGoalsByClientId).mockResolvedValue([
      { goalId: 1, goalType: 'RETIREMENT', status: 'IN_PROGRESS', priority: 1,
        targetAmount: 1000000, targetDate: '2030-01-01' },
    ]);
    renderClientDetail();
    await waitFor(() => screen.getByRole('button', { name: /^Goals$/ }));
    fireEvent.click(screen.getByRole('button', { name: /^Goals$/ }));
    await waitFor(() =>
      expect(screen.getByText(/RETIREMENT/i)).toBeInTheDocument()
    );
  });

  it('Profile tab shows "All clients" back link', async () => {
    renderClientDetail();
    await waitFor(() =>
      expect(screen.getByText(/All clients/i)).toBeInTheDocument()
    );
  });

  it('KYC tab shows KYC document when kycDocs are loaded', async () => {
    vi.mocked(getKycDocs).mockResolvedValue([
      { kycId: 1, documentType: 'PAN', status: 'Verified', verifiedDate: '2024-01-01' },
    ]);
    renderClientDetail();
    await waitFor(() => screen.getByRole('button', { name: /^KYC$/ }));
    fireEvent.click(screen.getByRole('button', { name: /^KYC$/ }));
    await waitFor(() =>
      expect(screen.getAllByText(/PAN/i).length).toBeGreaterThan(0)
    );
  });
});
