import { useState, useEffect } from 'react';
import { getAuditLogs } from '@/api/admin';

const METHODS = ['ALL', 'GET', 'POST', 'PUT', 'PATCH', 'DELETE'];

export default function Audit() {
  const [audits, setAudits] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchUser, setSearchUser] = useState('');
  const [methodFilter, setMethodFilter] = useState('ALL');

  useEffect(() => {
    loadAudits();
  }, []);

  async function loadAudits(filters?: any) {
    setLoading(true);
    try {
      const data = await getAuditLogs(filters || { limit: 200 });
      setAudits(data);
    } catch (e) {
    }
    setLoading(false);
  }

  function applyFilters() {
    const params: any = { limit: 200 };
    if (searchUser) params.username = searchUser;
    if (methodFilter !== 'ALL') params.method = methodFilter;
    loadAudits(params);
  }

  function resetFilters() {
    setSearchUser('');
    setMethodFilter('ALL');
    loadAudits({ limit: 200 });
  }

  function getMethodColor(method: string): string {
    if (method === 'GET') return 'pill-info';
    if (method === 'POST') return 'pill-success';
    if (method === 'DELETE') return 'pill-danger';
    if (method === 'PUT' || method === 'PATCH') return 'pill-warn';
    return 'pill-info';
  }

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Audit Log</h1>
          <p className="text-sm text-text-2">All gateway requests, newest first</p>
        </div>
        <p className="text-xs text-text-2">Showing {audits.length} entries</p>
      </div>

      {/* filter bar */}
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
        </div>
      </div>

      {/* table */}
      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading...</div>
        ) : audits.length === 0 ? (
          <div className="panel-b text-center text-text-2 py-10">No audit events</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Time</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">User</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Role</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Method</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Path</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">From</th>
              </tr>
            </thead>
            <tbody>
              {audits.map((a: any) => (
                <tr key={a.id} className="border-t border-border-hairline">
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
