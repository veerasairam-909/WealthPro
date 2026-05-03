import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '@/auth/store';
import { getAccountsByClientId } from '@/api/accounts';
import { getHoldingsByAccountId } from '@/api/holdings';
import { getBalanceByAccountId } from '@/api/cashLedger';
import { getOrdersByClient } from '@/api/orders';
import { getAllSecurities } from '@/api/securities';
import { getUnreadNotifications } from '@/api/notifications';
import { getPerformanceByAccount, getRiskMeasuresByAccount } from '@/api/analytics';
import { getRecommendationsByClientId } from '@/api/recommendations';
import {
  PieChart, Pie, Cell, Sector, Tooltip, ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, ReferenceLine, LabelList,
} from 'recharts';

const ASSET_COLORS: Record<string, string> = {
  EQUITY:      '#f97316',
  MUTUAL_FUND: '#22c55e',
  BOND:        '#3b82f6',
  ETF:         '#a855f7',
  CASH:        '#94a3b8',
};
const FALLBACK_COLORS = ['#06b6d4', '#ec4899', '#eab308', '#14b8a6'];

function assetColor(name: string, idx: number) {
  return ASSET_COLORS[name] ?? FALLBACK_COLORS[idx % FALLBACK_COLORS.length];
}

function fmtShort(v: number) {
  if (v >= 1_00_00_000) return '₹' + (v / 1_00_00_000).toFixed(1) + 'Cr';
  if (v >= 1_00_000)    return '₹' + (v / 1_00_000).toFixed(1) + 'L';
  if (v >= 1_000)       return '₹' + (v / 1_000).toFixed(1) + 'K';
  return '₹' + v;
}

function renderActiveShape(props: any) {
  const { cx, cy, innerRadius, outerRadius, startAngle, endAngle,
          fill, payload, percent, value } = props;
  return (
    <g>
      <Sector cx={cx} cy={cy} innerRadius={innerRadius} outerRadius={outerRadius + 10}
              startAngle={startAngle} endAngle={endAngle} fill={fill} />
      <Sector cx={cx} cy={cy} innerRadius={outerRadius + 14} outerRadius={outerRadius + 18}
              startAngle={startAngle} endAngle={endAngle} fill={fill} opacity={0.4} />
      <text x={cx} y={cy - 16} textAnchor="middle" fill={fill}
            fontSize={13} fontWeight={700} fontFamily="system-ui">
        {payload.name}
      </text>
      <text x={cx} y={cy + 4} textAnchor="middle" fill="#374151"
            fontSize={12} fontFamily="monospace">
        {fmtShort(value)}
      </text>
      <text x={cx} y={cy + 22} textAnchor="middle" fill="#9ca3af" fontSize={11}>
        {(percent * 100).toFixed(1)}%
      </text>
    </g>
  );
}

// Custom bar shape: rounds top corners for gains, bottom corners for losses.
// Handles both positive and negative height values that Recharts may pass.
function PnlBar(props: any) {
  const { x, y, width, height, value } = props;
  const absH = Math.abs(Number(height));
  if (!width || width <= 0 || absH < 1) return null;
  const r = Math.min(5, width / 2, absH / 2);
  const isNeg = Number(value) < 0;
  const fill = isNeg ? '#ef4444' : '#22c55e';
  // Normalise origin: Recharts may give a negative height for negative-value bars
  const top = Number(height) < 0 ? Number(y) + Number(height) : Number(y);
  const path = isNeg
    // negative bar: flat top (zero-line), rounded bottom corners
    ? `M${x},${top} h${width} v${absH - r} a${r},${r} 0 0 1 ${-r},${r} h${-(width - 2 * r)} a${r},${r} 0 0 1 ${-r},${-r} z`
    // positive bar: flat bottom (zero-line), rounded top corners
    : `M${x},${top + absH} h${width} v${-(absH - r)} a${r},${r} 0 0 0 ${-r},${-r} h${-(width - 2 * r)} a${r},${r} 0 0 0 ${-r},${r} z`;
  return <path d={path} fill={fill} opacity={0.88} />;
}

