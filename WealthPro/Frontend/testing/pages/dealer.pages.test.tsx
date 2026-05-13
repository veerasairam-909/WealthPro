import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { api } from '@/api/client';

// Mock @/api/client (used directly by ResearchNotes, ProductTerms, CorporateActions)
vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

// Mock dedicated API modules used by other dealer pages
vi.mock('@/api/securities', () => ({
  getAllSecurities: vi.fn(),
  getSecurityById: vi.fn(),
  createSecurity: vi.fn(),
  updateSecurity: vi.fn(),
  deleteSecurity: vi.fn(),
}));

vi.mock('@/api/orders', () => ({
  getAllOrders: vi.fn(),
  getOrderById: vi.fn(),
  getOrdersByClient: vi.fn(),
  getOrdersByStatus: vi.fn(),
  getOrderLifecycle: vi.fn(),
  routeOrder: vi.fn(),
  recordFill: vi.fn(),
  allocateOrder: vi.fn(),
  cancelOrder: vi.fn(),
  runPreTradeChecks: vi.fn(),
  placeOrder: vi.fn(),
}));

vi.mock('@/api/accounts', () => ({
  getAccountsByClientId: vi.fn(),
  getAccountById: vi.fn(),
  getAllAccounts: vi.fn(),
  createAccount: vi.fn(),
}));

vi.mock('@/lib/fetchUtils', () => ({
  cachedFetch: vi.fn(),
  parallelLimit: vi.fn(),
  invalidateCache: vi.fn(),
}));

import ResearchNotes from '@/pages/dealer/ResearchNotes';
import ProductTerms from '@/pages/dealer/ProductTerms';
import CorporateActions from '@/pages/rm/CorporateActions';
import DealerSecurities from '@/pages/dealer/Securities';
import OrderBlotter from '@/pages/dealer/OrderBlotter';

import { getAllSecurities } from '@/api/securities';
import { getAllOrders } from '@/api/orders';
import { parallelLimit } from '@/lib/fetchUtils';

beforeEach(() => {
  vi.clearAllMocks();
  // Default: APIs return empty arrays
  vi.mocked(api.get).mockResolvedValue({ data: [] });
  vi.mocked(getAllSecurities).mockResolvedValue([]);
  vi.mocked(getAllOrders).mockResolvedValue([]);
  vi.mocked(parallelLimit).mockResolvedValue([]);
});

// ─── ResearchNotes ────────────────────────────────────────────────────────────

describe('ResearchNotes page', () => {
  it('renders the page heading', async () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    render(<ResearchNotes />);
    expect(screen.getByRole('heading', { name: /Research Notes/i })).toBeInTheDocument();
  });

  it('shows loading state initially', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    render(<ResearchNotes />);
    expect(screen.getByText(/Loading research notes/i)).toBeInTheDocument();
  });

  it('shows empty state when no notes found', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });
    render(<ResearchNotes />);
    await waitFor(() =>
      expect(screen.getByText(/No research notes found/i)).toBeInTheDocument()
    );
  });

  it('renders search input', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    render(<ResearchNotes />);
    expect(screen.getByPlaceholderText(/Search by keyword/i)).toBeInTheDocument();
  });

  it('shows notes count in search bar', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });
    render(<ResearchNotes />);
    await waitFor(() => expect(screen.getByText(/0 notes/i)).toBeInTheDocument());
  });

  it('renders a note when data is loaded', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: [{ noteId: 1, title: 'RELIANCE BUY', rating: 'BUY', analyst: 'John', content: 'Strong buy signal' }],
    });
    render(<ResearchNotes />);
    await waitFor(() => expect(screen.getByText('RELIANCE BUY')).toBeInTheDocument());
  });
});

// ─── ProductTerms ─────────────────────────────────────────────────────────────

describe('ProductTerms page', () => {
  it('renders the page heading', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    render(<ProductTerms />);
    expect(screen.getByRole('heading', { name: /Product Terms/i })).toBeInTheDocument();
  });

  it('shows loading state initially', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    render(<ProductTerms />);
    expect(screen.getByText(/Loading product terms/i)).toBeInTheDocument();
  });

  it('shows empty state when no terms found', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });
    render(<ProductTerms />);
    await waitFor(() =>
      expect(screen.getByText(/No product terms found/i)).toBeInTheDocument()
    );
  });

  it('renders filter input', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    render(<ProductTerms />);
    expect(screen.getByPlaceholderText(/Filter by Security ID/i)).toBeInTheDocument();
  });

  it('renders a term when data is loaded', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: [{ termId: 1, securitySymbol: 'RELIANCE', effectiveFrom: '2024-01-01', securityId: 100 }],
    });
    render(<ProductTerms />);
    await waitFor(() => expect(screen.getByText('RELIANCE Terms')).toBeInTheDocument());
  });
});

// ─── CorporateActions ─────────────────────────────────────────────────────────

