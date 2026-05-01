import { useState, useEffect } from 'react';
import { getOrdersByClient } from '@/api/orders';
import { getAllSecurities } from '@/api/securities';
import { useAuth } from '@/auth/store';

export default function MyOrders() {
  const user = useAuth((s) => s.user);
  const clientId = user?.clientId;

  const [orders, setOrders] = useState<any[]>([]);
  const [secMap, setSecMap] = useState<{ [id: number]: any }>({});
  const [loading, setLoading] = useState(true);

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
                return (
                  <tr key={o.orderId} className="border-t border-border-hairline">
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
                      <span className={'pill ' + getStatusPill(o.status)}>{o.status}</span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
