import { useState, useEffect } from 'react';
import { getAllClients } from '@/api/clients';
import { getAccountsByClientId } from '@/api/accounts';
import { getPerformanceByAccount, getRiskMeasuresByAccount } from '@/api/analytics';
import { CardSkeleton } from '@/components/Skeleton';
import EmptyState from '@/components/EmptyState';
import { Activity, TrendingUp, TrendingDown } from 'lucide-react';
import {
  ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, Legend, ReferenceLine,
} from 'recharts';

// ── Helpers ──────────────────────────────────────────────────────────────────

function fmtPct(v: number | null | undefined) {
  if (v == null) return '—';
  return (v >= 0 ? '+' : '') + Number(v).toFixed(2) + '%';
}

function measureLabel(type: string) {
  const MAP: Record<string, string> = {
    VOLATILITY:    'Volatility (σ)',
    DRAWDOWN:      'Max Drawdown',
    TRACKINGERROR: 'Tracking Error',
    VARPROXY:      'VaR Proxy',
  };
  return MAP[type] || type;
}

function measureColor(type: string) {
  const MAP: Record<string, string> = {
    VOLATILITY:    '#387ED1',
    DRAWDOWN:      '#EB5B3C',
    TRACKINGERROR: '#F4A41E',
    VARPROXY:      '#a855f7',
  };
  return MAP[type] || '#94a3b8';
}

function periodLabel(period: string, asOf: string) {
  if (!period && !asOf) return '—';
  return period ? period : asOf?.slice(0, 7);
}

// Custom tooltip for the chart
function PerfTooltip({ active, payload, label }: any) {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-white border border-border rounded-lg shadow-lg px-4 py-3 text-sm min-w-[180px]">
      <p className="font-semibold mb-1">{label}</p>
      {payload.map((p: any) => (
        <div key={p.dataKey} className="flex justify-between gap-4 text-xs">
          <span style={{ color: p.color }}>{p.name}</span>
          <span className="font-medium mono">{fmtPct(p.value)}</span>
        </div>
      ))}
    </div>
  );
}

// ── Component ─────────────────────────────────────────────────────────────────

