import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllOrders } from '@/api/orders';
import { getAllSecurities } from '@/api/securities';
import { cachedFetch } from '@/lib/fetchUtils';
import { TableSkeleton } from '@/components/Skeleton';
import EmptyState from '@/components/EmptyState';
import Pagination from '@/components/Pagination';
import { Layers } from 'lucide-react';

const STATUSES = ['ALL', 'PLACED', 'VALIDATED', 'ROUTED', 'PARTIALLY_FILLED', 'FILLED', 'CANCELLED', 'REJECTED'];
const PAGE_SIZE = 15;

export default function OrderBlotter() {
  const [orders, setOrders] = useState<any[]>([]);
  const [secMap, setSecMap] = useState<{ [id: number]: any }>({});
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);

  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    setLoading(true);
    try {
      const [ordersResult, secsResult] = await Promise.allSettled([
        getAllOrders(),
        cachedFetch('securities', getAllSecurities),
      ]);

      if (secsResult.status === 'fulfilled' && Array.isArray(secsResult.value)) {
        const map: { [id: number]: any } = {};
        for (let i = 0; i < secsResult.value.length; i++) {
          map[secsResult.value[i].securityId] = secsResult.value[i];
        }
        setSecMap(map);
      }

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

  // filter orders
  const filtered = orders.filter((o) => {
    if (statusFilter !== 'ALL' && o.status !== statusFilter) return false;
    if (search) {
      const q = search.toLowerCase();
      const sec = secMap[o.securityId];
      if (
        !String(o.orderId).includes(q) &&
        !String(o.clientId).includes(q) &&
        !(sec && sec.symbol.toLowerCase().includes(q))
      ) {
        return false;
      }
    }
    return true;
  });

  const pagedOrders = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  function getStatusPill(status: string): string {
    if (status === 'FILLED' || status === 'PARTIALLY_FILLED') return 'pill-success';
    if (status === 'CANCELLED' || status === 'REJECTED') return 'pill-danger';
    if (status === 'PLACED' || status === 'VALIDATED') return 'pill-info';
    if (status === 'ROUTED') return 'pill-warn';
    return 'pill-info';
  }

  // count by status for header
  const counts: { [k: string]: number } = {};
  for (let i = 0; i < orders.length; i++) {
    counts[orders[i].status] = (counts[orders[i].status] || 0) + 1;
  }

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Order Blotter</h1>
          <p className="text-sm text-text-2">
            All client orders. Click a row to route, fill, or allocate.
          </p>
        </div>
        <button onClick={loadAll} className="btn btn-ghost btn-sm">
          ↻ Refresh
        </button>
      </div>

      {/* status summary KPIs */}
      <div className="grid grid-cols-3 md:grid-cols-6 gap-3 mb-4">
        <div className="panel"><div className="panel-b py-3"><p className="label">Placed</p><p className="text-xl font-semibold mono">{counts.PLACED || 0}</p></div></div>
        <div className="panel"><div className="panel-b py-3"><p className="label">Validated</p><p className="text-xl font-semibold mono">{counts.VALIDATED || 0}</p></div></div>
        <div className="panel"><div className="panel-b py-3"><p className="label">Routed</p><p className="text-xl font-semibold mono">{counts.ROUTED || 0}</p></div></div>
        <div className="panel"><div className="panel-b py-3"><p className="label">Filled</p><p className="text-xl font-semibold mono">{(counts.FILLED || 0) + (counts.PARTIALLY_FILLED || 0)}</p></div></div>
        <div className="panel"><div className="panel-b py-3"><p className="label">Cancelled</p><p className="text-xl font-semibold mono">{counts.CANCELLED || 0}</p></div></div>
        <div className="panel"><div className="panel-b py-3"><p className="label">Rejected</p><p className="text-xl font-semibold mono">{counts.REJECTED || 0}</p></div></div>
      </div>

      {/* filters */}
      <div className="panel mb-4">
        <div className="panel-b py-3">
          <div className="flex gap-3 items-center flex-wrap">
            <input
              className="input max-w-xs"
              type="text"
              placeholder="Search by order ID, client ID, or symbol..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            <div className="flex gap-1 ml-auto flex-wrap">
              {STATUSES.map((s) => (
                <button
                  key={s}
                  onClick={() => setStatusFilter(s)}
                  className={
                    'px-3 py-1.5 text-xs font-medium rounded border ' +
                    (statusFilter === s
                      ? 'bg-primary text-white border-primary'
                      : 'bg-white text-text-2 border-border')
                  }
                >
                  {s}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* orders table */}
      <div className="panel">
        {loading ? (
          <TableSkeleton rows={8} cols={10} />
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<Layers size={26} />}
            title="No matching orders"
            description="Try adjusting the status filter or search query."
          />
        ) : (
          <>
            <table className="w-full text-sm">
              <thead className="bg-surface">
                <tr>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Order</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Date</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Client</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Symbol</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Side</th>
                  <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Qty</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Price Type</th>
                  <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Limit</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Venue</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
                </tr>
              </thead>
              <tbody>
                {pagedOrders.map((o: any) => {
                  const sec = secMap[o.securityId];
                  return (
                    <tr key={o.orderId} className="border-t border-border-hairline hover:bg-surface">
                      <td className="px-5 py-3 mono text-xs">
                        <Link to={'/dealer/orders/' + o.orderId} className="text-primary font-medium">
                          {o.orderId}
                        </Link>
                      </td>
                      <td className="px-5 py-3 mono text-xs text-text-2">
                        {(o.orderDate || '').replace('T', ' ').slice(0, 19)}
                      </td>
                      <td className="px-5 py-3 mono text-xs">{o.clientId}</td>
                      <td className="px-5 py-3 font-medium mono">{sec ? sec.symbol : o.securityId}</td>
                      <td className="px-5 py-3 font-semibold">{o.side}</td>
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
            <Pagination
              page={page}
              total={filtered.length}
              pageSize={PAGE_SIZE}
              onChange={(p) => setPage(p)}
            />
          </>
        )}
      </div>
    </div>
  );
}
