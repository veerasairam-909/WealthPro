import { api } from './client';

// ─── Orders ───
export async function placeOrder(data: any) {
  const res = await api.post('/api/orders', data);
  return res.data;
}

export async function getAllOrders() {
  const res = await api.get('/api/orders');
  return res.data;
}

export async function getOrderById(orderId: number) {
  const res = await api.get('/api/orders/' + orderId);
  return res.data;
}

export async function getOrdersByClient(clientId: number) {
  const res = await api.get('/api/orders/by-client', { params: { clientId } });
  return res.data;
}

export async function getOrdersByStatus(status: string) {
  const res = await api.get('/api/orders/by-status', { params: { status } });
  return res.data;
}

export async function getOrderLifecycle(orderId: number) {
  const res = await api.get('/api/orders/' + orderId + '/lifecycle');
  return res.data;
}

// ─── Dealer actions ───
// backend chooses the venue automatically - just hit the route endpoint
export async function routeOrder(orderId: number) {
  const res = await api.post('/api/orders/' + orderId + '/route');
  return res.data;
}

export async function recordFill(orderId: number, fillQuantity: number, fillPrice: number, venue?: string) {
  const res = await api.post('/api/orders/' + orderId + '/fills', {
    orderId: orderId,    // backend requires this in body
    fillQuantity,
    fillPrice,
    venue,
  });
  return res.data;
}

export async function allocateOrder(orderId: number, accountId: number, allocQuantity: number, allocPrice: number) {
  const res = await api.post('/api/orders/' + orderId + '/allocations', {
    orderId,
    accountId,
    allocQuantity,
    allocPrice,
  });
  return res.data;
}

export async function cancelOrder(orderId: number) {
  const res = await api.post('/api/orders/' + orderId + '/cancel');
  return res.data;
}

export async function runPreTradeChecks(orderId: number) {
  const res = await api.post('/api/orders/' + orderId + '/pre-trade-checks');
  return res.data;
}
