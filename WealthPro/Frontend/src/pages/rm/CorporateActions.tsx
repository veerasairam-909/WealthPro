import React, { useState, useEffect } from 'react';
import { api } from '@/api/client';

async function getAllCorporateActions() {
  const res = await api.get('/api/corporate-actions');
  return res.data;
}

async function getCorporateActionsByAccount(accountId: number) {
  const res = await api.get(`/api/corporate-actions/account/${accountId}`);
  return res.data;
}

export default function CorporateActions() {
  const [actions, setActions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [expanded, setExpanded] = useState<number | null>(null);

  const CA_TYPES = ['ALL', 'DIVIDEND', 'BONUS', 'SPLIT', 'REDEMPTION', 'COUPON', 'RIGHTS'];

  useEffect(() => {
    loadActions();
  }, []);

  async function loadActions() {
    setLoading(true);
    try {
      const data = await getAllCorporateActions();
      if (Array.isArray(data)) {
        // sort newest first by payDate or effectiveDate
        const sorted = [...data].sort((a, b) => {
          const da = a.payDate || a.effectiveDate || '';
          const db = b.payDate || b.effectiveDate || '';
          return db.localeCompare(da);
        });
        setActions(sorted);
      }
    } catch (e) {
    }
    setLoading(false);
  }

  function getTypePill(type: string) {
    if (type === 'DIVIDEND' || type === 'COUPON') return 'pill-success';
    if (type === 'BONUS')   return 'pill-info';
    if (type === 'SPLIT')   return 'pill-warn';
    if (type === 'REDEMPTION') return 'pill-danger';
    return 'pill-info';
  }

  function parseTerms(raw: string) {
    try { return JSON.parse(raw); } catch { return null; }
  }

  const filtered = typeFilter === 'ALL'
    ? actions
    : actions.filter((a) => a.caType === typeFilter);

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Corporate Actions</h1>
          <p className="text-sm text-text-2">
            Dividends, bonuses, splits, redemptions, and coupon payments across all client accounts.
          </p>
        </div>
        <button onClick={loadActions} disabled={loading} className="btn btn-ghost btn-sm">
          {loading ? 'Loading...' : '↻ Refresh'}
        </button>
      </div>

      {/* type filter */}
      <div className="panel mb-4">
        <div className="panel-b py-3 flex gap-2 items-center flex-wrap">
          <span className="text-xs text-text-2 font-medium">Type:</span>
          {CA_TYPES.map((t) => (
            <button
              key={t}
              onClick={() => setTypeFilter(t)}
              className={
                'px-3 py-1.5 text-xs font-medium rounded border ' +
                (typeFilter === t
                  ? 'bg-primary text-white border-primary'
                  : 'bg-white text-text-2 border-border')
              }
            >
              {t}
            </button>
          ))}
          <span className="ml-auto text-xs text-text-2">{filtered.length} actions</span>
        </div>
      </div>

      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading corporate actions...</div>
        ) : filtered.length === 0 ? (
          <div className="panel-b text-center py-10">
            <p className="text-3xl mb-2">📂</p>
            <p className="font-semibold">No corporate actions found</p>
            <p className="text-sm text-text-2 mt-1">
              {typeFilter !== 'ALL' ? 'No ' + typeFilter + ' actions found. Try a different type.' : 'No corporate actions have been recorded yet.'}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-surface">
                <tr>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Type</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Account</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Record Date</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Ex-Date</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Pay Date</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Effective Date</th>
                  <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Details</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((a: any) => {
                  const isExpanded = expanded === a.caId;
                  const parsed = a.termsJson ? parseTerms(a.termsJson) : null;
                  return (
                    <React.Fragment key={a.caId}>
                      <tr className="border-t border-border-hairline">
                        <td className="px-5 py-3">
                          <span className={'pill ' + getTypePill(a.caType)}>{a.caType}</span>
                        </td>
                        <td className="px-5 py-3 mono text-xs">{a.accountId}</td>
                        <td className="px-5 py-3 mono text-xs text-text-2">{a.recordDate || '—'}</td>
                        <td className="px-5 py-3 mono text-xs text-text-2">{a.exDate || '—'}</td>
                        <td className="px-5 py-3 mono text-xs text-text-2">{a.payDate || '—'}</td>
                        <td className="px-5 py-3 mono text-xs text-text-2">{a.effectiveDate || '—'}</td>
                        <td className="px-5 py-3 text-right">
                          {(a.termsJson || a.description) && (
                            <button
                              onClick={() => setExpanded(isExpanded ? null : a.caId)}
                              className="btn btn-ghost btn-sm"
                            >
                              {isExpanded ? 'Hide ▲' : 'View ▼'}
                            </button>
                          )}
                        </td>
                      </tr>
                      {isExpanded && (
                        <tr className="bg-surface border-t border-border-hairline">
                          <td colSpan={7} className="px-5 py-4">
                            {parsed ? (
                              <div className="flex flex-wrap gap-4">
                                {Object.entries(parsed).map(([k, v]) => (
                                  <div key={k}>
                                    <p className="label capitalize">{k.replace(/_/g, ' ')}</p>
                                    <p className="font-medium text-sm mt-0.5">{String(v)}</p>
                                  </div>
                                ))}
                              </div>
                            ) : (
                              <p className="text-sm text-text-2">{a.description || a.termsJson}</p>
                            )}
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
