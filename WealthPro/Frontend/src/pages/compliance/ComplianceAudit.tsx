import { useState, useEffect } from 'react';
import { getAuditLogs } from '@/api/admin';

// Compliance-relevant HTTP methods: mutations + all reads that touch sensitive endpoints
const METHODS = ['ALL', 'GET', 'POST', 'PUT', 'PATCH', 'DELETE'];

// Compliance cares about: client data changes, KYC, orders, recommendations
const COMPLIANCE_PATHS = ['/api/clients', '/api/orders', '/api/recommendations', '/api/goals', '/auth'];

export default function ComplianceAudit() {
  const [audits, setAudits] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchUser, setSearchUser] = useState('');
  const [methodFilter, setMethodFilter] = useState('ALL');
  const [pathFilter, setPathFilter] = useState('ALL');

  useEffect(() => {
    loadAudits();
  }, []);

  async function loadAudits(filters?: any) {
    setLoading(true);
    try {
      const data = await getAuditLogs(filters || { limit: 500 });
      setAudits(Array.isArray(data) ? data : []);
    } catch (e) {
      setAudits([]);
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

  // Counts for summary
  const mutationCount = audits.filter((a) => ['POST', 'PUT', 'PATCH', 'DELETE'].includes(a.method)).length;
  const clientChanges = audits.filter((a) => (a.path || '').startsWith('/api/clients') && a.method !== 'GET').length;
  const orderEvents = audits.filter((a) => (a.path || '').startsWith('/api/orders')).length;

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Compliance Audit Log</h1>
          <p className="text-sm text-text-2">Full gateway audit trail — all requests, newest first.</p>
        </div>
        <p className="text-xs text-text-2">Showing {filtered.length} of {audits.length} entries</p>
      </div>

      {/* Summary strip */}
      <div className="grid grid-cols-3 gap-4 mb-4">
        <div className="panel">
          <div className="panel-b py-3">
            <p className="label text-xs">Write operations</p>
            <p className="text-xl font-semibold mt-0.5">{mutationCount}</p>
          </div>
        </div>
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
            {(['ALL', ...COMPLIANCE_PATHS] as string[]).map((p) => (
              <button
                key={p}
                onClick={() => setPathFilter(p)}
                className={
                  'px-2 py-0.5 text-xs rounded border ' +
                  (pathFilter === p
                    ? 'bg-primary text-white border-primary'
                    : 'bg-white text-text-2 border-border')
                }
              >
                {p === 'ALL' ? 'All paths' : p}
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
          <div className="panel-b text-center text-text-2 py-10">No audit entries match current filters.</div>
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
