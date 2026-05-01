import { useState, useEffect } from 'react';
import { getOrdersByStatus } from '@/api/orders';
import { getAllSecurities } from '@/api/securities';
import { createNotification } from '@/api/notifications';
import { cachedFetch } from '@/lib/fetchUtils';

interface FailedOrder {
  orderId: number;
  orderDate: string;
  clientId: number;
  securityId: number;
  side: string;
  quantity: number;
  priceType: string;
  status: string;
}

interface NotifyState {
  orderId: number;
  clientId: number;
  message: string;
  sending: boolean;
  sent: boolean;
  error: string;
}

function getStatusPill(status: string): string {
  if (status === 'REJECTED') return 'pill-danger';
  if (status === 'CANCELLED') return 'pill-warn';
  return 'pill-info';
}

export default function FailedOrders() {
  const [orders, setOrders] = useState<FailedOrder[]>([]);
  const [secMap, setSecMap] = useState<{ [id: number]: any }>({});
  const [loading, setLoading] = useState(true);
  const [notifyState, setNotifyState] = useState<NotifyState | null>(null);

  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    setLoading(true);
    setOrders([]);

    // Two targeted status calls instead of one expensive getAllOrders()
    const [rejectedRes, cancelledRes, secsRes] = await Promise.allSettled([
      getOrdersByStatus('REJECTED'),
      getOrdersByStatus('CANCELLED'),
      cachedFetch('securities', getAllSecurities),
    ]);

    // Build security map
    if (secsRes.status === 'fulfilled' && Array.isArray(secsRes.value)) {
      const map: { [id: number]: any } = {};
      for (const s of secsRes.value) {
        map[s.securityId] = s;
      }
      setSecMap(map);
    }

    // Collect failed orders from both sources, deduplicate by orderId
    const seen = new Set<number>();
    const collected: FailedOrder[] = [];

    function addIfFailed(o: any) {
      if (!o || seen.has(o.orderId)) return;
      if (o.status === 'REJECTED' || o.status === 'CANCELLED') {
        seen.add(o.orderId);
        collected.push(o as FailedOrder);
      }
    }

    if (rejectedRes.status === 'fulfilled' && Array.isArray(rejectedRes.value)) {
      for (const o of rejectedRes.value) addIfFailed(o);
    }

    if (cancelledRes.status === 'fulfilled' && Array.isArray(cancelledRes.value)) {
      for (const o of cancelledRes.value) addIfFailed(o);
    }

    // Sort newest first
    collected.sort((a, b) => (b.orderDate || '').localeCompare(a.orderDate || ''));
    setOrders(collected);
    setLoading(false);
  }

  // KPI counts
  const totalRejected = orders.filter((o) => o.status === 'REJECTED').length;
  const totalCancelled = orders.filter((o) => o.status === 'CANCELLED').length;
  const todayStr = new Date().toISOString().slice(0, 10);
  const todayCount = orders.filter(
    (o) => o.status === 'REJECTED' && (o.orderDate || '').startsWith(todayStr)
  ).length;

  // Notify helpers
  function openNotify(order: FailedOrder) {
    setNotifyState({
      orderId: order.orderId,
      clientId: order.clientId,
      message: `Order ${order.orderId} for client ${order.clientId} was rejected — please review and take action.`,
      sending: false,
      sent: false,
      error: '',
    });
  }

  function closeNotify() {
    setNotifyState(null);
  }

  async function sendNotification() {
    if (!notifyState) return;
    setNotifyState((prev) => prev ? { ...prev, sending: true, error: '' } : prev);
    try {
      await createNotification({
        userId: notifyState.clientId,
        message: notifyState.message,
        category: 'Compliance',
      });
      setNotifyState((prev) => prev ? { ...prev, sending: false, sent: true } : prev);
      setTimeout(() => setNotifyState(null), 1500);
    } catch (e: any) {
      const msg =
        typeof e?.response?.data === 'string'
          ? e.response.data
          : e?.response?.data?.message || 'Failed to send notification.';
      setNotifyState((prev) => prev ? { ...prev, sending: false, error: msg } : prev);
    }
  }

  return (
    <div>
      {/* Header */}
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Failed &amp; Rejected Orders</h1>
          <p className="text-sm text-text-2">
            Orders that were rejected or cancelled — for compliance monitoring and client follow-up.
          </p>
        </div>
        <button onClick={loadAll} disabled={loading} className="btn btn-ghost btn-sm">
          {loading ? 'Loading...' : '↻ Refresh'}
        </button>
      </div>

      {/* KPI cards */}
      <div className="grid grid-cols-3 gap-4 mb-5">
        <div className="panel">
          <div className="panel-b">
            <p className="label">Total Rejected</p>
            <p className={'text-3xl font-bold mono mt-1 ' + (totalRejected > 0 ? 'text-danger' : 'text-success')}>
              {loading ? '—' : totalRejected}
            </p>
            <p className="text-xs text-text-3 mt-1">all time</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Rejected Today</p>
            <p className={'text-3xl font-bold mono mt-1 ' + (todayCount > 0 ? 'text-danger' : 'text-success')}>
              {loading ? '—' : todayCount}
            </p>
            <p className="text-xs text-text-3 mt-1">{todayStr}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Cancelled</p>
            <p className={'text-3xl font-bold mono mt-1 ' + (totalCancelled > 0 ? 'text-text-2' : 'text-success')}>
              {loading ? '—' : totalCancelled}
            </p>
            <p className="text-xs text-text-3 mt-1">manually cancelled</p>
          </div>
        </div>
      </div>

      {/* Inline notify panel */}
      {notifyState && (
        <div className="panel mb-4">
          <div className="panel-h">
            <span className="text-sm font-medium">
              Send Compliance Notification — Order{' '}
              <span className="mono">{notifyState.orderId}</span>
            </span>
            <button onClick={closeNotify} className="btn btn-ghost btn-sm ml-auto">
              Cancel
            </button>
          </div>
          <div className="panel-b">
            <label className="label block mb-1">Message to client {notifyState.clientId}</label>
            <textarea
              className="input w-full mb-3"
              rows={3}
              value={notifyState.message}
              onChange={(e) =>
                setNotifyState((prev) => prev ? { ...prev, message: e.target.value } : prev)
              }
              disabled={notifyState.sending || notifyState.sent}
            />
            {notifyState.error && (
              <p className="text-sm text-danger mb-2">{notifyState.error}</p>
            )}
            {notifyState.sent && (
              <p className="text-sm text-success mb-2">Notification sent successfully.</p>
            )}
            <div className="flex gap-2">
              <button
                onClick={sendNotification}
                disabled={notifyState.sending || notifyState.sent || !notifyState.message.trim()}
                className="btn btn-primary btn-sm"
              >
                {notifyState.sending ? 'Sending...' : notifyState.sent ? 'Sent' : 'Send Notification'}
              </button>
              <button onClick={closeNotify} className="btn btn-ghost btn-sm">
                Dismiss
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Orders table */}
      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading rejected orders...</div>
        ) : orders.length === 0 ? (
          <div className="panel-b text-center py-10">
            <p className="text-4xl mb-2">✓</p>
            <p className="font-semibold text-success">No rejected orders</p>
            <p className="text-sm text-text-2 mt-1">
              There are currently no rejected or failed orders on record.
            </p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Order ID</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Date</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Client ID</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Symbol</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Side</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Qty</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Price Type</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Action</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((o) => {
                const sec = secMap[o.securityId];
                const symbol = sec ? sec.symbol : String(o.securityId);
                const isActiveNotify = notifyState?.orderId === o.orderId;
                return (
                  <tr
                    key={o.orderId}
                    className={
                      'border-t border-border-hairline ' +
                      (isActiveNotify ? 'bg-surface' : 'hover:bg-surface')
                    }
                  >
                    <td className="px-5 py-3 mono text-xs font-medium">{o.orderId}</td>
                    <td className="px-5 py-3 mono text-xs text-text-2">
                      {(o.orderDate || '').replace('T', ' ').slice(0, 16)}
                    </td>
                    <td className="px-5 py-3 mono text-xs">{o.clientId}</td>
                    <td className="px-5 py-3 font-medium mono">{symbol}</td>
                    <td className="px-5 py-3 font-semibold">{o.side}</td>
                    <td className="px-5 py-3 mono text-right">{o.quantity}</td>
                    <td className="px-5 py-3 text-xs text-text-2">{o.priceType}</td>
                    <td className="px-5 py-3">
                      <span className={'pill ' + getStatusPill(o.status)}>{o.status}</span>
                    </td>
                    <td className="px-5 py-3">
                      <button
                        onClick={() => openNotify(o)}
                        className="btn btn-ghost btn-sm"
                        disabled={isActiveNotify}
                      >
                        Notify
                      </button>
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
