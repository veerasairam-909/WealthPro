import { useState, useEffect } from 'react';
import { useAuth } from '@/auth/store';
import { getAccountsByClientId } from '@/api/accounts';
import { getHoldingsByAccountId } from '@/api/holdings';
import { getBalanceByAccountId } from '@/api/cashLedger';
import { getAllSecurities } from '@/api/securities';
import { placeOrder } from '@/api/orders';
import { TableSkeleton, CardSkeleton } from '@/components/Skeleton';
import EmptyState from '@/components/EmptyState';
import { AlertTriangle, Landmark, Inbox } from 'lucide-react';
import {
  PieChart, Pie, Cell, Sector, Tooltip, Legend, ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, LabelList,
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

export default function Holdings() {
  const user = useAuth((s) => s.user);
  const clientId = user?.clientId;

  const [loading, setLoading] = useState(true);
  const [account, setAccount] = useState<any>(null);
  const [holdings, setHoldings] = useState<any[]>([]);
  const [secMap, setSecMap] = useState<{ [id: number]: any }>({});
  const [cashBalance, setCashBalance] = useState<number | null>(null);
  const [pborDown, setPborDown] = useState(false);
  const [activeAllocIdx, setActiveAllocIdx] = useState(-1);

  // sell modal state
  const [sellHolding, setSellHolding] = useState<any>(null);
  const [sellQty, setSellQty] = useState('');
  const [sellOrderType, setSellOrderType] = useState('MARKET');
  const [sellPrice, setSellPrice] = useState('');
  const [sellError, setSellError] = useState('');
  const [sellSubmitting, setSellSubmitting] = useState(false);
  const [sellSuccess, setSellSuccess] = useState('');

  useEffect(() => {
    if (clientId) loadAll();
  }, [clientId]);

  async function loadAll() {
    if (!clientId) return;
    setLoading(true);
    setPborDown(false);
    try {
      const [accsRes, secsRes] = await Promise.allSettled([
        getAccountsByClientId(clientId),
        getAllSecurities(),
      ]);

      if (secsRes.status === 'fulfilled' && Array.isArray(secsRes.value)) {
        const map: { [id: number]: any } = {};
        for (let i = 0; i < secsRes.value.length; i++) {
          map[secsRes.value[i].securityId] = secsRes.value[i];
        }
        setSecMap(map);
      }

      if (accsRes.status === 'fulfilled' && Array.isArray(accsRes.value) && accsRes.value.length > 0) {
        const acc = accsRes.value[0];
        setAccount(acc);

        const [holdRes, balRes] = await Promise.allSettled([
          getHoldingsByAccountId(acc.accountId),
          getBalanceByAccountId(acc.accountId),
        ]);

        if (holdRes.status === 'fulfilled' && Array.isArray(holdRes.value)) {
          setHoldings(holdRes.value);
        } else {
          setPborDown(true);
        }
        if (balRes.status === 'fulfilled') {
          setCashBalance(Number(balRes.value) || 0);
        }
      } else if (accsRes.status === 'rejected') {
        setPborDown(true);
      }
    } catch (e) {
    }
    setLoading(false);
  }

  function fmt(n: number) {
    return n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  // portfolio totals
  let totalValue = 0;
  let totalInvested = 0;
  for (let i = 0; i < holdings.length; i++) {
    const h = holdings[i];
    const sec = secMap[h.securityId];
    const qty = Number(h.quantity) || 0;
    const avgCost = Number(h.avgCost) || 0;
    const price = sec?.currentPrice ? Number(sec.currentPrice) : avgCost;
    totalValue += qty * price;
    totalInvested += qty * avgCost;
  }
  const totalPnl = totalValue - totalInvested;
  const pnlPct = totalInvested > 0 ? (totalPnl / totalInvested) * 100 : 0;

  // chart data
  const allocationMap: Record<string, number> = {};
  for (let i = 0; i < holdings.length; i++) {
    const h = holdings[i];
    const sec = secMap[h.securityId];
    const qty = Number(h.quantity) || 0;
    const avgCost = Number(h.avgCost) || 0;
    const price = sec?.currentPrice ? Number(sec.currentPrice) : avgCost;
    const mv = qty * price;
    const cls = sec?.assetClass ?? 'OTHER';
    allocationMap[cls] = (allocationMap[cls] || 0) + mv;
  }
  const allocationData = Object.entries(allocationMap).map(([name, value]) => ({
    name: name.replace('_', ' '),
    value: Math.round(value),
    raw: name,
  }));

  // Top 5 holdings with both invested + currentValue for grouped bar
  const topHoldingsData = [...holdings]
    .map((h) => {
      const sec = secMap[h.securityId];
      const qty = Number(h.quantity) || 0;
      const avgCost = Number(h.avgCost) || 0;
      const price = sec?.currentPrice ? Number(sec.currentPrice) : avgCost;
      const invested = Math.round(qty * avgCost);
      const currentValue = Math.round(qty * price);
      const pnl = currentValue - invested;
      const pct = avgCost > 0 ? ((price - avgCost) / avgCost) * 100 : 0;
      return {
        name: sec?.symbol ?? '#' + h.securityId,
        invested,
        currentValue,
        pnl,
        pct,
        assetClass: sec?.assetClass ?? 'OTHER',
      };
    })
    .sort((a, b) => b.currentValue - a.currentValue)
    .slice(0, 5);

  // sell modal helpers
  function openSellModal(h: any) {
    setSellHolding(h);
    setSellQty('');
    setSellOrderType('MARKET');
    setSellPrice('');
    setSellError('');
    setSellSuccess('');
  }

  function closeSellModal() {
    setSellHolding(null);
    setSellError('');
    setSellSuccess('');
  }

  async function submitSell(e: React.FormEvent) {
    e.preventDefault();
    if (!clientId || !sellHolding) return;

    const maxQty = Number(sellHolding.quantity) || 0;
    const qty = parseInt(sellQty, 10);
    if (sellQty.trim() === '' || isNaN(qty) || qty <= 0) {
      setSellError('Quantity must be a positive whole number');
      return;
    }
    if (qty > maxQty) {
      setSellError(`You only hold ${maxQty} units — cannot sell more than you own`);
      return;
    }

    const sec = secMap[sellHolding.securityId];
    const isMF = sec?.assetClass === 'MUTUAL_FUND';

    let limitPrice: number | null = null;
    if (!isMF && sellOrderType === 'LIMIT') {
      const p = parseFloat(sellPrice);
      if (sellPrice.trim() === '' || isNaN(p) || p <= 0) {
        setSellError('Enter a valid limit price greater than zero');
        return;
      }
      if (p > 1_000_000) {
        setSellError('Limit price cannot exceed ₹10,00,000');
        return;
      }
      if (!/^\d+(\.\d{1,2})?$/.test(sellPrice.trim())) {
        setSellError('Limit price can have at most 2 decimal places');
        return;
      }
      limitPrice = p;
    }

    const side = isMF ? 'REDEEM' : 'SELL';
    const priceType = isMF ? 'NAV' : (sellOrderType === 'LIMIT' ? 'LIMIT' : 'MARKET');

    setSellError('');
    setSellSubmitting(true);
    try {
      await placeOrder({
        clientId,
        securityId: sellHolding.securityId,
        side,
        quantity: qty,
        priceType,
        limitPrice,
      });
      setSellSuccess('Order placed! Track it in My Orders.');
      setTimeout(() => closeSellModal(), 1500);
    } catch (err: any) {
      const data = err.response?.data;
      const msg = typeof data === 'string' ? data
        : (data?.message || 'Failed to place order. Please try again.');
      setSellError(msg);
    }
    setSellSubmitting(false);
  }

  if (!clientId) return <CardSkeleton count={4} />;

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-1">My Holdings</h1>
      <p className="text-sm text-text-2 mb-5">
        Your current investment portfolio. Prices are last known market prices.
      </p>

      {/* summary KPIs */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-5">
        <div className="panel">
          <div className="panel-b">
            <p className="label">Market Value</p>
            <p className="text-2xl font-semibold mono mt-1">₹{fmt(totalValue)}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Invested</p>
            <p className="text-2xl font-semibold mono mt-1">₹{fmt(totalInvested)}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Overall P&L</p>
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
              {cashBalance !== null ? '₹' + fmt(cashBalance) : <span className="text-text-3 text-sm">—</span>}
            </p>
          </div>
        </div>
      </div>

      {/* account info strip */}
      {account && (
        <div className="panel mb-4">
          <div className="panel-b py-3 flex gap-6 text-sm">
            <div><span className="label mr-2">Account</span><span className="mono font-medium">{account.accountId}</span></div>
            <div><span className="label mr-2">Type</span><span className="font-medium">{account.accountType}</span></div>
            <div><span className="label mr-2">Currency</span><span className="font-medium">{account.baseCurrency}</span></div>
            <div><span className="label mr-2">Status</span><span className={'pill ' + (account.status === 'ACTIVE' ? 'pill-success' : 'pill-warn')}>{account.status}</span></div>
          </div>
        </div>
      )}

      {/* ── Charts ── */}
      {!loading && holdings.length > 0 && (
        <div className="grid md:grid-cols-2 gap-4 mb-4">

          {/* ── Donut: Asset Allocation ── */}
          <div className="panel">
            <div className="panel-h">
              <h3>Asset Allocation</h3>
              <span className="text-xs text-text-2 mono font-medium">{fmtShort(totalValue)} total</span>
            </div>
            <div className="panel-b">
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
                      <p className="text-[10px] text-text-3 uppercase tracking-widest mb-0.5">Holdings</p>
                      <p className="text-sm font-bold mono text-text">{fmtShort(totalValue)}</p>
                      <p className={'text-xs font-medium mt-0.5 ' + (totalPnl >= 0 ? 'text-success' : 'text-danger')}>
                        {totalPnl >= 0 ? '▲ +' : '▼ '}{Math.abs(pnlPct).toFixed(1)}%
                      </p>
                    </div>
                  </div>
                )}
              </div>
              {/* Custom legend below */}
              <div className="flex flex-wrap justify-center gap-x-5 gap-y-2 pt-1 pb-1 border-t border-border-hairline mt-1">
                {allocationData.map((entry, idx) => {
                  const pct = totalValue > 0 ? ((entry.value / totalValue) * 100).toFixed(1) : '0';
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

          {/* ── Grouped Bar: Invested vs Current per holding ── */}
          <div className="panel">
            <div className="panel-h">
              <h3>Invested vs Current — Top 5</h3>
              <span className={'text-xs font-semibold ' + (totalPnl >= 0 ? 'text-success' : 'text-danger')}>
                {totalPnl >= 0 ? '▲ +' : '▼ '}{Math.abs(pnlPct).toFixed(2)}% overall
              </span>
            </div>
            <div className="panel-b">
              <ResponsiveContainer width="100%" height={270}>
                <BarChart
                  data={topHoldingsData}
                  margin={{ top: 20, right: 16, left: 4, bottom: 4 }}
                  barGap={3}
                  barCategoryGap="28%"
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" vertical={false} />
                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 11, fontFamily: 'monospace' }}
                    axisLine={false} tickLine={false}
                  />
                  <YAxis
                    tick={{ fontSize: 11 }}
                    axisLine={false} tickLine={false}
                    tickFormatter={fmtShort}
                    domain={['auto', (max: number) => max * 1.14]}
                  />
                  <Tooltip
                    cursor={{ fill: '#f9fafb' }}
                    content={({ active, payload }: any) => {
                      if (!active || !payload?.length) return null;
                      const d = payload[0].payload;
                      return (
                        <div className="bg-white border border-border rounded-lg shadow-lg px-4 py-3 text-sm min-w-[190px]">
                          <p className="font-semibold mono mb-0.5">{d.name}</p>
                          <p className="text-xs text-text-3 mb-2">{d.assetClass.replace('_', ' ')}</p>
                          <div className="space-y-1 text-xs">
                            <div className="flex justify-between gap-4">
                              <span className="text-text-3">Invested</span>
                              <span className="mono font-medium">₹{d.invested.toLocaleString('en-IN')}</span>
                            </div>
                            <div className="flex justify-between gap-4">
                              <span className="text-text-3">Current</span>
                              <span className="mono font-medium">₹{d.currentValue.toLocaleString('en-IN')}</span>
                            </div>
                            <div className={'flex justify-between gap-4 pt-1 border-t border-border-hairline font-semibold ' +
                                (d.pnl >= 0 ? 'text-success' : 'text-danger')}>
                              <span>P&L</span>
                              <span className="mono">
                                {d.pnl >= 0 ? '+' : '–'}₹{Math.abs(d.pnl).toLocaleString('en-IN')}
                                {' '}({d.pct >= 0 ? '+' : ''}{d.pct.toFixed(2)}%)
                              </span>
                            </div>
                          </div>
                        </div>
                      );
                    }}
                  />
                  <Legend
                    iconType="circle"
                    iconSize={8}
                    formatter={(value: any) => (
                      <span style={{ fontSize: 12, color: '#6b7280' }}>{value}</span>
                    )}
                  />
                  {/* Invested bar — always slate */}
                  <Bar dataKey="invested" name="Invested" fill="#94a3b8" radius={[4, 4, 0, 0]} maxBarSize={32}>
                    <LabelList
                      dataKey="invested"
                      position="top"
                      formatter={(v: any) => fmtShort(Number(v))}
                      style={{ fontSize: 10, fill: '#9ca3af' }}
                    />
                  </Bar>
                  {/* Current value bar — green if gain, red if loss */}
                  <Bar dataKey="currentValue" name="Current Value" radius={[4, 4, 0, 0]} maxBarSize={32}>
                    {topHoldingsData.map((entry) => (
                      <Cell key={entry.name} fill={entry.pnl >= 0 ? '#22c55e' : '#ef4444'} opacity={0.9} />
                    ))}
                    <LabelList
                      dataKey="currentValue"
                      position="top"
                      formatter={(v: any) => fmtShort(Number(v))}
                      style={{ fontSize: 10, fill: '#374151', fontWeight: 600 }}
                    />
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

        </div>
      )}

      {/* holdings table */}
      <div className="panel">
        {loading ? (
          <TableSkeleton rows={5} cols={9} />
        ) : pborDown ? (
          <EmptyState
            icon={<AlertTriangle size={26} />}
            title="Portfolio service unavailable"
            description="PBOR service is not running. Ask your administrator."
          />
        ) : !account ? (
          <EmptyState
            icon={<Landmark size={26} />}
            title="No investment account yet"
            description="Your RM will set up your account during onboarding."
          />
        ) : holdings.length === 0 ? (
          <EmptyState
            icon={<Inbox size={26} />}
            title="No holdings yet"
            description="Your portfolio will appear here once your orders are filled and allocated."
          />
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Symbol</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Asset Class</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Qty</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Avg Cost</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Market Price</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Market Value</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">P&L</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">P&L %</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Action</th>
              </tr>
            </thead>
            <tbody>
              {holdings.map((h: any) => {
                const sec = secMap[h.securityId];
                const qty = Number(h.quantity) || 0;
                const avgCost = Number(h.avgCost) || 0;
                const price = sec?.currentPrice ? Number(sec.currentPrice) : avgCost;
                const value = qty * price;
                const pnl = (price - avgCost) * qty;
                const pct = avgCost > 0 ? ((price - avgCost) / avgCost) * 100 : 0;
                const isGain = pnl >= 0;
                const isMF = sec?.assetClass === 'MUTUAL_FUND';
                return (
                  <tr key={h.holdingId} className="border-t border-border-hairline hover:bg-surface">
                    <td className="px-5 py-3">
                      <p className="font-medium mono">{sec ? sec.symbol : '#' + h.securityId}</p>
                      <p className="text-xs text-text-3">{h.valuationCurrency || 'INR'}</p>
                    </td>
                    <td className="px-5 py-3 text-xs text-text-2">{sec ? sec.assetClass : '—'}</td>
                    <td className="px-5 py-3 mono text-right">{qty}</td>
                    <td className="px-5 py-3 mono text-right">₹{fmt(avgCost)}</td>
                    <td className="px-5 py-3 mono text-right">
                      {sec?.currentPrice ? '₹' + fmt(price) : <span className="text-text-3">NAV</span>}
                    </td>
                    <td className="px-5 py-3 mono text-right font-medium">₹{fmt(value)}</td>
                    <td className={'px-5 py-3 mono text-right font-medium ' + (isGain ? 'text-success' : 'text-danger')}>
                      {isGain ? '+' : '-'}₹{fmt(Math.abs(pnl))}
                    </td>
                    <td className={'px-5 py-3 mono text-right ' + (isGain ? 'text-success' : 'text-danger')}>
                      {isGain ? '+' : ''}{pct.toFixed(2)}%
                    </td>
                    <td className="px-5 py-3 text-right">
                      <button
                        onClick={() => openSellModal(h)}
                        className="btn btn-danger btn-sm"
                        disabled={qty <= 0}
                      >
                        {isMF ? 'Redeem' : 'Sell'}
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
            <tfoot className="bg-surface border-t-2 border-border">
              <tr>
                <td colSpan={5} className="px-5 py-3 text-xs font-medium text-text-2 uppercase">Total</td>
                <td className="px-5 py-3 mono text-right font-semibold">₹{fmt(totalValue)}</td>
                <td className={'px-5 py-3 mono text-right font-semibold ' + (totalPnl >= 0 ? 'text-success' : 'text-danger')}>
                  {totalPnl >= 0 ? '+' : '-'}₹{fmt(Math.abs(totalPnl))}
                </td>
                <td className={'px-5 py-3 mono text-right ' + (pnlPct >= 0 ? 'text-success' : 'text-danger')}>
                  {pnlPct >= 0 ? '+' : ''}{pnlPct.toFixed(2)}%
                </td>
                <td />
              </tr>
            </tfoot>
          </table>
        )}
      </div>

      {/* ── Sell / Redeem modal ── */}
      {sellHolding && (() => {
        const sec = secMap[sellHolding.securityId];
        const maxQty = Number(sellHolding.quantity) || 0;
        const isMF = sec?.assetClass === 'MUTUAL_FUND';
        const price = sec?.currentPrice ? Number(sec.currentPrice) : null;
        return (
          <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg max-w-md w-full">
              <div className="px-6 py-4 border-b border-border-hairline flex items-center justify-between">
                <div>
                  <h3 className="font-semibold">
                    {isMF ? 'Redeem ' : 'Sell '}
                    <span className="mono">{sec ? sec.symbol : '#' + sellHolding.securityId}</span>
                  </h3>
                  <p className="text-xs text-text-2">
                    {sec?.assetClass} · Available: <span className="mono font-medium">{maxQty} units</span>
                  </p>
                </div>
                <button onClick={closeSellModal} className="text-text-3 text-xl">×</button>
              </div>

              <form onSubmit={submitSell} className="p-6">

                {/* price info row */}
                <div className="bg-surface rounded-lg px-4 py-3 mb-4 flex items-center justify-between">
                  <span className="text-xs text-text-2 font-medium">
                    {isMF ? 'Price type' : 'Market price'}
                  </span>
                  {isMF ? (
                    <span className="text-sm font-semibold text-text-2">NAV (end of day)</span>
                  ) : price ? (
                    <span className="text-lg font-semibold mono">
                      ₹{price.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </span>
                  ) : (
                    <span className="text-sm text-text-3">Price at execution</span>
                  )}
                </div>

                <div className="mb-3">
                  <label className="label block mb-1">
                    Quantity <span className="text-text-3 font-normal">(max {maxQty})</span>
                  </label>
                  <input
                    className="input mono"
                    type="number"
                    min="1"
                    max={maxQty}
                    placeholder={`e.g. ${maxQty}`}
                    value={sellQty}
                    onChange={(e) => setSellQty(e.target.value)}
                    autoFocus
                  />
                </div>

                {/* estimated proceeds */}
                {price && sellQty && parseInt(sellQty) > 0 && (
                  <p className="text-xs text-text-2 mb-3">
                    Estimated proceeds:{' '}
                    <span className="font-semibold mono text-text">
                      ₹{(price * parseInt(sellQty)).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </span>
                    <span className="text-text-3 ml-1">(indicative — actual fill price may differ)</span>
                  </p>
                )}

                {/* order type toggle — not for mutual funds */}
                {!isMF && (
                  <div className="mb-3">
                    <label className="label block mb-1">Order type</label>
                    <div className="flex gap-2">
                      <button
                        type="button"
                        onClick={() => setSellOrderType('MARKET')}
                        className={
                          'flex-1 py-2 px-3 text-sm rounded border font-medium ' +
                          (sellOrderType === 'MARKET'
                            ? 'bg-primary text-white border-primary'
                            : 'bg-white text-text border-border')
                        }
                      >
                        Market
                      </button>
                      <button
                        type="button"
                        onClick={() => setSellOrderType('LIMIT')}
                        className={
                          'flex-1 py-2 px-3 text-sm rounded border font-medium ' +
                          (sellOrderType === 'LIMIT'
                            ? 'bg-primary text-white border-primary'
                            : 'bg-white text-text border-border')
                        }
                      >
                        Limit
                      </button>
                    </div>
                  </div>
                )}

                {!isMF && sellOrderType === 'LIMIT' && (
                  <div className="mb-3">
                    <label className="label block mb-1">Limit price (₹)</label>
                    <input
                      className="input mono"
                      type="number"
                      step="0.05"
                      min="0.01"
                      placeholder="e.g. 1648.50"
                      value={sellPrice}
                      onChange={(e) => setSellPrice(e.target.value)}
                    />
                  </div>
                )}

                {isMF && (
                  <p className="text-xs text-text-3 mb-3">
                    ⓘ Mutual fund redemptions are processed at end-of-day NAV price.
                  </p>
                )}

                {sellError && (
                  <div className="pill pill-danger block mb-3 text-center w-full">{sellError}</div>
                )}
                {sellSuccess && (
                  <div className="pill pill-success block mb-3 text-center w-full">{sellSuccess}</div>
                )}

                <div className="flex justify-end gap-2 mt-4">
                  <button type="button" onClick={closeSellModal} className="btn btn-ghost">
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-danger" disabled={sellSubmitting}>
                    {sellSubmitting ? 'Placing...' : (isMF ? 'Redeem' : 'Sell')}
                  </button>
                </div>
              </form>
            </div>
          </div>
        );
      })()}
    </div>
  );
}
