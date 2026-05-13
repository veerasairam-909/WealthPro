import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';

vi.mock('@/api/orders', () => ({
  getOrderLifecycle: vi.fn(),
  routeOrder: vi.fn(),
  recordFill: vi.fn(),
  allocateOrder: vi.fn(),
  cancelOrder: vi.fn(),
  runPreTradeChecks: vi.fn(),
  placeOrder: vi.fn(),
  getAllOrders: vi.fn(),
  getOrdersByStatus: vi.fn(),
}));

vi.mock('@/api/securities', () => ({
  getSecurityById: vi.fn(),
  getAllSecurities: vi.fn(),
}));

vi.mock('@/api/accounts', () => ({
  getAccountsByClientId: vi.fn(),
  getAllAccounts: vi.fn(),
  getAccountById: vi.fn(),
  createAccount: vi.fn(),
}));

vi.mock('@/lib/fetchUtils', () => ({
  cachedFetch: vi.fn(),
  parallelLimit: vi.fn(),
  invalidateCache: vi.fn(),
}));

import OrderDetail from '@/pages/dealer/OrderDetail';

import { getOrderLifecycle } from '@/api/orders';
import { getSecurityById } from '@/api/securities';
import { getAccountsByClientId } from '@/api/accounts';

function renderOrderDetail(orderId = '42') {
  return render(
    <MemoryRouter initialEntries={[`/dealer/orders/${orderId}`]}>
      <Routes>
        <Route path="/dealer/orders/:id" element={<OrderDetail />} />
      </Routes>
    </MemoryRouter>
  );
}

const sampleLifecycle = {
  order: {
    orderId: 42,
    clientId: 5,
    securityId: 1,
    side: 'BUY',
    quantity: 10,
    priceType: 'LIMIT',
    limitPrice: 100,
    status: 'PENDING',
    routedVenue: null,
    placedAt: '2024-01-01T10:00:00',
  },
  fills: [],
  allocations: [],
  preTradeChecks: [],
};

beforeEach(() => {
  vi.clearAllMocks();
  vi.mocked(getOrderLifecycle).mockResolvedValue(sampleLifecycle);
  vi.mocked(getSecurityById).mockResolvedValue({
    securityId: 1, symbol: 'RELIANCE', assetClass: 'EQUITY',
  });
  vi.mocked(getAccountsByClientId).mockResolvedValue([]);
});

// ─── OrderDetail page ─────────────────────────────────────────────────────────

describe('OrderDetail page', () => {
  it('shows loading order while fetching', () => {
    vi.mocked(getOrderLifecycle).mockReturnValue(new Promise(() => {}));
    renderOrderDetail();
    expect(screen.getByText(/Loading order/i)).toBeInTheDocument();
  });

  it('renders order heading after load', async () => {
    renderOrderDetail();
    await waitFor(() =>
      expect(screen.getByText(/Order 42/i)).toBeInTheDocument()
    );
  });

  it('renders without crashing', () => {
    expect(() => renderOrderDetail()).not.toThrow();
  });

  it('renders order status after load', async () => {
    renderOrderDetail();
    await waitFor(() =>
      expect(screen.getByText(/PENDING/i)).toBeInTheDocument()
    );
  });

  it('renders LIMIT price type after load', async () => {
    renderOrderDetail();
    await waitFor(() =>
      expect(screen.getAllByText(/LIMIT/i).length).toBeGreaterThan(0)
    );
  });
});
