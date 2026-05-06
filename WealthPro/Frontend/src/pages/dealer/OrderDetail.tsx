import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  getOrderLifecycle, routeOrder, recordFill, allocateOrder,
  cancelOrder, runPreTradeChecks,
} from '@/api/orders';
import { getSecurityById } from '@/api/securities';
import { getAccountsByClientId, createAccount } from '@/api/accounts';

export default function OrderDetail() {
  const { id } = useParams();
  const orderId = Number(id);

  const [lifecycle, setLifecycle] = useState<any>(null);
  const [security, setSecurity] = useState<any>(null);
  const [accounts, setAccounts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionError, setActionError] = useState('');
  const [actionMsg, setActionMsg] = useState('');

  // route action state
  const [routing, setRouting] = useState(false);

  // fill form state
  const [fillQty, setFillQty] = useState('');
  const [fillPrice, setFillPrice] = useState('');
  const [filling, setFilling] = useState(false);

  // allocate form state
  const [allocAccountId, setAllocAccountId] = useState<number | null>(null);
  const [allocQty, setAllocQty] = useState('');
  const [allocPrice, setAllocPrice] = useState('');
  const [allocating, setAllocating] = useState(false);

  // cancel state
  const [cancelling, setCancelling] = useState(false);

  // pre-trade state
  const [checkingPretrade, setCheckingPretrade] = useState(false);

  // create account state (when client has no PBOR account yet)
  const [creatingAccount, setCreatingAccount] = useState(false);

  useEffect(() => {
    if (orderId) loadAll();
  }, [orderId]);

  // Fix 1 & 4 — Pre-fill qty for ALL orders (BUY and SELL), not just SELL.
  // Fill qty  = remaining units the client hasn't had filled yet.
  // Alloc qty = filled units that haven't been allocated to PBOR yet.
  // Alloc price is taken from the last completed fill so the dealer doesn't
  // have to re-type it.
  useEffect(() => {
    if (!lifecycle?.order) return;
    const o = lifecycle.order;

    const completedFills = (lifecycle.executionFills || []).filter((f: any) => f.status === 'COMPLETED');
    const filledSoFar = completedFills.reduce((s: number, f: any) => s + (f.fillQuantity || 0), 0);
    const allocatedSoFar = (lifecycle.allocations || []).reduce((s: number, a: any) => s + (a.allocQuantity || 0), 0);

    const remFill  = o.quantity - filledSoFar;
    const remAlloc = filledSoFar - allocatedSoFar;

    if (remFill > 0)  setFillQty(String(remFill));
    if (remAlloc > 0) {
      setAllocQty(String(remAlloc));
      // Pre-fill alloc price from the last completed fill price
      const lastFill = completedFills[completedFills.length - 1];
      if (lastFill?.fillPrice) setAllocPrice(String(lastFill.fillPrice));
    }
  }, [lifecycle]);

  // Fix 2 — Pre-fill fill price from the security's current market price.
  // Only applied when the order is in a fillable state and the dealer
  // hasn't typed anything yet (prev is empty).
  useEffect(() => {
    if (!security?.currentPrice) return;
    if (!lifecycle?.order) return;
    const { status } = lifecycle.order;
    if (status !== 'ROUTED' && status !== 'PARTIALLY_FILLED') return;
    setFillPrice((prev) => prev || String(security.currentPrice));
  }, [security, lifecycle]);

  async function loadAll() {
    setLoading(true);
    try {
      const data = await getOrderLifecycle(orderId);
      setLifecycle(data);

      // fetch security info
      if (data.order && data.order.securityId) {
        try {
          const sec = await getSecurityById(data.order.securityId);
          setSecurity(sec);
        } catch (e) {
          setSecurity(null);
        }
      }

      // fetch accounts of the client (for allocation form)
      if (data.order && data.order.clientId) {
        try {
          const accs = await getAccountsByClientId(data.order.clientId);
          setAccounts(accs);
          if (accs.length > 0) {
            setAllocAccountId(accs[0].accountId);
          }
        } catch (e) {
          setAccounts([]);
        }
      }
    } catch (e) {
    }
    setLoading(false);
  }

  async function handleRunPretrade() {
    setActionError('');
    setActionMsg('');
    setCheckingPretrade(true);
    try {
      await runPreTradeChecks(orderId);
      setActionMsg('Pre-trade checks completed.');
      loadAll();
    } catch (e: any) {
      setActionError(e.response?.data?.message || 'Pre-trade check failed');
    }
    setCheckingPretrade(false);
  }

  async function handleRoute() {
    setActionError('');
    setActionMsg('');
    setRouting(true);
    try {
      const result = await routeOrder(orderId);
      setActionMsg('Order routed to ' + (result.routedVenue || 'venue'));
      loadAll();
    } catch (e: any) {
      setActionError(e.response?.data?.message || 'Routing failed');
    }
    setRouting(false);
  }

  async function handleFill(e: React.FormEvent) {
    e.preventDefault();
    setActionError('');
    setActionMsg('');
    const qty = parseInt(fillQty, 10);
    const price = parseFloat(fillPrice);
    if (fillQty.trim() === '' || isNaN(qty) || qty <= 0) {
      setActionError('Fill quantity must be a positive whole number');
      return;
    }
    if (qty > remainingToFill) {
      setActionError(`Fill quantity cannot exceed remaining quantity (${remainingToFill})`);
      return;
    }
    if (fillPrice.trim() === '' || isNaN(price) || price <= 0) {
      setActionError('Fill price must be greater than zero');
      return;
    }
    if (price > 1_000_000) {
      setActionError('Fill price cannot exceed ₹10,00,000');
      return;
    }
    if (!/^\d+(\.\d{1,2})?$/.test(fillPrice.trim())) {
      setActionError('Fill price can have at most 2 decimal places');
      return;
    }
    setFilling(true);
    try {
      // venue is optional; backend uses the order's routedVenue
      await recordFill(orderId, qty, price, lifecycle.order.routedVenue || undefined);
      setActionMsg('Fill recorded');
      setFillQty('');
      setFillPrice('');
      loadAll();
    } catch (e: any) {
      setActionError(e.response?.data?.message || 'Fill failed');
    }
    setFilling(false);
  }

  async function handleAllocate(e: React.FormEvent) {
    e.preventDefault();
    setActionError('');
    setActionMsg('');
    const qty = parseInt(allocQty, 10);
    const price = parseFloat(allocPrice);
    if (!allocAccountId) {
      setActionError('Select an account to allocate to');
      return;
    }
    if (allocQty.trim() === '' || isNaN(qty) || qty <= 0) {
      setActionError('Allocation quantity must be a positive whole number');
      return;
    }
    if (qty > remainingToAllocate) {
      setActionError(`Allocation quantity cannot exceed remaining unallocated quantity (${remainingToAllocate})`);
      return;
    }
    if (allocPrice.trim() === '' || isNaN(price) || price <= 0) {
      setActionError('Allocation price must be greater than zero');
      return;
    }
    if (price > 1_000_000) {
      setActionError('Allocation price cannot exceed ₹10,00,000');
      return;
    }
    if (!/^\d+(\.\d{1,2})?$/.test(allocPrice.trim())) {
      setActionError('Allocation price can have at most 2 decimal places');
      return;
    }
    setAllocating(true);
    try {
      await allocateOrder(orderId, allocAccountId, qty, price);
      setActionMsg('Allocated to account ' + allocAccountId);
      setAllocQty('');
      setAllocPrice('');
      loadAll();
    } catch (e: any) {
      setActionError(e.response?.data?.message || 'Allocation failed');
    }
    setAllocating(false);
  }

  async function handleCreateAccount() {
    if (!lifecycle?.order?.clientId) return;
    setActionError('');
    setActionMsg('');
    setCreatingAccount(true);
    try {
      await createAccount({
        clientId: lifecycle.order.clientId,
        accountType: 'INDIVIDUAL',
        baseCurrency: 'INR',
        status: 'ACTIVE',
      });
      setActionMsg('Investment account created for client ' + lifecycle.order.clientId);
      loadAll();
    } catch (e: any) {
      setActionError(e.response?.data?.message || 'Failed to create account');
    }
    setCreatingAccount(false);
  }

  async function handleCancel() {
    if (!confirm('Cancel this order?')) return;
    setActionError('');
    setActionMsg('');
    setCancelling(true);
    try {
      await cancelOrder(orderId);
      setActionMsg('Order cancelled');
      loadAll();
    } catch (e: any) {
      setActionError(e.response?.data?.message || 'Cancel failed');
    }
    setCancelling(false);
  }

  if (loading) return <div className="p-10 text-center text-text-2">Loading order...</div>;
  if (!lifecycle || !lifecycle.order) {
    return (
      <div>
        <p className="mb-3">Order not found</p>
        <Link to="/dealer/orders" className="btn btn-ghost btn-sm">← Back to blotter</Link>
      </div>
    );
  }

  const order = lifecycle.order;
  const fills = lifecycle.executionFills || [];
  const allocs = lifecycle.allocations || [];
  const checks = lifecycle.preTradeChecks || [];

  // calculate totals
  const totalFilled = fills
    .filter((f: any) => f.status === 'COMPLETED')
    .reduce((s: number, f: any) => s + (f.fillQuantity || 0), 0);
  const totalAlloc = allocs.reduce((s: number, a: any) => s + (a.allocQuantity || 0), 0);
  const remainingToFill = order.quantity - totalFilled;
  const remainingToAllocate = totalFilled - totalAlloc;

  // figure out what actions are available
  const canCancel = ['PLACED', 'VALIDATED', 'ROUTED'].indexOf(order.status) !== -1;
  const canRoute = order.status === 'VALIDATED' || order.status === 'PLACED';
  const canFill = order.status === 'ROUTED' || order.status === 'PARTIALLY_FILLED';
  const canAllocate = (order.status === 'FILLED' || order.status === 'PARTIALLY_FILLED') && remainingToAllocate > 0;
  const canPretrade = order.status === 'PLACED';

  function getStatusPill(s: string) {
    if (s === 'FILLED' || s === 'PARTIALLY_FILLED' || s === 'PASS' || s === 'COMPLETED') return 'pill-success';
    if (s === 'CANCELLED' || s === 'REJECTED' || s === 'FAIL') return 'pill-danger';
    if (s === 'ROUTED' || s === 'PENDING') return 'pill-warn';
    return 'pill-info';
  }

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <Link to="/dealer/orders" className="text-sm text-text-2 inline-block mb-1">← Order blotter</Link>
          <h1 className="text-2xl font-semibold">
            Order {order.orderId}
            <span className={'pill ml-3 ' + getStatusPill(order.status)}>{order.status}</span>
          </h1>
          <p className="text-sm text-text-2">
            {security ? security.symbol : order.securityId} · {order.side} · qty {order.quantity}
          </p>
        </div>
        {canCancel && (
          <button onClick={handleCancel} disabled={cancelling} className="btn btn-danger btn-sm">
            {cancelling ? 'Cancelling...' : 'Cancel order'}
          </button>
        )}
      </div>

      {/* notification messages */}
      {actionError && (
        <div className="pill pill-danger w-full block mb-3 py-2">{actionError}</div>
      )}
      {actionMsg && (
        <div className="pill pill-success w-full block mb-3 py-2">{actionMsg}</div>
      )}

      {/* order summary panel */}
      <div className="panel mb-4">
        <div className="panel-h"><h3>Order details</h3></div>
        <div className="panel-b">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-x-6 gap-y-3">
            <Field label="Client ID" value={order.clientId} />
            <Field label="Security" value={security ? security.symbol : order.securityId} />
            <Field label="Asset class" value={security ? security.assetClass : '-'} />
            <Field label="Side" value={order.side} />
            <Field label="Quantity" value={order.quantity} />
            <Field label="Price type" value={order.priceType} />
            <Field label="Limit price" value={order.limitPrice ? '₹' + order.limitPrice : '-'} />
            {/* Fix 3 — Show total order value so dealer can instantly see the ₹ amount */}
            <Field
              label="Order value (est.)"
              value={
                '₹' +
                (
                  order.quantity *
                  (order.limitPrice ?? security?.currentPrice ?? 0)
                ).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
              }
            />
            <Field label="Market price" value={security?.currentPrice ? '₹' + security.currentPrice : '-'} />
            <Field label="Venue" value={order.routedVenue || '-'} />
            <Field label="Order date" value={(order.orderDate || '').replace('T', ' ').slice(0, 19)} />
            <Field label="Filled" value={totalFilled + ' / ' + order.quantity} />
            <Field label="Remaining to fill" value={remainingToFill} />
            <Field label="Allocated" value={totalAlloc + ' / ' + totalFilled} />
          </div>
        </div>
      </div>

      {/* dealer action panels */}
      {canPretrade && (
        <div className="panel mb-4">
          <div className="panel-h"><h3>Step 1: Pre-trade checks</h3></div>
          <div className="panel-b">
            <p className="text-sm text-text-2 mb-3">
              Run suitability, cash, and exposure checks before routing.
            </p>
            <button onClick={handleRunPretrade} disabled={checkingPretrade} className="btn btn-primary btn-sm">
              {checkingPretrade ? 'Running checks...' : 'Run pre-trade checks'}
            </button>
          </div>
        </div>
      )}

      {canRoute && (
        <div className="panel mb-4">
          <div className="panel-h"><h3>Step 2: Route to venue</h3></div>
          <div className="panel-b">
            <p className="text-sm text-text-2 mb-3">
              The order will be routed to a simulated exchange (NSE or BSE)
              based on the asset class.
            </p>
            <button onClick={handleRoute} disabled={routing} className="btn btn-primary btn-sm">
              {routing ? 'Routing...' : 'Route order →'}
            </button>
          </div>
        </div>
      )}

      {canFill && (
        <div className="panel mb-4">
          <div className="panel-h"><h3>Step 3: Record fill</h3></div>
          <form onSubmit={handleFill} className="panel-b">
            <p className="text-sm text-text-2 mb-3">
              {remainingToFill} units remaining out of {order.quantity}.
              <span className="ml-2 text-xs text-primary font-medium">
                ⓘ Quantity pre-filled from client's order.
                {security?.currentPrice
                  ? ` Fill price pre-filled from current market price (₹${security.currentPrice}).`
                  : ' Enter the actual execution price.'}
              </span>
            </p>
            <div className="grid grid-cols-2 gap-3 mb-3">
              <div>
                <label className="label block mb-1">Fill quantity</label>
                <input
                  className="input mono"
                  type="number"
                  min="1"
                  max={remainingToFill}
                  placeholder={'max ' + remainingToFill}
                  value={fillQty}
                  onChange={(e) => setFillQty(e.target.value)}
                />
              </div>
              <div>
                <label className="label block mb-1">Fill price (₹)</label>
                <input
                  className="input mono"
                  type="number"
                  step="0.05"
                  min="0.01"
                  placeholder="e.g. 1648.50"
                  value={fillPrice}
                  onChange={(e) => setFillPrice(e.target.value)}
                />
              </div>
            </div>
            <button type="submit" disabled={filling} className="btn btn-success btn-sm">
              {filling ? 'Recording...' : 'Record fill'}
            </button>
          </form>
        </div>
      )}

      {canAllocate && (
        <div className="panel mb-4">
          <div className="panel-h"><h3>Step 4: Allocate to client account</h3></div>
          <form onSubmit={handleAllocate} className="panel-b">
            <p className="text-sm text-text-2 mb-3">
              {remainingToAllocate} of {totalFilled} filled units remaining to allocate.
              <span className="ml-2 text-xs text-primary font-medium">
                ⓘ Quantity and price pre-filled from the recorded fill.
              </span>
            </p>
            {accounts.length === 0 ? (
              <div className="flex items-center justify-between bg-amber-50 border border-amber-200 rounded-lg px-4 py-3">
                <div>
                  <p className="text-sm font-medium text-amber-800">No investment account found</p>
                  <p className="text-xs text-amber-600 mt-0.5">
                    Client {order.clientId} has no PBOR account yet. Create one to proceed.
                  </p>
                </div>
                <button
                  onClick={handleCreateAccount}
                  disabled={creatingAccount}
                  className="btn btn-primary btn-sm ml-4 shrink-0"
                >
                  {creatingAccount ? 'Creating...' : '+ Create account'}
                </button>
              </div>
            ) : (
              <>
                <div className="grid grid-cols-3 gap-3 mb-3">
                  <div>
                    <label className="label block mb-1">Account</label>
                    <select
                      className="input"
                      value={allocAccountId ?? ''}
                      onChange={(e) => setAllocAccountId(Number(e.target.value))}
                    >
                      {accounts.map((a) => (
                        <option key={a.accountId} value={a.accountId}>
                          {a.accountId} ({a.accountType})
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="label block mb-1">Quantity</label>
                    <input
                      className="input mono"
                      type="number"
                      min="1"
                      max={remainingToAllocate}
                      placeholder={'max ' + remainingToAllocate}
                      value={allocQty}
                      onChange={(e) => setAllocQty(e.target.value)}
                    />
                  </div>
                  <div>
                    <label className="label block mb-1">Price (₹)</label>
                    <input
                      className="input mono"
                      type="number"
                      step="0.05"
                      min="0.01"
                      placeholder="e.g. 1648.50"
                      value={allocPrice}
                      onChange={(e) => setAllocPrice(e.target.value)}
                    />
                  </div>
                </div>
                <button type="submit" disabled={allocating} className="btn btn-success btn-sm">
                  {allocating ? 'Allocating...' : 'Allocate'}
                </button>
              </>
            )}
          </form>
        </div>
      )}

      {/* History panels */}
      <div className="grid md:grid-cols-3 gap-4">
        <div className="panel">
          <div className="panel-h"><h3>Pre-trade checks ({checks.length})</h3></div>
          {checks.length === 0 ? (
            <div className="panel-b text-center text-text-2 text-sm py-6">No checks yet</div>
          ) : (
            <div className="panel-b text-sm space-y-3">
              {checks.map((c: any) => (
                <div key={c.checkId} className="border-b border-border-hairline pb-2">
                  <div className="flex justify-between items-center mb-1">
                    <span className="font-medium text-text">{c.checkType}</span>
                    <span className={'pill text-xs ' + getStatusPill(c.result)}>{c.result}</span>
                  </div>
                  {c.message && (
                    <p className={
                      'text-xs leading-relaxed ' +
                      (c.result === 'FAIL' ? 'text-danger' : 'text-text-2')
                    }>
                      {c.result === 'FAIL' ? '⛔ ' : '✓ '}{c.message}
                    </p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="panel">
          <div className="panel-h"><h3>Fills ({fills.length})</h3></div>
          {fills.length === 0 ? (
            <div className="panel-b text-center text-text-2 text-sm py-6">No fills yet</div>
          ) : (
            <div className="panel-b text-sm space-y-2">
              {fills.map((f: any) => (
                <div key={f.fillId} className="border-b border-border-hairline pb-1.5">
                  <div className="flex justify-between">
                    <span className="mono text-xs text-text-2">Fill {f.fillId}</span>
                    <span className={'pill text-xs ' + getStatusPill(f.status)}>{f.status}</span>
                  </div>
                  <div className="text-xs text-text-2 mt-1">
                    Qty {f.fillQuantity} @ ₹{f.fillPrice}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="panel">
          <div className="panel-h"><h3>Allocations ({allocs.length})</h3></div>
          {allocs.length === 0 ? (
            <div className="panel-b text-center text-text-2 text-sm py-6">No allocations yet</div>
          ) : (
            <div className="panel-b text-sm space-y-2">
              {allocs.map((a: any) => (
                <div key={a.allocationId} className="border-b border-border-hairline pb-1.5">
                  <div className="flex justify-between">
                    <span className="mono text-xs text-text-2">Alloc {a.allocationId}</span>
                    <span className="text-xs">→ acc {a.accountId}</span>
                  </div>
                  <div className="text-xs text-text-2 mt-1">
                    Qty {a.allocQuantity} @ ₹{a.allocPrice}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function Field({ label, value }: { label: string; value: any }) {
  return (
    <div>
      <p className="label">{label}</p>
      <p className="font-medium mono">{value || '—'}</p>
    </div>
  );
}