export default function Dashboard() {
  const user = useAuth((s) => s.user);
  const clientId = user?.clientId;

  const [loading, setLoading] = useState(true);
  const [account, setAccount] = useState<any>(null);
  const [holdings, setHoldings] = useState<any[]>([]);
  const [secMap, setSecMap] = useState<{ [id: number]: any }>({});
  const [cashBalance, setCashBalance] = useState<number | null>(null);
  const [recentOrders, setRecentOrders] = useState<any[]>([]);
  const [unreadNotifs, setUnreadNotifs] = useState<any[]>([]);
  const [activeAllocIdx, setActiveAllocIdx] = useState(-1);
  const [perfRecords, setPerfRecords] = useState<any[]>([]);
  const [riskMeasures, setRiskMeasures] = useState<any[]>([]);
  const [pendingRecos, setPendingRecos] = useState<any[]>([]);

  const loadPendingRecos = useCallback(async () => {
    if (!clientId) return;
    try {
      const res = await getRecommendationsByClientId(clientId);
      if (Array.isArray(res)) {
        setPendingRecos(res.filter((r: any) => r.status === 'SUBMITTED'));
      }
    } catch (e) {}
  }, [clientId]);

  useEffect(() => {
    if (clientId) loadAll();
  }, [clientId]);

  // Re-check pending recommendations whenever the tab comes back into focus
  // so the banner updates if the RM added a new recommendation while the
  // client was away
  useEffect(() => {
    function onFocus() {
      if (clientId) loadPendingRecos();
    }
    document.addEventListener('visibilitychange', onFocus);
    window.addEventListener('focus', onFocus);
    return () => {
      document.removeEventListener('visibilitychange', onFocus);
      window.removeEventListener('focus', onFocus);
    };
  }, [clientId, loadPendingRecos]);

  async function loadAll() {
    if (!clientId) return;
    setLoading(true);
    try {
      const [accsRes, secsRes, ordersRes, notifsRes, recosRes] = await Promise.allSettled([
        getAccountsByClientId(clientId),
        getAllSecurities(),
        getOrdersByClient(clientId),
        getUnreadNotifications(clientId),
        getRecommendationsByClientId(clientId),
      ]);

      // pending recommendations (SUBMITTED = awaiting client action)
      if (recosRes.status === 'fulfilled' && Array.isArray(recosRes.value)) {
        setPendingRecos(recosRes.value.filter((r: any) => r.status === 'SUBMITTED'));
      }

      // build security map
      if (secsRes.status === 'fulfilled' && Array.isArray(secsRes.value)) {
        const map: { [id: number]: any } = {};
        for (let i = 0; i < secsRes.value.length; i++) {
          map[secsRes.value[i].securityId] = secsRes.value[i];
        }
        setSecMap(map);
      }

      // recent orders (last 5)
      if (ordersRes.status === 'fulfilled' && Array.isArray(ordersRes.value)) {
        const sorted = [...ordersRes.value].sort((a, b) =>
          (b.orderDate || '').localeCompare(a.orderDate || '')
        );
        setRecentOrders(sorted.slice(0, 5));
      }

      // unread notifications (last 4)
      if (notifsRes.status === 'fulfilled' && Array.isArray(notifsRes.value)) {
        setUnreadNotifs(notifsRes.value.slice(0, 4));
      }

      // account → holdings + cash balance
      if (accsRes.status === 'fulfilled' && Array.isArray(accsRes.value) && accsRes.value.length > 0) {
        const acc = accsRes.value[0];
        setAccount(acc);

        const [holdRes, balRes] = await Promise.allSettled([
          getHoldingsByAccountId(acc.accountId),
          getBalanceByAccountId(acc.accountId),
        ]);

        if (holdRes.status === 'fulfilled' && Array.isArray(holdRes.value)) {
          setHoldings(holdRes.value);
        }
        if (balRes.status === 'fulfilled') {
          setCashBalance(Number(balRes.value) || 0);
        }

        // load analytics — best-effort, don't block dashboard if service is down
        const [perfRes, riskRes] = await Promise.allSettled([
          getPerformanceByAccount(acc.accountId),
          getRiskMeasuresByAccount(acc.accountId),
        ]);
        if (perfRes.status === 'fulfilled' && Array.isArray(perfRes.value)) {
          setPerfRecords(perfRes.value);
        }
        if (riskRes.status === 'fulfilled' && Array.isArray(riskRes.value)) {
          setRiskMeasures(riskRes.value);
        }
      }
    } catch (e) {
    }
    setLoading(false);
  }

  // calculate portfolio stats
  let portfolioValue = 0;
  let totalInvested = 0;
  const allocationMap: Record<string, number> = {};
  const allocationInvestedMap: Record<string, number> = {};

  for (let i = 0; i < holdings.length; i++) {
    const h = holdings[i];
    const sec = secMap[h.securityId];
    const qty = Number(h.quantity) || 0;
    const avgCost = Number(h.avgCost) || 0;
    const currentPrice = sec?.currentPrice ? Number(sec.currentPrice) : avgCost;
    const mv = qty * currentPrice;
    portfolioValue += mv;
    totalInvested += qty * avgCost;

    const cls = sec?.assetClass ?? 'OTHER';
    allocationMap[cls] = (allocationMap[cls] || 0) + mv;
    allocationInvestedMap[cls] = (allocationInvestedMap[cls] || 0) + (qty * avgCost);
  }
  const totalPnl = portfolioValue - totalInvested;
  const pnlPct = totalInvested > 0 ? (totalPnl / totalInvested) * 100 : 0;

  // chart data
  const allocationData = Object.entries(allocationMap).map(([name, value]) => ({
    name: name.replace('_', ' '),
    value: Math.round(value),
    raw: name,
  }));

  // P&L broken down per asset class — drives "Returns by Asset Class" line chart
  const assetPnlData = Object.entries(allocationMap).map(([cls, mv]) => {
    const invested = allocationInvestedMap[cls] || 0;
    const pnl = Math.round(mv - invested);
    const pct = invested > 0 ? ((mv - invested) / invested) * 100 : 0;
    const label = cls.split('_').map((w: string) => w[0] + w.slice(1).toLowerCase()).join(' ');
    return { name: label, pnl, pct, absPct: Math.abs(pct), raw: cls, mv: Math.round(mv), invested: Math.round(invested) };
  }).sort((a, b) => b.pct - a.pct);


  function fmt(n: number) {
    return n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  function getStatusPill(s: string) {
    if (s === 'FILLED' || s === 'PARTIALLY_FILLED') return 'pill-success';
    if (s === 'CANCELLED' || s === 'REJECTED') return 'pill-danger';
    if (s === 'ROUTED') return 'pill-warn';
    return 'pill-info';
  }

  if (!clientId) return <div className="p-10 text-center text-text-2">Loading...</div>;

  if (loading) return <div className="p-10 text-center text-text-2">Loading dashboard...</div>;

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">My Dashboard</h1>
          <p className="text-sm text-text-2">Welcome back, {user?.username}</p>
        </div>
        <Link to="/me/products" className="btn btn-success btn-sm">+ Invest</Link>
      </div>

      {/* Pending RM recommendations alert */}
      {pendingRecos.length > 0 && (
        <div className="bg-primary-soft border border-primary/30 rounded-lg p-4 mb-5 flex items-center gap-4">
          <div className="text-2xl shrink-0">📋</div>
          <div className="flex-1 min-w-0">
            <p className="font-semibold text-primary">
              {pendingRecos.length === 1
                ? 'Your RM has prepared 1 investment recommendation for you'
                : `Your RM has prepared ${pendingRecos.length} investment recommendations for you`}
            </p>
            <p className="text-sm text-text-2 mt-0.5">
              Review the proposal, select a security, and place an order — or decline if you prefer.
            </p>
          </div>
          <Link
            to="/me/recommendations"
            className="btn btn-primary btn-sm shrink-0"
          >
            Review now →
          </Link>
        </div>
      )}

      {/* portfolio KPIs */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-5">
        <div className="panel">
          <div className="panel-b">
            <p className="label">Portfolio Value</p>
            <p className="text-2xl font-semibold mono mt-1">₹{fmt(portfolioValue)}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Total Invested</p>
            <p className="text-2xl font-semibold mono mt-1">₹{fmt(totalInvested)}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Total P&L</p>
            <p className={'text-2xl font-semibold mono mt-1 leading-none ' + (totalPnl >= 0 ? 'text-success' : 'text-danger')}>
              {totalPnl >= 0 ? '+' : '-'}₹{fmt(Math.abs(totalPnl))}
            </p>
            <p className={'text-sm mt-1 ' + (totalPnl >= 0 ? 'text-success' : 'text-danger')}>
              {pnlPct >= 0 ? '+' : ''}{pnlPct.toFixed(2)}%
            </p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Cash Balance</p>
            <p className="text-2xl font-semibold mono mt-1">
              {cashBalance !== null ? '₹' + fmt(cashBalance) : <span className="text-text-3 text-sm">No account</span>}
            </p>
          </div>
        </div>
      </div>

      {/* ── Charts ── */}
      {!loading && holdings.length > 0 && (
        <div className="grid md:grid-cols-2 gap-4 mb-4">

          {/* ── Donut: Portfolio Allocation ── */}
          <div className="panel">
            <div className="panel-h">
              <h3>Portfolio Allocation</h3>
              <span className="text-xs text-text-2 mono font-medium">{fmtShort(portfolioValue)} total</span>
            </div>
            <div className="panel-b">
              {/* chart + overlay wrapper */}
              <div className="relative">
                <ResponsiveContainer width="100%" height={210}>
                  <PieChart>
                    <Pie
                      data={allocationData}
                      cx="50%"
                      cy="50%"
                      innerRadius={72}
                      outerRadius={106}
                      paddingAngle={allocationData.length > 1 ? 3 : 0}
                      dataKey="value"
                      strokeWidth={0}
                      {...({ activeIndex: activeAllocIdx } as any)}
                      activeShape={renderActiveShape}
                      onMouseEnter={(_: any, idx: number) => setActiveAllocIdx(idx)}
                      onMouseLeave={() => setActiveAllocIdx(-1)}
                    >
                      {allocationData.map((entry, idx) => (
                        <Cell key={entry.raw} fill={assetColor(entry.raw, idx)} />
                      ))}
                    </Pie>
                  </PieChart>
                </ResponsiveContainer>
                {/* Centre label — visible only when nothing is hovered */}
                {activeAllocIdx === -1 && (
                  <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                    <div className="text-center">
                      <p className="text-[10px] text-text-3 uppercase tracking-widest mb-0.5">Portfolio</p>
                      <p className="text-sm font-bold mono text-text">{fmtShort(portfolioValue)}</p>
                      <p className={'text-xs font-medium mt-0.5 ' + (totalPnl >= 0 ? 'text-success' : 'text-danger')}>
                        {totalPnl >= 0 ? '▲ +' : '▼ '}{Math.abs(pnlPct).toFixed(1)}%
                      </p>
                    </div>
                  </div>
                )}
              </div>
              {/* Custom legend below the donut */}
              <div className="flex flex-wrap justify-center gap-x-5 gap-y-2 pt-1 pb-1 border-t border-border-hairline mt-1">
                {allocationData.map((entry, idx) => {
                  const pct = portfolioValue > 0 ? ((entry.value / portfolioValue) * 100).toFixed(1) : '0';
                  return (
                    <div key={entry.raw} className="flex items-center gap-1.5 text-xs cursor-default"
                         onMouseEnter={() => setActiveAllocIdx(idx)}
                         onMouseLeave={() => setActiveAllocIdx(-1)}>
                      <span className="w-2.5 h-2.5 rounded-full shrink-0"
                            style={{ background: assetColor(entry.raw, idx) }} />
                      <span className="text-text-2">{entry.name}</span>
                      <span className="font-semibold text-text">{pct}%</span>
                      <span className="text-text-3">{fmtShort(entry.value)}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          {/* ── Positive / Negative Bar Chart: Returns by Asset Class ── */}
          <div className="panel">
            <div className="panel-h">
              <h3>Returns by Asset Class</h3>
              <span className={'text-xs font-semibold ' + (totalPnl >= 0 ? 'text-success' : 'text-danger')}>
                {totalPnl >= 0 ? '▲ +' : '▼ '}{Math.abs(pnlPct).toFixed(2)}% overall
              </span>
            </div>
            <div className="panel-b">
              <ResponsiveContainer width="100%" height={270}>
                <BarChart
                  data={assetPnlData}
                  margin={{ top: 28, right: 16, left: 4, bottom: 4 }}
                  barCategoryGap="38%"
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" vertical={false} />
                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 12 }}
                    axisLine={false} tickLine={false}
                  />
                  <YAxis
                    tick={{ fontSize: 11 }}
                    axisLine={false} tickLine={false}
                    tickFormatter={(v: any) => {
                      const n = Number(v);
                      return (n > 0 ? '+' : '') + n.toFixed(0) + '%';
                    }}
                    domain={[
                      (min: number) => min >= 0 ? 0 : Math.floor(min * 1.3),
                      (max: number) => max <= 0 ? 0 : Math.ceil(max * 1.3),
                    ]}
                  />
                  {/* Zero baseline */}
                  <ReferenceLine y={0} stroke="#cbd5e1" strokeWidth={1.5} />
                  <Tooltip
                    cursor={{ fill: '#f9fafb' }}
                    content={({ active, payload }: any) => {
                      if (!active || !payload?.length) return null;
                      const d = payload[0].payload;
                      return (
                        <div className="bg-white border border-border rounded-lg shadow-lg px-4 py-3 text-sm min-w-[175px]">
                          <div className="flex items-center gap-2 mb-2">
                            <span
                              className="w-2.5 h-2.5 rounded-full shrink-0"
                              style={{ background: assetColor(d.raw, assetPnlData.findIndex(x => x.raw === d.raw)) }}
                            />
                            <p className="font-semibold">{d.name}</p>
                          </div>
                          <div className="space-y-1 text-xs">
                            <div className="flex justify-between gap-4">
                              <span className="text-text-3">Invested</span>
                              <span className="mono">₹{d.invested.toLocaleString('en-IN')}</span>
                            </div>
                            <div className="flex justify-between gap-4">
                              <span className="text-text-3">Current</span>
                              <span className="mono">₹{d.mv.toLocaleString('en-IN')}</span>
                            </div>
                            <div className={'flex justify-between gap-4 font-semibold pt-1 border-t border-border-hairline ' +
                                (d.pnl >= 0 ? 'text-success' : 'text-danger')}>
                              <span>P&L</span>
                              <span className="mono">
                                {d.pnl >= 0 ? '+' : '–'}₹{Math.abs(d.pnl).toLocaleString('en-IN')}
                              </span>
                            </div>
                            <div className={'flex justify-between gap-4 ' + (d.pct >= 0 ? 'text-success' : 'text-danger')}>
                              <span>Return</span>
                              <span className="font-semibold">
                                {d.pct >= 0 ? '+' : ''}{d.pct.toFixed(2)}%
                              </span>
                            </div>
                          </div>
                        </div>
                      );
                    }}
                  />
                  <Bar dataKey="pct" maxBarSize={60} shape={(p: any) => <PnlBar {...p} />}>
                    {/* Label: above bar for gains, below bar for losses */}
                    <LabelList
                      dataKey="pct"
                      content={(props: any) => {
                        const { x, y, width, height, value } = props;
                        if (value == null || !width) return null;
                        const n = Number(value);
                        const isNeg = n < 0;
                        const cx = Number(x) + Number(width) / 2;
                        // Compute true bar bottom/top regardless of height sign
                        const barTop    = Math.min(Number(y), Number(y) + Number(height));
                        const barBottom = Math.max(Number(y), Number(y) + Number(height));
                        const cy = isNeg ? barBottom + 16 : barTop - 8;
                        return (
                          <text
                            x={cx} y={cy}
                            textAnchor="middle"
                            fontSize={11} fontWeight={700}
                            fill={isNeg ? '#dc2626' : '#16a34a'}
                          >
                            {n >= 0 ? '+' : ''}{n.toFixed(1)}%
                          </text>
                        );
                      }}
                    />
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

        </div>
      )}

      {/* ── Performance Records ── */}
      {perfRecords.length > 0 && (
        <div className="panel mb-4">
          <div className="panel-h">
            <h3>Performance History</h3>
            <span className="text-xs text-text-2">vs. benchmark</span>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-surface">
                <tr>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Period</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Date Range</th>
                  <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Return %</th>
                  <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Benchmark %</th>
                  <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Alpha</th>
                </tr>
              </thead>
              <tbody>
                {perfRecords.slice(0, 6).map((p: any) => {
                  const ret = Number(p.returnPercentage) || 0;
                  const bench = Number(p.benchmarkReturnPercentage) || 0;
                  const alpha = ret - bench;
                  return (
                    <tr key={p.recordId} className="border-t border-border-hairline">
                      <td className="px-5 py-3 font-medium">{p.period}</td>
                      <td className="px-5 py-3 mono text-xs text-text-2">
                        {p.startDate} → {p.endDate}
                      </td>
                      <td className={'px-5 py-3 mono text-right font-semibold ' + (ret >= 0 ? 'text-success' : 'text-danger')}>
                        {ret >= 0 ? '+' : ''}{ret.toFixed(2)}%
                      </td>
                      <td className="px-5 py-3 mono text-right text-text-2">
                        {bench.toFixed(2)}%
                      </td>
                      <td className={'px-5 py-3 mono text-right font-semibold ' + (alpha >= 0 ? 'text-success' : 'text-danger')}>
                        {alpha >= 0 ? '+' : ''}{alpha.toFixed(2)}%
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* ── Risk Measures ── */}
      {riskMeasures.length > 0 && (
        <div className="panel mb-4">
          <div className="panel-h">
            <h3>Risk Measures</h3>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-0 divide-x divide-border-hairline">
            {riskMeasures.map((r: any) => (
              <div key={r.measureId} className="panel-b text-center">
                <p className="label">{r.measureType?.replace(/_/g, ' ')}</p>
                <p className="text-2xl font-bold mono mt-1">{Number(r.value).toFixed(2)}</p>
                <p className="text-xs text-text-3 mt-0.5">{r.period}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="grid md:grid-cols-2 gap-4 mb-4">

        {/* holdings snapshot */}
        <div className="panel">
          <div className="panel-h">
            <h3>Holdings ({holdings.length})</h3>
            <Link to="/me/holdings" className="text-sm text-primary">View all</Link>
          </div>
          {holdings.length === 0 ? (
            <div className="panel-b text-center text-text-2 py-8 text-sm">
              No holdings yet.{' '}
              <Link to="/me/products" className="text-primary">Start investing →</Link>
            </div>
          ) : (
            <div className="panel-b text-sm space-y-0">
              {holdings.slice(0, 5).map((h: any) => {
                const sec = secMap[h.securityId];
                const qty = Number(h.quantity) || 0;
                const avgCost = Number(h.avgCost) || 0;
                const price = sec?.currentPrice ? Number(sec.currentPrice) : avgCost;
                const value = qty * price;
                const pnl = (price - avgCost) * qty;
                return (
                  <div key={h.holdingId} className="flex items-center justify-between py-2.5 border-b border-border-hairline last:border-0">
                    <div>
                      <p className="font-medium mono">{sec ? sec.symbol : '#' + h.securityId}</p>
                      <p className="text-xs text-text-2">{qty} units @ ₹{fmt(avgCost)}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-medium mono">₹{fmt(value)}</p>
                      <p className={'text-xs ' + (pnl >= 0 ? 'text-success' : 'text-danger')}>
                        {pnl >= 0 ? '+' : '-'}₹{fmt(Math.abs(pnl))}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* recent orders */}
        <div className="panel">
          <div className="panel-h">
            <h3>Recent Orders</h3>
            <Link to="/me/orders" className="text-sm text-primary">View all</Link>
          </div>
          {recentOrders.length === 0 ? (
            <div className="panel-b text-center text-text-2 py-8 text-sm">No orders yet.</div>
          ) : (
            <div className="panel-b text-sm space-y-0">
              {recentOrders.map((o: any) => {
                const sec = secMap[o.securityId];
                return (
                  <div key={o.orderId} className="flex items-center justify-between py-2.5 border-b border-border-hairline last:border-0">
                    <div>
                      <p className="font-medium mono">{sec ? sec.symbol : '#' + o.securityId}</p>
                      <p className="text-xs text-text-2">
                        {o.side} · {o.quantity} units · {(o.orderDate || '').slice(0, 10)}
                      </p>
                    </div>
                    <span className={'pill ' + getStatusPill(o.status)}>{o.status}</span>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* notifications preview */}
      <div className="panel">
        <div className="panel-h">
          <h3>Unread Notifications {unreadNotifs.length > 0 && <span className="pill pill-danger ml-2">{unreadNotifs.length}</span>}</h3>
          <Link to="/me/notifications" className="text-sm text-primary">View all</Link>
        </div>
        {unreadNotifs.length === 0 ? (
          <div className="panel-b text-center text-text-2 py-6 text-sm">All caught up! No unread notifications.</div>
        ) : (
          <div className="panel-b space-y-0">
            {unreadNotifs.map((n: any) => (
              <div key={n.notificationId} className="flex items-start gap-3 py-3 border-b border-border-hairline last:border-0">
                <div className="w-2 h-2 rounded-full bg-primary mt-1.5 shrink-0" />
                <div className="flex-1 min-w-0">
                  <p className="text-sm">{n.message}</p>
                  <p className="text-xs text-text-3 mt-0.5">
                    {(n.createdDate || '').replace('T', ' ').slice(0, 16)}
                  </p>
                </div>
                <span className="pill pill-info text-xs shrink-0">{n.category}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
