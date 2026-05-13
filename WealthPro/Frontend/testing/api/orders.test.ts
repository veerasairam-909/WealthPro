import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import {
  placeOrder,
  getAllOrders,
  getOrderById,
  getOrdersByClient,
  getOrdersByStatus,
  getOrderLifecycle,
  routeOrder,
  recordFill,
  allocateOrder,
  cancelOrder,
  runPreTradeChecks,
} from '@/api/orders';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('orders API', () => {
  beforeEach(() => vi.clearAllMocks());

  // ─── placeOrder ──────────────────────────────────────────────────────────────

  it('placeOrder posts to /api/orders', async () => {
    const payload = { clientId: 5, securityId: 100, side: 'BUY', quantity: 10 };
    const created = { orderId: 1, ...payload };
    vi.mocked(api.post).mockResolvedValue({ data: created });

    const result = await placeOrder(payload);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/orders', payload);
    expect(result.orderId).toBe(1);
  });

  // ─── getAllOrders ────────────────────────────────────────────────────────────

  it('getAllOrders calls /api/orders', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getAllOrders();

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/orders');
  });

  it('getAllOrders returns list of orders', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ orderId: 1 }, { orderId: 2 }] });

    const result = await getAllOrders();

    expect(result).toHaveLength(2);
  });

  // ─── getOrderById ────────────────────────────────────────────────────────────

  it('getOrderById calls correct endpoint', async () => {
    const order = { orderId: 5, status: 'PLACED' };
    vi.mocked(api.get).mockResolvedValue({ data: order });

    const result = await getOrderById(5);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/orders/5');
    expect(result).toEqual(order);
  });

  // ─── getOrdersByClient ───────────────────────────────────────────────────────

  it('getOrdersByClient calls by-client endpoint with param', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getOrdersByClient(10);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/orders/by-client', {
      params: { clientId: 10 },
    });
  });

  it('getOrdersByClient returns matching orders', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ orderId: 3, clientId: 10 }] });

    const result = await getOrdersByClient(10);

    expect(result[0].clientId).toBe(10);
  });

  // ─── getOrdersByStatus ───────────────────────────────────────────────────────

  it('getOrdersByStatus calls by-status endpoint with param', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getOrdersByStatus('PLACED');

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/orders/by-status', {
      params: { status: 'PLACED' },
    });
  });

  it('getOrdersByStatus returns filtered orders', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ orderId: 1, status: 'PLACED' }] });

    const result = await getOrdersByStatus('PLACED');

    expect(result[0].status).toBe('PLACED');
  });

  // ─── getOrderLifecycle ───────────────────────────────────────────────────────

  it('getOrderLifecycle calls lifecycle endpoint', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { order: {}, fills: [], allocations: [] } });

    await getOrderLifecycle(7);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/orders/7/lifecycle');
  });

  // ─── routeOrder ──────────────────────────────────────────────────────────────

  it('routeOrder posts to route endpoint', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { orderId: 5, status: 'ROUTED' } });

    const result = await routeOrder(5);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/orders/5/route');
    expect(result.status).toBe('ROUTED');
  });

  // ─── recordFill ──────────────────────────────────────────────────────────────

  it('recordFill posts fill data to correct endpoint', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { fillQuantity: 10, fillPrice: 150.0 } });

    const result = await recordFill(5, 10, 150.0, 'NSE');

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/orders/5/fills', {
      orderId: 5,
      fillQuantity: 10,
      fillPrice: 150.0,
      venue: 'NSE',
    });
    expect(result.fillQuantity).toBe(10);
  });

  it('recordFill works without venue parameter', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { fillQuantity: 20 } });

    await recordFill(3, 20, 200.0);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/orders/3/fills', expect.objectContaining({
      fillQuantity: 20,
      fillPrice: 200.0,
      venue: undefined,
    }));
  });

  // ─── allocateOrder ───────────────────────────────────────────────────────────

  it('allocateOrder posts allocation data to correct endpoint', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { allocQuantity: 10, allocPrice: 150.0 } });

    const result = await allocateOrder(5, 200, 10, 150.0);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/orders/5/allocations', {
      orderId: 5,
      accountId: 200,
      allocQuantity: 10,
      allocPrice: 150.0,
    });
    expect(result.allocQuantity).toBe(10);
  });

  // ─── cancelOrder ────────────────────────────────────────────────────────────

  it('cancelOrder posts to cancel endpoint', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { orderId: 5, status: 'CANCELLED' } });

    const result = await cancelOrder(5);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/orders/5/cancel');
    expect(result.status).toBe('CANCELLED');
  });

  // ─── runPreTradeChecks ───────────────────────────────────────────────────────

  it('runPreTradeChecks posts to pre-trade-checks endpoint', async () => {
    const checks = [{ checkType: 'CREDIT_LIMIT', result: 'PASS' }];
    vi.mocked(api.post).mockResolvedValue({ data: checks });

    const result = await runPreTradeChecks(5);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/orders/5/pre-trade-checks');
    expect(result).toEqual(checks);
  });
});
