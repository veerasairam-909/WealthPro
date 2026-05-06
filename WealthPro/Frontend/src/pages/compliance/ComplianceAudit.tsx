import { useState, useEffect } from 'react';
import { getAuditLogs } from '@/api/admin';

const METHODS = ['ALL', 'GET', 'POST', 'PUT', 'PATCH', 'DELETE'];

// Paths compliance cares about — admin/system paths are excluded
const COMPLIANCE_RELEVANT_PREFIXES = [
  '/api/clients',
  '/api/orders',
  '/api/aml-flags',
  '/api/recommendations',
  '/api/goals',
  '/api/holdings',
  '/api/accounts',
  '/api/cash-ledger',
  '/api/compliance-breaches',
  '/api/suitability-rules',
  '/api/analytics',
  '/auth/login',   // login events for security monitoring
];

// Sub-paths excluded from compliance view — internal dealer/system operations
const EXCLUDED_SUBPATHS = [
  'pre-trade-checks',   // dealer internal — POST /api/orders/{id}/pre-trade-checks
  '/fills',             // dealer fill recording
  '/allocations',       // dealer allocation recording
  '/route',             // dealer routing
];

const PATH_FILTERS = [
  { label: 'All',            value: 'ALL' },
  { label: 'Clients',        value: '/api/clients' },
  { label: 'Orders',         value: '/api/orders' },
  { label: 'AML Flags',      value: '/api/aml-flags' },
  { label: 'Recommendations',value: '/api/recommendations' },
  { label: 'Goals',          value: '/api/goals' },
  { label: 'Holdings',       value: '/api/holdings' },
  { label: 'Breaches',       value: '/api/compliance-breaches' },
];

function isComplianceRelevant(path: string): boolean {
  const relevant = COMPLIANCE_RELEVANT_PREFIXES.some((prefix) => path.startsWith(prefix));
  if (!relevant) return false;
  const excluded = EXCLUDED_SUBPATHS.some((sub) => path.includes(sub));
  return !excluded;
}