export default function RmAnalytics() {
  const [clients, setClients]           = useState<any[]>([]);
  const [clientsLoading, setClientsLoading] = useState(true);

  const [selectedClientId, setSelectedClientId] = useState<number | ''>('');
  const [accounts, setAccounts]         = useState<any[]>([]);
  const [selectedAccountId, setSelectedAccountId] = useState<number | ''>('');

  const [perfRecords, setPerfRecords]   = useState<any[]>([]);
  const [riskMeasures, setRiskMeasures] = useState<any[]>([]);
  const [analyticsLoading, setAnalyticsLoading] = useState(false);
  const [analyticsError, setAnalyticsError] = useState('');

  // ── load client list ──────────────────────────────────────────────────────
  useEffect(() => {
    (async () => {
      try {
        const data = await getAllClients();
        if (Array.isArray(data)) setClients(data.filter((c: any) => c.status === 'Active'));
      } catch { /* ignore */ }
      setClientsLoading(false);
    })();
  }, []);

  // ── when client changes: load accounts ───────────────────────────────────
  useEffect(() => {
    if (!selectedClientId) { setAccounts([]); setSelectedAccountId(''); return; }
    (async () => {
      try {
        const data = await getAccountsByClientId(selectedClientId as number);
        if (Array.isArray(data) && data.length > 0) {
          setAccounts(data);
          setSelectedAccountId(data[0].accountId);
        } else {
          setAccounts([]);
          setSelectedAccountId('');
        }
      } catch {
        setAccounts([]);
        setSelectedAccountId('');
      }
    })();
  }, [selectedClientId]);

  // ── when account changes: load analytics ──────────────────────────────────
  useEffect(() => {
    if (!selectedAccountId) {
      setPerfRecords([]);
      setRiskMeasures([]);
      return;
    }
    loadAnalytics(selectedAccountId as number);
  }, [selectedAccountId]);

  async function loadAnalytics(accountId: number) {
    setAnalyticsLoading(true);
    setAnalyticsError('');
    try {
      const [perfRes, riskRes] = await Promise.allSettled([
        getPerformanceByAccount(accountId),
        getRiskMeasuresByAccount(accountId),
      ]);

      if (perfRes.status === 'fulfilled' && Array.isArray(perfRes.value)) {
        // sort chronologically
        const sorted = [...perfRes.value].sort((a, b) =>
          (a.asOfDate || '').localeCompare(b.asOfDate || '')
        );
        setPerfRecords(sorted);
      } else {
        setPerfRecords([]);
      }

      if (riskRes.status === 'fulfilled' && Array.isArray(riskRes.value)) {
        // pick the latest record per measure type
        const latestMap: Record<string, any> = {};
        for (const r of riskRes.value) {
          const existing = latestMap[r.measureType];
          if (!existing || (r.asOfDate || '') > (existing.asOfDate || '')) {
            latestMap[r.measureType] = r;
          }
        }
        setRiskMeasures(Object.values(latestMap));
      } else {
        setRiskMeasures([]);
      }
    } catch {
      setAnalyticsError('Analytics service unavailable. Ensure the analytics microservice is running.');
    }
    setAnalyticsLoading(false);
  }

  // ── chart data ────────────────────────────────────────────────────────────
  const chartData = perfRecords.map((r) => ({
    period:    periodLabel(r.period, r.asOfDate),
    Return:    r.returnPct    != null ? Number(r.returnPct)    : null,
    Benchmark: r.benchmarkPct != null ? Number(r.benchmarkPct) : null,
  }));

  // summary stats
  const latestPerf = perfRecords[perfRecords.length - 1];
  const avgReturn   = perfRecords.length
    ? perfRecords.reduce((s, r) => s + (Number(r.returnPct) || 0), 0) / perfRecords.length
    : null;
  const avgBench    = perfRecords.length && perfRecords.some((r) => r.benchmarkPct != null)
    ? perfRecords.filter((r) => r.benchmarkPct != null).reduce((s, r) => s + Number(r.benchmarkPct), 0) /
      perfRecords.filter((r) => r.benchmarkPct != null).length
    : null;
  const alpha = avgReturn != null && avgBench != null ? avgReturn - avgBench : null;

  const selectedClient = clients.find((c) => c.clientId === selectedClientId);

  return (
    <div>
      {/* header */}
      <div className="mb-5">
        <h1 className="text-2xl font-semibold mb-1">Analytics Dashboard</h1>
        <p className="text-sm text-text-2">
          Performance vs benchmark, risk measures, and exposure analysis per client account.
        </p>
      </div>

      {/* ── Client / Account selector ── */}
      <div className="panel mb-5">
        <div className="panel-h"><h3>Select Client Account</h3></div>
        <div className="panel-b">
          <div className="grid md:grid-cols-2 gap-4">
            {/* client */}
            <div>
              <label className="label block mb-1">Client</label>
              {clientsLoading ? (
                <div className="input text-text-3 text-xs flex items-center">Loading clients...</div>
              ) : (
                <select
                  className="input"
                  value={selectedClientId}
                  onChange={(e) => {
                    setSelectedClientId(e.target.value ? Number(e.target.value) : '');
                  }}
                >
                  <option value="">— Select a client —</option>
                  {clients.map((c) => (
                    <option key={c.clientId} value={c.clientId}>
                      {c.name} (ID: {c.clientId})
                    </option>
                  ))}
                </select>
              )}
              {selectedClient && (
                <p className="text-xs text-text-2 mt-1">
                  Segment: <span className="font-medium">{selectedClient.segment}</span>
                  {' · '}Status: <span className="font-medium">{selectedClient.status}</span>
                </p>
              )}
            </div>

            {/* account */}
            <div>
              <label className="label block mb-1">Account</label>
              {accounts.length === 0 ? (
                <div className="input text-text-3 text-xs flex items-center">
                  {selectedClientId ? 'No investment account found' : 'Select a client first'}
                </div>
              ) : (
                <select
                  className="input"
                  value={selectedAccountId}
                  onChange={(e) => setSelectedAccountId(e.target.value ? Number(e.target.value) : '')}
                >
                  {accounts.map((a) => (
                    <option key={a.accountId} value={a.accountId}>
                      Acc {a.accountId} — {a.accountType} ({a.baseCurrency})
                    </option>
                  ))}
                </select>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* ── No selection ── */}
      {!selectedAccountId && !analyticsLoading && (
        <div className="panel">
          <EmptyState
            icon={<Activity size={28} />}
            title="No account selected"
            description="Choose a client and their investment account above to view analytics."
          />
        </div>
      )}

      {/* ── Loading ── */}
      {analyticsLoading && <CardSkeleton count={4} />}

      {/* ── Error ── */}
      {analyticsError && !analyticsLoading && (
        <div className="panel">
          <div className="panel-b text-center py-10">
            <p className="font-semibold text-danger mb-1">Analytics Unavailable</p>
            <p className="text-sm text-text-2">{analyticsError}</p>
          </div>
        </div>
      )}

      {/* ── Analytics content ── */}
      {!analyticsLoading && !analyticsError && selectedAccountId && (
        <>
          {/* ── KPI summary strip ── */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-5">
            {/* Latest return */}
            <div className="panel">
              <div className="panel-b">
                <p className="label">Latest Return</p>
                {latestPerf ? (
                  <p className={'text-2xl font-semibold mono mt-1 ' +
                    (Number(latestPerf.returnPct) >= 0 ? 'text-success' : 'text-danger')}>
                    {fmtPct(latestPerf.returnPct)}
                  </p>
                ) : (
                  <p className="text-2xl font-semibold text-text-3 mt-1">—</p>
                )}
                <p className="text-xs text-text-3 mt-0.5">{latestPerf?.period || latestPerf?.asOfDate?.slice(0, 7) || 'no data'}</p>
              </div>
            </div>

            {/* Avg return */}
            <div className="panel">
              <div className="panel-b">
                <p className="label">Avg Return ({perfRecords.length} periods)</p>
                <p className={'text-2xl font-semibold mono mt-1 ' +
                  (avgReturn == null ? 'text-text-3' : avgReturn >= 0 ? 'text-success' : 'text-danger')}>
                  {fmtPct(avgReturn)}
                </p>
              </div>
            </div>

            {/* Avg benchmark */}
            <div className="panel">
              <div className="panel-b">
                <p className="label">Avg Benchmark</p>
                <p className="text-2xl font-semibold mono mt-1">
                  {fmtPct(avgBench)}
                </p>
              </div>
            </div>

            {/* Alpha */}
            <div className="panel">
              <div className="panel-b">
                <p className="label">Alpha (vs benchmark)</p>
                {alpha != null ? (
                  <div className="flex items-center gap-2 mt-1">
                    {alpha >= 0
                      ? <TrendingUp size={20} className="text-success" />
                      : <TrendingDown size={20} className="text-danger" />
                    }
                    <p className={'text-2xl font-semibold mono ' +
                      (alpha >= 0 ? 'text-success' : 'text-danger')}>
                      {fmtPct(alpha)}
                    </p>
                  </div>
                ) : (
                  <p className="text-2xl font-semibold text-text-3 mt-1">—</p>
                )}
                <p className="text-xs text-text-3 mt-0.5">Return − Benchmark</p>
              </div>
            </div>
          </div>

          {/* ── Performance vs Benchmark chart ── */}
          <div className="panel mb-5">
            <div className="panel-h">
              <h3>Performance vs Benchmark</h3>
              <span className="text-xs text-text-2">{perfRecords.length} periods · Account {selectedAccountId}</span>
            </div>
            <div className="panel-b">
              {chartData.length === 0 ? (
                <EmptyState
                  icon={<Activity size={24} />}
                  title="No performance records"
                  description="Performance records are created by the analytics service. Ensure the service has run a calculation for this account."
                />
              ) : (
                <ResponsiveContainer width="100%" height={280}>
                  <BarChart
                    data={chartData}
                    margin={{ top: 20, right: 16, left: 0, bottom: 4 }}
                    barGap={4}
                    barCategoryGap="30%"
                  >
                    <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" vertical={false} />
                    <XAxis
                      dataKey="period"
                      tick={{ fontSize: 11, fontFamily: 'monospace' }}
                      axisLine={false}
                      tickLine={false}
                    />
                    <YAxis
                      tick={{ fontSize: 11 }}
                      axisLine={false}
                      tickLine={false}
                      tickFormatter={(v) => v + '%'}
                    />
                    <ReferenceLine y={0} stroke="#E5E7EB" />
                    <Tooltip content={<PerfTooltip />} />
                    <Legend
                      iconType="circle"
                      iconSize={8}
                      formatter={(v: any) => <span style={{ fontSize: 12, color: '#6b7280' }}>{v}</span>}
                    />
                    <Bar dataKey="Return"    name="Portfolio Return" fill="#387ED1" radius={[4, 4, 0, 0]} maxBarSize={36} />
                    <Bar dataKey="Benchmark" name="Benchmark"        fill="#94a3b8" radius={[4, 4, 0, 0]} maxBarSize={36} />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>

          {/* ── Risk Measures ── */}
          <div className="panel mb-5">
            <div className="panel-h">
              <h3>Risk Measures</h3>
              <span className="text-xs text-text-2">Latest value per measure type</span>
            </div>
            <div className="panel-b">
              {riskMeasures.length === 0 ? (
                <EmptyState
                  icon={<Activity size={24} />}
                  title="No risk measures yet"
                  description="Risk measures (Volatility, Drawdown, TrackingError, VaR) are calculated by the analytics service."
                />
              ) : (
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  {riskMeasures.map((rm: any) => {
                    const color = measureColor(rm.measureType);
                    const val   = rm.value != null ? Number(rm.value).toFixed(4) : '—';
                    return (
                      <div key={rm.riskId} className="bg-surface rounded-lg p-4 border border-border-hairline">
                        <div
                          className="w-8 h-8 rounded-full flex items-center justify-center mb-3"
                          style={{ background: color + '20' }}
                        >
                          <Activity size={16} style={{ color }} />
                        </div>
                        <p className="text-xs text-text-2 mb-1">{measureLabel(rm.measureType)}</p>
                        <p className="text-xl font-bold mono" style={{ color }}>{val}</p>
                        {rm.asOfDate && (
                          <p className="text-xs text-text-3 mt-1">as of {rm.asOfDate.slice(0, 10)}</p>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>

          {/* ── Full performance table ── */}
          {perfRecords.length > 0 && (
            <div className="panel">
              <div className="panel-h">
                <h3>Performance History</h3>
                <span className="text-xs text-text-2">{perfRecords.length} records</span>
              </div>
              <table className="w-full text-sm">
                <thead className="bg-surface">
                  <tr>
                    <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Period</th>
                    <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">As Of Date</th>
                    <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Return %</th>
                    <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Benchmark %</th>
                    <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Alpha</th>
                  </tr>
                </thead>
                <tbody>
                  {[...perfRecords].reverse().map((r: any) => {
                    const ret   = r.returnPct    != null ? Number(r.returnPct)    : null;
                    const bench = r.benchmarkPct != null ? Number(r.benchmarkPct) : null;
                    const al    = ret != null && bench != null ? ret - bench : null;
                    return (
                      <tr key={r.perfId} className="border-t border-border-hairline hover:bg-surface">
                        <td className="px-5 py-3 font-medium">{r.period || '—'}</td>
                        <td className="px-5 py-3 mono text-xs text-text-2">{r.asOfDate?.slice(0, 10) || '—'}</td>
                        <td className={'px-5 py-3 mono text-right font-medium ' +
                          (ret == null ? 'text-text-3' : ret >= 0 ? 'text-success' : 'text-danger')}>
                          {fmtPct(ret)}
                        </td>
                        <td className="px-5 py-3 mono text-right text-text-2">{fmtPct(bench)}</td>
                        <td className={'px-5 py-3 mono text-right font-medium ' +
                          (al == null ? 'text-text-3' : al >= 0 ? 'text-success' : 'text-danger')}>
                          {fmtPct(al)}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  );
}