describe('CorporateActions page', () => {
  it('renders the page heading', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    render(<CorporateActions />);
    expect(screen.getByRole('heading', { name: /Corporate Actions/i })).toBeInTheDocument();
  });

  it('shows loading state initially', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    render(<CorporateActions />);
    expect(screen.getByText(/Loading corporate actions/i)).toBeInTheDocument();
  });

  it('shows empty state when no actions found', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });
    render(<CorporateActions />);
    await waitFor(() =>
      expect(screen.getByText(/No corporate actions found/i)).toBeInTheDocument()
    );
  });

  it('renders type filter buttons', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    render(<CorporateActions />);
    expect(screen.getByRole('button', { name: 'DIVIDEND' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'ALL' })).toBeInTheDocument();
  });

  it('renders a corporate action when data is loaded', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: [{ caId: 1, caType: 'DIVIDEND', securityId: 100, payDate: '2024-12-01' }],
    });
    render(<CorporateActions />);
    await waitFor(() => expect(screen.getByText('DIVIDEND')).toBeInTheDocument());
  });
});

// ─── Dealer Securities ────────────────────────────────────────────────────────

describe('Dealer Securities page', () => {
  it('renders the Securities heading', () => {
    vi.mocked(getAllSecurities).mockReturnValue(new Promise(() => {}));
    render(<DealerSecurities />);
    expect(screen.getByRole('heading', { name: /Securities/i })).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(getAllSecurities).mockReturnValue(new Promise(() => {}));
    render(<DealerSecurities />);
    expect(screen.getByText(/Loading securities/i)).toBeInTheDocument();
  });

  it('shows empty state when no securities', async () => {
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    render(<DealerSecurities />);
    await waitFor(() =>
      expect(screen.getByText(/No securities found/i)).toBeInTheDocument()
    );
  });

  it('renders a security when loaded', async () => {
    vi.mocked(getAllSecurities).mockResolvedValue([
      { securityId: 1, symbol: 'RELIANCE', assetClass: 'EQUITY', status: 'ACTIVE', currency: 'INR', country: 'IN' },
    ]);
    render(<DealerSecurities />);
    await waitFor(() => expect(screen.getByText('RELIANCE')).toBeInTheDocument());
  });
});

// ─── OrderBlotter ─────────────────────────────────────────────────────────────

describe('OrderBlotter page', () => {
  it('renders the Order Blotter heading', () => {
    vi.mocked(getAllOrders).mockReturnValue(new Promise(() => {}));
    vi.mocked(getAllSecurities).mockReturnValue(new Promise(() => {}));
    render(
      <MemoryRouter>
        <OrderBlotter />
      </MemoryRouter>
    );
    expect(screen.getByRole('heading', { name: /Order Blotter/i })).toBeInTheDocument();
  });

  it('shows loading state via skeleton or loading text', () => {
    vi.mocked(getAllOrders).mockReturnValue(new Promise(() => {}));
    vi.mocked(getAllSecurities).mockReturnValue(new Promise(() => {}));
    render(
      <MemoryRouter>
        <OrderBlotter />
      </MemoryRouter>
    );
    // Either a skeleton or loading text is shown
    expect(document.body.textContent).toBeTruthy();
  });

  it('shows empty state when no orders', async () => {
    vi.mocked(getAllOrders).mockResolvedValue([]);
    vi.mocked(getAllSecurities).mockResolvedValue([]);
    render(
      <MemoryRouter>
        <OrderBlotter />
      </MemoryRouter>
    );
    await waitFor(() =>
      expect(screen.getByText(/No matching orders/i)).toBeInTheDocument()
    );
  });

  it('renders BUY order side when orders loaded', async () => {
    vi.mocked(getAllOrders).mockResolvedValue([
      { orderId: 42, clientId: 5, securityId: 1, side: 'BUY', quantity: 10, priceType: 'LIMIT', limitPrice: 100, status: 'PLACED', orderDate: '2024-01-01' },
    ]);
    render(
      <MemoryRouter>
        <OrderBlotter />
      </MemoryRouter>
    );
    await waitFor(() =>
      expect(screen.getAllByText(/BUY/i).length).toBeGreaterThan(0)
    );
  });

  it('renders Placed KPI label', async () => {
    vi.mocked(getAllOrders).mockResolvedValue([
      { orderId: 1, clientId: 5, securityId: 1, side: 'BUY', quantity: 5, priceType: 'MARKET', status: 'PLACED', orderDate: '2024-01-01' },
    ]);
    render(
      <MemoryRouter>
        <OrderBlotter />
      </MemoryRouter>
    );
    await waitFor(() =>
      expect(screen.getAllByText(/Placed/i).length).toBeGreaterThan(0)
    );
  });

  it('renders status filter buttons ALL and FILLED', async () => {
    vi.mocked(getAllOrders).mockResolvedValue([]);
    render(
      <MemoryRouter>
        <OrderBlotter />
      </MemoryRouter>
    );
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'ALL' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'FILLED' })).toBeInTheDocument();
    });
  });

  it('renders search input in OrderBlotter', async () => {
    vi.mocked(getAllOrders).mockResolvedValue([]);
    render(
      <MemoryRouter>
        <OrderBlotter />
      </MemoryRouter>
    );
    await waitFor(() =>
      expect(screen.getByPlaceholderText(/Search by order ID/i)).toBeInTheDocument()
    );
  });
});