export default function ComplianceAudit() {
  const [audits, setAudits] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchUser, setSearchUser] = useState('');
  const [methodFilter, setMethodFilter] = useState('ALL');
  const [pathFilter, setPathFilter] = useState('ALL');

  useEffect(() => {
    loadAudits();
  }, []);

  async function loadAudits(filters?: any) {
    setLoading(true);
    setError('');
    try {
      const data = await getAuditLogs(filters || { limit: 500 });
      // Keep only compliance-relevant paths — exclude admin/system/internal calls
      const relevant = Array.isArray(data)
        ? data.filter((a: any) => isComplianceRelevant(a.path || ''))
        : [];
      setAudits(relevant);
    } catch (e: any) {
      setAudits([]);
      const status = e?.response?.status;
      if (status === 403) {
        setError('Access denied — the API Gateway needs to be restarted to apply the latest permissions. Ask your admin to restart the gateway service.');
      } else {
        setError('Failed to load audit log. Please try again.');
      }
    }
    setLoading(false);
  }

  function applyFilters() {
    const params: any = { limit: 500 };
    if (searchUser) params.username = searchUser;
    if (methodFilter !== 'ALL') params.method = methodFilter;
    loadAudits(params);
  }

  function resetFilters() {
    setSearchUser('');
    setMethodFilter('ALL');
    setPathFilter('ALL');
    loadAudits({ limit: 500 });
  }

  function getMethodColor(method: string): string {
    if (method === 'GET') return 'pill-info';
    if (method === 'POST') return 'pill-success';
    if (method === 'DELETE') return 'pill-danger';
    if (method === 'PUT' || method === 'PATCH') return 'pill-warn';
    return 'pill-info';
  }

  // Client-side path filter
  const filtered = audits.filter((a) => {
    if (pathFilter === 'ALL') return true;
    return (a.path || '').startsWith(pathFilter);
  });

  // Compliance-specific summary counts
  const clientChanges  = audits.filter((a) => (a.path || '').startsWith('/api/clients') && a.method !== 'GET').length;
  const orderEvents    = audits.filter((a) => (a.path || '').startsWith('/api/orders')).length;
  const amlEvents      = audits.filter((a) => (a.path || '').startsWith('/api/aml-flags')).length;

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Compliance Audit Log</h1>
          <p className="text-sm text-text-2">Full gateway audit trail — all requests, newest first.</p>
        </div>
        <p className="text-xs text-text-2">Showing {filtered.length} of {audits.length} entries</p>
      </div>

      {error && (
        <div className="pill pill-danger block mb-4 text-center">{error}</div>
      )}

      {/* Summary strip */}
      <div className="grid grid-cols-3 gap-4 mb-4">
        <div className="panel">
          <div className="panel-b py-3">
            <p className="label text-xs">Client data changes</p>
            <p className="text-xl font-semibold mt-0.5">{clientChanges}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b py-3">
            <p className="label text-xs">Order events</p>
            <p className="text-xl font-semibold mt-0.5">{orderEvents}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b py-3">
            <p className="label text-xs">AML flag actions</p>
            <p className="text-xl font-semibold mt-0.5">{amlEvents}</p>
          </div>
        </div>
      </div>

      {/* Filter bar */}
      <div className="panel mb-4">
        <div className="panel-b py-3">
          <div className="flex gap-2 items-center flex-wrap">
            <input
              className="input max-w-xs"
              type="text"
              placeholder="Filter by username..."
              value={searchUser}
              onChange={(e) => setSearchUser(e.target.value)}
            />
            <div className="flex gap-1">
              {METHODS.map((m) => (
                <button
                  key={m}
                  onClick={() => setMethodFilter(m)}
                  className={
                    'px-3 py-1.5 text-xs font-medium rounded border ' +
                    (methodFilter === m
                      ? 'bg-primary text-white border-primary'
                      : 'bg-white text-text-2 border-border')
                  }
                >
                  {m}
                </button>
              ))}
            </div>
            <div className="ml-auto flex gap-2">
              <button onClick={resetFilters} className="btn btn-ghost btn-sm">Reset</button>
              <button onClick={applyFilters} className="btn btn-primary btn-sm">Apply</button>
            </div>
          </div>

          {/* Path quick-filter */}
          <div className="flex gap-1 mt-2 flex-wrap">
            <span className="text-xs text-text-2 self-center mr-1">Path:</span>
            {PATH_FILTERS.map(({ label, value }) => (
              <button
                key={value}
                onClick={() => setPathFilter(value)}
                className={
                  'px-2 py-0.5 text-xs rounded border ' +
                  (pathFilter === value
                    ? 'bg-primary text-white border-primary'
                    : 'bg-white text-text-2 border-border')
                }
              >
                {label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading audit log...</div>
        ) : filtered.length === 0 ? (
          <div className="panel-b text-center text-text-2 py-10">
            {audits.length === 0 ? 'No audit entries recorded yet.' : 'No entries match current filters.'}
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Time</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">User</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Role</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Method</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Path</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">IP</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((a: any) => (
                <tr
                  key={a.id}
                  className={
                    'border-t border-border-hairline ' +
                    (['DELETE'].includes(a.method) ? 'bg-danger/5' :
                     ['POST', 'PUT', 'PATCH'].includes(a.method) ? 'bg-warn/5' : '')
                  }
                >
                  <td className="px-5 py-3 mono text-xs text-text-2">
                    {(a.timestamp || '').replace('T', ' ').slice(0, 19)}
                  </td>
                  <td className="px-5 py-3 font-medium">{a.username || '-'}</td>
                  <td className="px-5 py-3 mono text-xs text-text-2">
                    {(a.roles || '').replace('[', '').replace(']', '').replace('ROLE_', '')}
                  </td>
                  <td className="px-5 py-3">
                    <span className={'pill ' + getMethodColor(a.method)}>{a.method || '-'}</span>
                  </td>
                  <td className="px-5 py-3 mono text-xs text-text-2 truncate max-w-md" title={a.path}>
                    {a.path || '-'}
                  </td>
                  <td className="px-5 py-3 mono text-xs text-text-3">{a.remoteAddress || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
