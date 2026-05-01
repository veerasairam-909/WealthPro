import { useState, useEffect } from 'react';
import { useAuth } from '@/auth/store';
import { getAccountsByClientId } from '@/api/accounts';
import { getHoldingsByAccountId } from '@/api/holdings';
import { getBalanceByAccountId } from '@/api/cashLedger';
import { getAllSecurities } from '@/api/securities';
import { getGoalsByClientId } from '@/api/goals';
import { getRecommendationsByClientId } from '@/api/recommendations';

export default function Reviews() {
  const user = useAuth((s) => s.user);
  const clientId = user?.clientId;

  const [portfolioValue, setPortfolioValue]   = useState(0);
  const [cashBalance, setCashBalance]         = useState(0);
  const [totalInvested, setTotalInvested]     = useState(0);
  const [goals, setGoals]                     = useState<any[]>([]);
  const [holdingsCount, setHoldingsCount]     = useState(0);
  const [recommendations, setRecommendations] = useState<any[]>([]);
  const [loading, setLoading]                 = useState(true);

  // expanded proposal view
  const [expandedId, setExpandedId] = useState<number | null>(null);

  useEffect(() => {
    if (clientId) loadAll();
  }, [clientId]);

  async function loadAll() {
    if (!clientId) return;
    setLoading(true);
    try {
      const [accsRes, secsRes, goalsRes, recoRes] = await Promise.allSettled([
        getAccountsByClientId(clientId),
        getAllSecurities(),
        getGoalsByClientId(clientId),
        getRecommendationsByClientId(clientId),
      ]);

      if (goalsRes.status === 'fulfilled' && Array.isArray(goalsRes.value)) {
        setGoals(goalsRes.value);
      }

      if (recoRes.status === 'fulfilled' && Array.isArray(recoRes.value)) {
        // newest first
        setRecommendations([...recoRes.value].sort((a, b) =>
          new Date(b.proposedDate || 0).getTime() - new Date(a.proposedDate || 0).getTime()
        ));
      }

      const secMap: { [id: number]: any } = {};
      if (secsRes.status === 'fulfilled' && Array.isArray(secsRes.value)) {
        for (const s of secsRes.value) secMap[s.securityId] = s;
      }

      if (accsRes.status === 'fulfilled' && Array.isArray(accsRes.value) && accsRes.value.length > 0) {
        const accountId = accsRes.value[0].accountId;

        const [holdingsRes, balanceRes] = await Promise.allSettled([
          getHoldingsByAccountId(accountId),
          getBalanceByAccountId(accountId),
        ]);

        if (holdingsRes.status === 'fulfilled' && Array.isArray(holdingsRes.value)) {
          let pv = 0, inv = 0;
          for (const h of holdingsRes.value) {
            const qty = Number(h.quantity) || 0;
            const avg = Number(h.avgCost) || 0;
            const price = secMap[h.securityId]?.currentPrice
              ? Number(secMap[h.securityId].currentPrice)
              : avg;
            pv  += qty * price;
            inv += qty * avg;
          }
          setPortfolioValue(pv);
          setTotalInvested(inv);
          setHoldingsCount(holdingsRes.value.length);
        }

        if (balanceRes.status === 'fulfilled' && balanceRes.value != null) {
          setCashBalance(Number(balanceRes.value) || 0);
        }
      }
    } catch (e) {
    }
    setLoading(false);
  }

  function fmt(n: number) {
    return n.toLocaleString('en-IN', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  }

  function fmtPct(n: number) {
    return (n >= 0 ? '+' : '') + n.toFixed(2) + '%';
  }

  function statusPill(s: string) {
    if (s === 'APPROVED') return 'pill-success';
    if (s === 'REJECTED') return 'pill-danger';
    return 'pill-warn';
  }

  function parseProposal(raw: string) {
    try { return JSON.parse(raw); } catch { return null; }
  }

  const pnl     = portfolioValue - totalInvested;
  const pnlPct  = totalInvested > 0 ? (pnl / totalInvested) * 100 : 0;
  const netWorth = portfolioValue + cashBalance;

  const activeGoals    = goals.filter((g) => g.status === 'ACTIVE');
  const completedGoals = goals.filter((g) => g.status === 'COMPLETED');

  const pendingRecos  = recommendations.filter((r) => r.status === 'PENDING');
  const resolvedRecos = recommendations.filter((r) => r.status !== 'PENDING');

  if (!clientId) return <div className="p-10 text-center text-text-2">Loading...</div>;

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Portfolio Reviews</h1>
          <p className="text-sm text-text-2">
            Your portfolio snapshot and recommendations sent by your relationship manager.
          </p>
        </div>
        <button
          onClick={() => window.print()}
          className="btn btn-ghost btn-sm"
          title="Save as PDF or print this review"
        >
          ⬇ Download Statement
        </button>
      </div>

      {loading ? (
        <div className="panel mb-5">
          <div className="panel-b text-center text-text-2 py-10">Loading data...</div>
        </div>
      ) : (
        <>
          {/* ── Portfolio snapshot ── */}
          <div className="panel mb-5">
            <div className="panel-h">
              <h3>Portfolio Snapshot</h3>
              <span className="text-xs text-text-2">
                as of {new Date().toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}
              </span>
            </div>
            <div className="panel-b">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mb-6">
                <div>
                  <p className="label">Net worth</p>
                  <p className="text-2xl font-bold mono mt-1">₹{fmt(netWorth)}</p>
                  <p className="text-xs text-text-3">investments + cash</p>
                </div>
                <div>
                  <p className="label">Portfolio value</p>
                  <p className="text-2xl font-semibold mono mt-1">₹{fmt(portfolioValue)}</p>
                  <p className="text-xs text-text-3">{holdingsCount} holdings</p>
                </div>
                <div>
                  <p className="label">Unrealised P&L</p>
                  <p className={'text-2xl font-semibold mono mt-1 ' + (pnl >= 0 ? 'text-success' : 'text-danger')}>
                    {pnl >= 0 ? '+' : ''}₹{fmt(Math.abs(pnl))}
                  </p>
                  <p className={'text-xs ' + (pnlPct >= 0 ? 'text-success' : 'text-danger')}>
                    {fmtPct(pnlPct)} overall
                  </p>
                </div>
                <div>
                  <p className="label">Cash balance</p>
                  <p className="text-2xl font-semibold mono mt-1">₹{fmt(cashBalance)}</p>
                  <p className="text-xs text-text-3">available to invest</p>
                </div>
              </div>

              {/* Goals progress */}
              {goals.length > 0 && (
                <div className="border-t border-border-hairline pt-4">
                  <p className="text-xs font-semibold text-text-2 uppercase mb-3">Goals Progress</p>
                  <div className="grid md:grid-cols-2 gap-3">
                    {goals.map((g: any) => {
                      const target = Number(g.targetAmount);
                      const pct = target > 0 ? Math.min(100, (portfolioValue / target) * 100) : 0;
                      return (
                        <div key={g.goalId} className="bg-surface rounded-lg p-3">
                          <div className="flex justify-between items-center mb-1.5">
                            <p className="text-sm font-medium">{g.goalType}</p>
                            <span className={
                              'text-xs font-medium ' +
                              (g.status === 'COMPLETED' ? 'text-success' : 'text-text-2')
                            }>
                              {g.status === 'COMPLETED' ? '✓ Completed' : pct.toFixed(1) + '%'}
                            </span>
                          </div>
                          <div className="w-full bg-border rounded-full h-1.5 mb-1">
                            <div
                              className={'h-1.5 rounded-full ' + (g.status === 'COMPLETED' ? 'bg-success' : 'bg-primary')}
                              style={{ width: pct + '%' }}
                            />
                          </div>
                          <p className="text-xs text-text-3">
                            Target: ₹{fmt(target)} · Due {g.targetDate}
                          </p>
                        </div>
                      );
                    })}
                  </div>
                  <p className="text-xs text-text-3 mt-2">
                    {activeGoals.length} active · {completedGoals.length} completed
                  </p>
                </div>
              )}
            </div>
          </div>

          {/* ── Pending recommendations ── */}
          <div className="panel mb-5">
            <div className="panel-h">
              <h3>Recommendations from your RM</h3>
              <span className="text-xs text-text-2">{recommendations.length} total</span>
            </div>

            {recommendations.length === 0 ? (
              <div className="panel-b text-center py-10">
                <p className="text-3xl mb-2">📬</p>
                <p className="font-semibold">No recommendations yet</p>
                <p className="text-sm text-text-2 mt-1">
                  Your relationship manager will send portfolio recommendations here.
                </p>
              </div>
            ) : (
              <div className="divide-y divide-border-hairline">
                {recommendations.map((r: any) => {
                  const proposal = parseProposal(r.proposalJson);
                  const isExpanded = expandedId === r.recoId;
                  return (
                    <div key={r.recoId} className="panel-b">
                      {/* header row */}
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 flex-wrap mb-1">
                            <span className={'pill ' + statusPill(r.status)}>{r.status}</span>
                            {r.riskClass && (
                              <span className="pill pill-info">{r.riskClass}</span>
                            )}
                          </div>
                          <p className="text-sm text-text-2">
                            Proposed on{' '}
                            <span className="font-medium text-text">
                              {r.proposedDate
                                ? new Date(r.proposedDate).toLocaleDateString('en-IN', {
                                    day: '2-digit', month: 'short', year: 'numeric',
                                  })
                                : '—'}
                            </span>
                          </p>
                          {/* quick summary from proposal */}
                          {proposal && (
                            <p className="text-sm text-text-2 mt-1 truncate">
                              {proposal.summary || proposal.title || proposal.description ||
                                (typeof proposal === 'object'
                                  ? Object.keys(proposal).slice(0, 3).map(k => `${k}: ${proposal[k]}`).join(' · ')
                                  : String(proposal))}
                            </p>
                          )}
                          {!proposal && r.proposalJson && (
                            <p className="text-sm text-text-2 mt-1 truncate font-mono text-xs">
                              {r.proposalJson.slice(0, 120)}{r.proposalJson.length > 120 ? '…' : ''}
                            </p>
                          )}
                        </div>
                        <button
                          onClick={() => setExpandedId(isExpanded ? null : r.recoId)}
                          className="btn btn-ghost btn-sm shrink-0"
                        >
                          {isExpanded ? 'Hide ▲' : 'View ▼'}
                        </button>
                      </div>

                      {/* expanded proposal detail */}
                      {isExpanded && (
                        <div className="mt-3 bg-surface rounded-lg p-4 text-sm">
                          {proposal ? (
                            <table className="w-full text-sm">
                              <tbody>
                                {Object.entries(proposal).map(([k, v]) => (
                                  <tr key={k} className="border-b border-border-hairline last:border-0">
                                    <td className="py-1.5 pr-4 label w-40 align-top">
                                      {/* preserve all-caps acronyms (SIP, ETF…), split camelCase otherwise */}
                                      {k === k.toUpperCase()
                                        ? k
                                        : k.replace(/([A-Z])/g, ' $1').trim().toLowerCase()
                                            .replace(/^\w/, (c) => c.toUpperCase())}
                                    </td>
                                    <td className="py-1.5 font-medium text-text">
                                      {Array.isArray(v) ? (
                                        v.join(', ')
                                      ) : typeof v === 'object' && v !== null ? (
                                        <div className="flex flex-wrap gap-2">
                                          {Object.entries(v as Record<string, any>).map(([asset, pct]) => (
                                            <span key={asset} className="inline-flex items-center gap-1 bg-white border border-border rounded px-2 py-0.5 text-xs">
                                              <span className="font-semibold text-text">{asset}</span>
                                              <span className="text-text-2">{String(pct)}%</span>
                                            </span>
                                          ))}
                                        </div>
                                      ) : (
                                        String(v)
                                      )}
                                    </td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          ) : (
                            <pre className="text-xs text-text-2 whitespace-pre-wrap break-all">
                              {r.proposalJson}
                            </pre>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* ── Summary counts ── */}
          {recommendations.length > 0 && (
            <div className="grid grid-cols-3 gap-4">
              <div className="panel">
                <div className="panel-b text-center">
                  <p className="text-2xl font-bold text-warn">{pendingRecos.length}</p>
                  <p className="label mt-1">Pending</p>
                </div>
              </div>
              <div className="panel">
                <div className="panel-b text-center">
                  <p className="text-2xl font-bold text-success">
                    {recommendations.filter((r) => r.status === 'APPROVED').length}
                  </p>
                  <p className="label mt-1">Approved</p>
                </div>
              </div>
              <div className="panel">
                <div className="panel-b text-center">
                  <p className="text-2xl font-bold text-danger">
                    {recommendations.filter((r) => r.status === 'REJECTED').length}
                  </p>
                  <p className="label mt-1">Rejected</p>
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
