import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllClients } from '@/api/clients';
import { getRecommendationsByClientId, updateRecommendationStatus } from '@/api/recommendations';
import { cachedFetch, parallelLimit } from '@/lib/fetchUtils';

interface RecoItem {
  client: any;
  reco: any;
}

const STATUSES = ['ALL', 'SUBMITTED', 'APPROVED', 'REJECTED', 'PENDING'];

export default function RmRecommendations() {
  const [items, setItems] = useState<RecoItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [updatingId, setUpdatingId] = useState<number | null>(null);

  useEffect(() => { loadAll(); }, []);

  async function loadAll() {
    setLoading(true);
    try {
      const clients = await cachedFetch('clients', getAllClients);
      if (!Array.isArray(clients)) { setLoading(false); return; }

      const recoResults = await parallelLimit(
        clients.map((c: any) => () => getRecommendationsByClientId(c.clientId))
      );

      const flat: RecoItem[] = [];
      for (let i = 0; i < clients.length; i++) {
        const r = recoResults[i];
        if (r.status === 'fulfilled' && Array.isArray(r.value)) {
          for (const reco of r.value) {
            flat.push({ client: clients[i], reco });
          }
        }
      }

      // Sort newest first
      flat.sort((a, b) =>
        (b.reco.proposedDate || '').localeCompare(a.reco.proposedDate || '')
      );

      setItems(flat);
    } catch (e) {
    }
    setLoading(false);
  }

  async function handleStatusChange(recoId: number, newStatus: string) {
    setUpdatingId(recoId);
    try {
      await updateRecommendationStatus(recoId, newStatus);
      // Update locally without full reload
      setItems((prev) =>
        prev.map((x) =>
          x.reco.recoId === recoId ? { ...x, reco: { ...x.reco, status: newStatus } } : x
        )
      );
    } catch (e) {
    }
    setUpdatingId(null);
  }

  function getStatusPill(s: string) {
    if (s === 'APPROVED') return 'pill-success';
    if (s === 'REJECTED') return 'pill-danger';
    if (s === 'SUBMITTED') return 'pill-info';
    return 'pill-warn';
  }

  function parseProposal(json: string): Record<string, any> | null {
    try { return JSON.parse(json); } catch { return null; }
  }

  function ProposalCard({ json }: { json: string }) {
    const p = parseProposal(json);

    // Fallback: unknown shape — just show readable text
    if (!p || typeof p !== 'object') {
      return <p className="text-sm text-text-2">{json}</p>;
    }

    // Known shape: { targetReturn, timeHorizon, SIP, allocation }
    const allocation: Record<string, number> | null =
      p.allocation && typeof p.allocation === 'object' ? p.allocation : null;

    const ALLOC_COLORS: Record<string, string> = {
      BOND:        'bg-blue-500',
      MUTUAL_FUND: 'bg-green-500',
      ETF:         'bg-purple-500',
      EQUITY:      'bg-orange-500',
      CASH:        'bg-gray-400',
    };

    return (
      <div className="mt-2 space-y-3">
        {/* Key metrics row */}
        <div className="flex flex-wrap gap-4">
          {p.targetReturn != null && (
            <div className="flex flex-col">
              <span className="text-xs text-text-3 uppercase tracking-wide">Target Return</span>
              <span className="font-semibold text-success">{p.targetReturn}% p.a.</span>
            </div>
          )}
          {p.timeHorizon && (
            <div className="flex flex-col">
              <span className="text-xs text-text-3 uppercase tracking-wide">Time Horizon</span>
              <span className="font-semibold">{p.timeHorizon}</span>
            </div>
          )}
          {p.SIP != null && (
            <div className="flex flex-col">
              <span className="text-xs text-text-3 uppercase tracking-wide">Monthly SIP</span>
              <span className="font-semibold">₹{Number(p.SIP).toLocaleString('en-IN')}</span>
            </div>
          )}
        </div>

        {/* Allocation bar + legend */}
        {allocation && Object.keys(allocation).length > 0 && (
          <div>
            <span className="text-xs text-text-3 uppercase tracking-wide block mb-1">
              Asset Allocation
            </span>
            {/* Legend */}
            <div className="flex flex-wrap gap-x-4 gap-y-1">
              {Object.entries(allocation).map(([asset, pct]) => (
                <div key={asset} className="flex items-center gap-1.5 text-xs text-text-2">
                  <span
                    className={`inline-block w-2.5 h-2.5 rounded-sm ${ALLOC_COLORS[asset] ?? 'bg-gray-500'}`}
                  />
                  {asset.replace('_', ' ')} <span className="font-medium text-text-1">{pct}%</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Any other fields the backend might send */}
        {Object.entries(p)
          .filter(([k]) => !['targetReturn', 'timeHorizon', 'SIP', 'allocation'].includes(k))
          .map(([k, v]) => (
            <div key={k} className="text-sm text-text-2">
              <span className="font-medium capitalize">{k.replace(/([A-Z])/g, ' $1')}: </span>
              {typeof v === 'object' ? JSON.stringify(v) : String(v)}
            </div>
          ))}
      </div>
    );
  }

  const displayed = statusFilter === 'ALL'
    ? items
    : items.filter((x) => x.reco.status === statusFilter);

  const submittedCount = items.filter((x) => x.reco.status === 'SUBMITTED').length;

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">
            All Recommendations
            {submittedCount > 0 && (
              <span className="pill pill-warn ml-3">{submittedCount} pending review</span>
            )}
          </h1>
          <p className="text-sm text-text-2">
            Investment proposals across all clients — review, approve, or reject.
          </p>
        </div>
        <button onClick={loadAll} disabled={loading} className="btn btn-ghost btn-sm">
          {loading ? 'Loading...' : '↻ Refresh'}
        </button>
      </div>

      {/* Status filter */}
      <div className="panel mb-4">
        <div className="panel-b py-3 flex gap-2 items-center flex-wrap">
          {STATUSES.map((s) => {
            const count = s === 'ALL' ? items.length : items.filter((x) => x.reco.status === s).length;
            return (
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
                {s} ({count})
              </button>
            );
          })}
          <span className="ml-auto text-xs text-text-2">{displayed.length} showing</span>
        </div>
      </div>

      {/* Recommendations */}
      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading recommendations...</div>
        ) : displayed.length === 0 ? (
          <div className="panel-b text-center py-10">
            <p className="text-3xl mb-2">📋</p>
            <p className="font-semibold">
              {statusFilter === 'ALL' ? 'No recommendations yet' : `No ${statusFilter.toLowerCase()} recommendations`}
            </p>
            <p className="text-sm text-text-2 mt-1">
              {statusFilter === 'ALL'
                ? 'Open a client profile to create an investment recommendation.'
                : 'Change the filter to see other recommendations.'}
            </p>
          </div>
        ) : (
          <div className="divide-y divide-border-hairline">
            {displayed.map(({ client, reco }) => (
              <div key={reco.recoId} className="panel-b">
                <div className="flex items-start justify-between gap-4">
                  {/* left: client + reco details */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1.5 flex-wrap">
                      <span className="font-semibold">{client.name}</span>
                      <span className="text-xs text-text-2 mono">{client.clientId}</span>
                      <span className="text-text-3">·</span>
                      <span className="pill pill-info">{reco.riskClass}</span>
                      <span className={'pill ' + getStatusPill(reco.status)}>{reco.status}</span>
                      <span className="text-xs text-text-3 ml-auto">{reco.proposedDate}</span>
                    </div>
                    <ProposalCard json={reco.proposalJson} />
                  </div>

                  {/* right: action buttons */}
                  <div className="flex items-center gap-2 shrink-0">
                    {reco.status === 'SUBMITTED' && (
                      <>
                        <button
                          onClick={() => handleStatusChange(reco.recoId, 'APPROVED')}
                          disabled={updatingId === reco.recoId}
                          className="btn btn-success btn-sm"
                        >
                          ✓ Approve
                        </button>
                        <button
                          onClick={() => handleStatusChange(reco.recoId, 'REJECTED')}
                          disabled={updatingId === reco.recoId}
                          className="btn btn-ghost btn-sm text-danger"
                        >
                          ✕ Reject
                        </button>
                      </>
                    )}
                    <Link
                      to={'/rm/clients/' + client.clientId}
                      className="btn btn-ghost btn-sm"
                    >
                      View client →
                    </Link>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
