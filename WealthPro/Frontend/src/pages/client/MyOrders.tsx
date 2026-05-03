import { useState, useEffect } from 'react';
import { getOrdersByClient, getOrderLifecycle } from '@/api/orders';
import { getAllSecurities } from '@/api/securities';
import { useAuth } from '@/auth/store';

export default function MyOrders() {
  const user = useAuth((s) => s.user);
  const clientId = user?.clientId;

  const [orders, setOrders] = useState<any[]>([]);
  const [secMap, setSecMap] = useState<{ [id: number]: any }>({});
  const [loading, setLoading] = useState(true);

  // Rejection reason state — keyed by orderId
  // null  = not loaded yet
  // []    = loaded, no failed checks found
  // [...]  = loaded, array of {checkType, message}
  const [reasons, setReasons]           = useState<{ [orderId: number]: { checkType: string; message: string }[] | null }>({});
  const [expandedOrder, setExpandedOrder] = useState<number | null>(null);
  const [loadingReason, setLoadingReason] = useState<number | null>(null);

  useEffect(() => {
    if (clientId) loadAll();
  }, [clientId]);

  async function loadAll() {
    if (!clientId) return;
    setLoading(true);
    try {
      // load orders and securities in parallel - both might fail independently
      const [ordersResult, secsResult] = await Promise.allSettled([
        getOrdersByClient(clientId),
        getAllSecurities(),
      ]);

      // securities are nice-to-have for symbol lookup
      if (secsResult.status === 'fulfilled' && Array.isArray(secsResult.value)) {
        const map: { [id: number]: any } = {};
        for (let i = 0; i < secsResult.value.length; i++) {
          map[secsResult.value[i].securityId] = secsResult.value[i];
        }
        setSecMap(map);
      }

      // orders are the main data
      if (ordersResult.status === 'fulfilled' && Array.isArray(ordersResult.value)) {
        const sorted = [...ordersResult.value].sort((a, b) =>
          (b.orderDate || '').localeCompare(a.orderDate || '')
        );
        setOrders(sorted);
      } else {
        setOrders([]);
      }
    } catch (e) {
    }
    setLoading(false);
  }

  async function toggleReason(orderId: number) {
    // Collapse if already open
    if (expandedOrder === orderId) {
      setExpandedOrder(null);
      return;
    }
    setExpandedOrder(orderId);
    // Already fetched — don't fetch again
    if (reasons[orderId] !== undefined) return;

    setLoadingReason(orderId);
    try {
      const lifecycle = await getOrderLifecycle(orderId);
      const failedChecks = (lifecycle.preTradeChecks || [])
        .filter((c: any) => c.result === 'FAIL')
        .map((c: any) => ({ checkType: c.checkType, message: c.message || '' }));
      setReasons((prev) => ({ ...prev, [orderId]: failedChecks }));
    } catch {
      setReasons((prev) => ({ ...prev, [orderId]: [] }));
    }
    setLoadingReason(null);
  }

  function getStatusPill(status: string): string {
    if (status === 'FILLED') return 'pill-success';
    if (status === 'PARTIALLY_FILLED') return 'pill-success';
    if (status === 'CANCELLED' || status === 'REJECTED') return 'pill-danger';
    if (status === 'PLACED' || status === 'VALIDATED') return 'pill-info';
    if (status === 'ROUTED') return 'pill-warn';
    return 'pill-info';
  }

  function getSideColor(side: string): string {
    if (side === 'BUY' || side === 'SUBSCRIBE') return 'text-success';
    return 'text-danger';
  }

  if (!clientId) return <div className="p-10 text-center text-text-2">Loading...</div>;

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-1">My Orders</h1>
      <p className="text-sm text-text-2 mb-5">
        All orders you've placed. Status updates automatically as your dealer routes and fills them.
      </p>

      <div className="panel">
        <div className="panel-h">
          <h3>Orders ({orders.length})</h3>
        </div>
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading orders...</div>
        ) : orders.length === 0 ? (
          <div className="panel-b text-center py-10">
            <p className="text-3xl mb-2">📭</p>
            <p className="font-semibold">No orders yet</p>
            <p className="text-sm text-text-2 mt-1">
              Browse products and place your first order.
            </p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Date</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Symbol</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Side</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Qty</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Price Type</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Limit Price</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Venue</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((o: any) => {
                const sec = secMap[o.securityId];
                const isRejected = o.status === 'REJECTED';
                const isExpanded = expandedOrder === o.orderId;
                const orderReasons = reasons[o.orderId];
                return (
                  <>
                    <tr key={o.orderId} className={'border-t border-border-hairline ' + (isRejected ? 'bg-danger-soft/20' : '')}>
                      <td className="px-5 py-3 mono text-xs text-text-2">
                        {(o.orderDate || '').replace('T', ' ').slice(0, 19)}
                      </td>
                      <td className="px-5 py-3 font-medium mono">{sec ? sec.symbol : '#' + o.securityId}</td>
                      <td className={'px-5 py-3 font-semibold ' + getSideColor(o.side)}>
                        {o.side}
                      </td>
                      <td className="px-5 py-3 mono text-right">{o.quantity}</td>
                      <td className="px-5 py-3 text-xs text-text-2">{o.priceType}</td>
                      <td className="px-5 py-3 mono text-right">{o.limitPrice ? '₹' + o.limitPrice : '-'}</td>
                      <td className="px-5 py-3 text-xs text-text-2">{o.routedVenue || '-'}</td>
                      <td className="px-5 py-3">
                        <div className="flex items-center gap-2">
                          <span className={'pill ' + getStatusPill(o.status)}>{o.status}</span>
                          {isRejected && (
                            <button
                              onClick={() => toggleReason(o.orderId)}
                              className="text-xs text-danger underline hover:no-underline shrink-0"
                            >
                              {isExpanded ? 'Hide' : 'Why?'}
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>

                    {/* Rejection reason expandable row */}
                    {isRejected && isExpanded && (
                      <tr key={o.orderId + '-reason'} className="bg-danger-soft/30">
                        <td colSpan={8} className="px-5 pb-3 pt-0">
                          {loadingReason === o.orderId ? (
                            <p className="text-xs text-text-2 py-2">Loading rejection details...</p>
                          ) : orderReasons && orderReasons.length > 0 ? (
                            <div className="border border-danger/20 rounded-lg p-3 bg-white mt-1">
                              <p className="text-xs font-semibold text-danger mb-2">
                                ⛔ Rejection reason{orderReasons.length > 1 ? 's' : ''}
                              </p>
                              <div className="space-y-1.5">
                                {orderReasons.map((r, i) => (
                                  <div key={i} className="flex gap-2 text-xs">
                                    <span className="font-medium text-text-2 shrink-0 w-24">{r.checkType}</span>
                                    <span className="text-danger">{r.message}</span>
                                  </div>
                                ))}
                              </div>
                            </div>
                          ) : (
                            <p className="text-xs text-text-2 py-2 italic">
                              No detailed rejection reason recorded.
                            </p>
                          )}
                        </td>
                      </tr>
                    )}
                  </>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
