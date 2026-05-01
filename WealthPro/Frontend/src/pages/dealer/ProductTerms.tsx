import { useState, useEffect } from 'react';
import { api } from '@/api/client';

async function getAllProductTerms() {
  const res = await api.get('/api/product-terms');
  return res.data;
}

async function getProductTermsBySecurityId(securityId: number) {
  const res = await api.get(`/api/product-terms/security/${securityId}`);
  return res.data;
}

export default function ProductTerms() {
  const [terms, setTerms] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState<number | null>(null);
  const [searchId, setSearchId] = useState('');

  useEffect(() => {
    loadTerms();
  }, []);

  async function loadTerms() {
    setLoading(true);
    try {
      const data = await getAllProductTerms();
      if (Array.isArray(data)) {
        setTerms(data);
      }
    } catch (e) {
    }
    setLoading(false);
  }

  async function searchBySecurity(e: React.FormEvent) {
    e.preventDefault();
    const id = parseInt(searchId, 10);
    if (!searchId.trim() || isNaN(id)) {
      loadTerms();
      return;
    }
    setLoading(true);
    try {
      const data = await getProductTermsBySecurityId(id);
      // API might return a single object or an array
      setTerms(Array.isArray(data) ? data : [data]);
    } catch (e) {
      setTerms([]);
    }
    setLoading(false);
  }

  function parseTerms(raw: string) {
    try { return JSON.parse(raw); } catch { return null; }
  }

  // Backend field names: termJson, effectiveFrom, effectiveTo, securitySymbol, securityId
  function getTermType(t: any): string | null {
    const parsed = t.termJson ? parseTerms(t.termJson) : null;
    return parsed?.termType || null;
  }

  return (
    <div>
      <div className="mb-5">
        <h1 className="text-2xl font-semibold mb-1">Product Terms</h1>
        <p className="text-sm text-text-2">
          Legal and regulatory terms for securities. Review before executing orders or providing advice.
        </p>
      </div>

      {/* search by security */}
      <form onSubmit={searchBySecurity} className="panel mb-4">
        <div className="panel-b py-3">
          <div className="flex gap-2 items-center">
            <input
              className="input mono max-w-xs"
              type="number"
              placeholder="Filter by Security ID..."
              value={searchId}
              onChange={(e) => setSearchId(e.target.value)}
            />
            <button type="submit" className="btn btn-primary btn-sm">Filter</button>
            {searchId && (
              <button type="button" className="btn btn-ghost btn-sm" onClick={() => { setSearchId(''); loadTerms(); }}>
                Clear
              </button>
            )}
            <span className="ml-auto text-xs text-text-2">{terms.length} records</span>
          </div>
        </div>
      </form>

      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading product terms...</div>
        ) : terms.length === 0 ? (
          <div className="panel-b text-center py-10">
            <p className="text-3xl mb-2">📋</p>
            <p className="font-semibold">No product terms found</p>
            <p className="text-sm text-text-2 mt-1">Product terms are added by compliance when securities are listed.</p>
          </div>
        ) : (
          <div className="divide-y divide-border-hairline">
            {terms.map((t: any) => {
              const isExpanded = expanded === t.termId;
              const parsed = t.termJson ? parseTerms(t.termJson) : null;
              const termType = parsed?.termType || null;
              return (
                <div key={t.termId} className="panel-b">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-3 mb-1 flex-wrap">
                        {/* title: symbol is most readable; fall back to term ID */}
                        <p className="font-semibold">
                          {t.securitySymbol ? `${t.securitySymbol} Terms` : `Term #${t.termId}`}
                        </p>
                        {termType && (
                          <span className="pill pill-info text-xs">{termType}</span>
                        )}
                        {t.effectiveFrom && (
                          <span className="text-xs text-text-2 mono">Effective: {t.effectiveFrom}</span>
                        )}
                        {t.effectiveTo && (
                          <span className="text-xs text-text-2 mono">Expires: {t.effectiveTo}</span>
                        )}
                        {!t.effectiveTo && (
                          <span className="pill pill-success text-xs">Open-ended</span>
                        )}
                      </div>
                      <p className="text-xs text-text-2">
                        Security ID: <span className="mono font-medium">{t.securityId}</span>
                      </p>
                    </div>
                    <button
                      onClick={() => setExpanded(isExpanded ? null : t.termId)}
                      className="btn btn-ghost btn-sm shrink-0"
                    >
                      {isExpanded ? 'Collapse ▲' : 'View ▼'}
                    </button>
                  </div>

                  {isExpanded && (
                    <div className="mt-3 bg-surface rounded-lg p-4 text-sm">
                      {parsed ? (
                        <table className="w-full text-sm">
                          <tbody>
                            {Object.entries(parsed)
                              .filter(([k]) => k !== 'termType')   // already shown as pill
                              .map(([k, v]) => (
                                <tr key={k} className="border-b border-border-hairline last:border-0">
                                  <td className="py-1.5 pr-4 label w-48 align-top capitalize">
                                    {k.replace(/([A-Z])/g, ' $1').replace(/_/g, ' ').trim()}
                                  </td>
                                  <td className="py-1.5 font-medium text-text">{String(v)}</td>
                                </tr>
                              ))}
                          </tbody>
                        </table>
                      ) : t.termJson ? (
                        <pre className="text-xs text-text-2 whitespace-pre-wrap break-all">{t.termJson}</pre>
                      ) : (
                        <p className="text-text-3 italic text-xs">No terms detail available.</p>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
