import { useState, useEffect } from 'react';
import { getAllSecurities } from '@/api/securities';
import { placeOrder } from '@/api/orders';
import { useAuth } from '@/auth/store';

const ASSET_CLASSES = ['ALL', 'EQUITY', 'MUTUAL_FUND', 'ETF', 'BOND', 'STRUCTURED'];

export default function Products() {
  const user = useAuth((s) => s.user);
  const clientId = user?.clientId;

  const [securities, setSecurities] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [assetFilter, setAssetFilter] = useState('ALL');

  // place order modal state
  const [orderSecurity, setOrderSecurity] = useState<any>(null);
  const [orderQty, setOrderQty] = useState('');
  const [orderType, setOrderType] = useState('MARKET');
  const [orderPrice, setOrderPrice] = useState('');
  const [orderError, setOrderError] = useState('');
  const [orderSubmitting, setOrderSubmitting] = useState(false);
  const [orderSuccess, setOrderSuccess] = useState('');

  useEffect(() => {
    loadSecurities();
  }, []);

  const [serviceDown, setServiceDown] = useState(false);

  async function loadSecurities() {
    setLoading(true);
    setServiceDown(false);
    try {
      const data = await getAllSecurities();
      // gateway sometimes returns a fallback string when the service is down
      if (!Array.isArray(data)) {
        setSecurities([]);
        setServiceDown(true);
      } else {
        setSecurities(data);
      }
    } catch (e) {
      setServiceDown(true);
    }
    setLoading(false);
  }

  // filter securities
  const filtered = securities.filter((s) => {
    if (s.status !== 'ACTIVE') return false;
    if (assetFilter !== 'ALL' && s.assetClass !== assetFilter) return false;
    if (search) {
      const q = search.toLowerCase();
      if (!s.symbol.toLowerCase().includes(q)) return false;
    }
    return true;
  });

  function openBuyModal(sec: any) {
    setOrderSecurity(sec);
    setOrderQty('');
    setOrderType('MARKET');
    setOrderPrice('');
    setOrderError('');
    setOrderSuccess('');
  }

  function closeModal() {
    setOrderSecurity(null);
    setOrderError('');
    setOrderSuccess('');
  }

  async function submitOrder(e: React.FormEvent) {
    e.preventDefault();
    if (!clientId || !orderSecurity) return;

    const qty = parseInt(orderQty, 10);
    if (orderQty.trim() === '' || isNaN(qty) || qty <= 0) {
      setOrderError('Quantity must be a positive whole number');
      return;
    }
    if (qty > 100_000) {
      setOrderError('Quantity cannot exceed 1,00,000 units per order');
      return;
    }
    let limitPrice: number | null = null;
    if (orderType === 'LIMIT') {
      const p = parseFloat(orderPrice);
      if (orderPrice.trim() === '' || isNaN(p) || p <= 0) {
        setOrderError('Enter a valid limit price greater than zero');
        return;
      }
      if (p > 1_000_000) {
        setOrderError('Limit price cannot exceed ₹10,00,000');
        return;
      }
      if (!/^\d+(\.\d{1,2})?$/.test(orderPrice.trim())) {
        setOrderError('Limit price can have at most 2 decimal places');
        return;
      }
      limitPrice = p;
    }

    // figure out the side based on asset class
    // mutual funds use SUBSCRIBE, others use BUY
    let side = 'BUY';
    if (orderSecurity.assetClass === 'MUTUAL_FUND') {
      side = 'SUBSCRIBE';
    }

    setOrderError('');
    setOrderSubmitting(true);
    try {
      await placeOrder({
        clientId: clientId,
        securityId: orderSecurity.securityId,
        side: side,
        quantity: qty,
        priceType: orderType === 'LIMIT' ? 'LIMIT' : (side === 'SUBSCRIBE' ? 'NAV' : 'MARKET'),
        limitPrice: limitPrice,
      });
      setOrderSuccess('Order placed successfully! Track it in My Orders.');
      // close modal after 2s
      setTimeout(() => closeModal(), 1500);
    } catch (err: any) {
      const data = err.response?.data;
      // fallback body is a plain string when the order service is down
      const msg = typeof data === 'string' ? data
        : (data?.message || 'Failed to place order. Please try again.');
      setOrderError(msg);
    }
    setOrderSubmitting(false);
  }

  function getAssetClassPill(ac: string): string {
    if (ac === 'EQUITY') return 'pill-info';
    if (ac === 'MUTUAL_FUND') return 'pill-success';
    if (ac === 'ETF') return 'pill-success';
    if (ac === 'BOND') return 'pill-warn';
    if (ac === 'STRUCTURED') return 'pill-danger';
    return 'pill-info';
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-1">Browse & Invest</h1>
      <p className="text-sm text-text-2 mb-5">
        Browse available securities and place orders. Orders are routed and filled by your dealer.
      </p>

      {/* filter bar */}
      <div className="panel mb-4">
        <div className="panel-b py-3">
          <div className="flex gap-3 items-center flex-wrap">
            <input
              className="input max-w-xs"
              type="text"
              placeholder="Search symbol..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            <div className="flex items-center gap-2">
              <span className="text-xs text-text-2 font-medium">Asset class:</span>
              <select
                className="input py-1.5"
                value={assetFilter}
                onChange={(e) => setAssetFilter(e.target.value)}
              >
                {ASSET_CLASSES.map((a) => (
                  <option key={a} value={a}>{a}</option>
                ))}
              </select>
            </div>
            <span className="ml-auto text-xs text-text-2">
              Showing {filtered.length} of {securities.length} securities
            </span>
          </div>
        </div>
      </div>

      {/* service down notice */}
      {serviceDown && (
        <div className="panel mb-4">
          <div className="panel-b text-center py-8">
            <p className="text-3xl mb-2">⚠️</p>
            <p className="font-semibold">Product catalog is unavailable</p>
            <p className="text-sm text-text-2 mt-1">
              The product catalog service isn't running. Ask your administrator to start it.
            </p>
          </div>
        </div>
      )}

      {/* securities table */}
      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading products...</div>
        ) : filtered.length === 0 ? (
          <div className="panel-b text-center text-text-2 py-10">
            {serviceDown ? 'Service offline' : 'No products match your filter'}
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Symbol</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Asset Class</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Price (₹)</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Currency</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Country</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Action</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((s: any) => (
                <tr key={s.securityId} className="border-t border-border-hairline">
                  <td className="px-5 py-3 font-medium mono">{s.symbol}</td>
                  <td className="px-5 py-3">
                    <span className={'pill ' + getAssetClassPill(s.assetClass)}>{s.assetClass}</span>
                  </td>
                  <td className="px-5 py-3 mono text-right font-medium">
                    {s.currentPrice ? '₹' + Number(s.currentPrice).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : <span className="text-text-3 text-xs">NAV</span>}
                  </td>
                  <td className="px-5 py-3 text-text-2">{s.currency}</td>
                  <td className="px-5 py-3 text-text-2">{s.country}</td>
                  <td className="px-5 py-3 text-right">
                    <button onClick={() => openBuyModal(s)} className="btn btn-success btn-sm">
                      {s.assetClass === 'MUTUAL_FUND' ? 'Subscribe' : 'Buy'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* place order modal */}
      {orderSecurity && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="px-6 py-4 border-b border-border-hairline flex items-center justify-between">
              <div>
                <h3 className="font-semibold">
                  {orderSecurity.assetClass === 'MUTUAL_FUND' ? 'Subscribe to ' : 'Buy '}
                  <span className="mono">{orderSecurity.symbol}</span>
                </h3>
                <p className="text-xs text-text-2">{orderSecurity.assetClass} · {orderSecurity.currency}</p>
              </div>
              <button onClick={closeModal} className="text-text-3 text-xl">×</button>
            </div>
            <form onSubmit={submitOrder} className="p-6">

              {/* price info row */}
              <div className="bg-surface rounded-lg px-4 py-3 mb-4 flex items-center justify-between">
                <span className="text-xs text-text-2 font-medium">
                  {orderSecurity.assetClass === 'MUTUAL_FUND' ? 'Price type' : 'Market price'}
                </span>
                {orderSecurity.assetClass === 'MUTUAL_FUND' ? (
                  <span className="text-sm font-semibold text-text-2">NAV (end of day)</span>
                ) : orderSecurity.currentPrice ? (
                  <span className="text-lg font-semibold mono">
                    ₹{Number(orderSecurity.currentPrice).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                  </span>
                ) : (
                  <span className="text-sm text-text-3">Price at execution</span>
                )}
              </div>

              <div className="mb-3">
                <label className="label block mb-1">Quantity</label>
                <input
                  className="input mono"
                  type="number"
                  min="1"
                  placeholder="e.g. 100"
                  value={orderQty}
                  onChange={(e) => setOrderQty(e.target.value)}
                  autoFocus
                />
              </div>

              {/* estimated value preview */}
              {orderSecurity.currentPrice && orderQty && parseInt(orderQty) > 0 && (
                <p className="text-xs text-text-2 mb-3">
                  Estimated value:{' '}
                  <span className="font-semibold mono text-text">
                    ₹{(Number(orderSecurity.currentPrice) * parseInt(orderQty)).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                  </span>
                  <span className="text-text-3 ml-1">(indicative — actual fill price may differ)</span>
                </p>
              )}

              {orderSecurity.assetClass !== 'MUTUAL_FUND' && (
                <>
                  <div className="mb-3">
                    <label className="label block mb-1">Order type</label>
                    <div className="flex gap-2">
                      <button
                        type="button"
                        onClick={() => setOrderType('MARKET')}
                        className={
                          'flex-1 py-2 px-3 text-sm rounded border font-medium ' +
                          (orderType === 'MARKET'
                            ? 'bg-primary text-white border-primary'
                            : 'bg-white text-text border-border')
                        }
                      >
                        Market
                      </button>
                      <button
                        type="button"
                        onClick={() => setOrderType('LIMIT')}
                        className={
                          'flex-1 py-2 px-3 text-sm rounded border font-medium ' +
                          (orderType === 'LIMIT'
                            ? 'bg-primary text-white border-primary'
                            : 'bg-white text-text border-border')
                        }
                      >
                        Limit
                      </button>
                    </div>
                  </div>

                  {orderType === 'LIMIT' && (
                    <div className="mb-3">
                      <label className="label block mb-1">Limit price (₹)</label>
                      <input
                        className="input mono"
                        type="number"
                        step="0.05"
                        min="0"
                        placeholder="e.g. 1648.50"
                        value={orderPrice}
                        onChange={(e) => setOrderPrice(e.target.value)}
                      />
                    </div>
                  )}
                </>
              )}

              {orderSecurity.assetClass === 'MUTUAL_FUND' && (
                <p className="text-xs text-text-3 mb-3">
                  ⓘ Mutual fund subscriptions are filled at end-of-day NAV price.
                </p>
              )}

              {orderError && (
                <div className="pill pill-danger block mb-3 text-center w-full">{orderError}</div>
              )}
              {orderSuccess && (
                <div className="pill pill-success block mb-3 text-center w-full">{orderSuccess}</div>
              )}

              <div className="flex justify-end gap-2 mt-4">
                <button type="button" onClick={closeModal} className="btn btn-ghost">
                  Cancel
                </button>
                <button type="submit" className="btn btn-success" disabled={orderSubmitting}>
                  {orderSubmitting ? 'Placing...' : 'Place order'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
