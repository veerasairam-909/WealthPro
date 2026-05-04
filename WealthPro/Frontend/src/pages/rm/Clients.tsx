import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllClients } from '@/api/clients';
import { TableSkeleton } from '@/components/Skeleton';
import EmptyState from '@/components/EmptyState';
import Pagination from '@/components/Pagination';
import { Users } from 'lucide-react';

const SEGMENTS = ['ALL', 'Retail', 'HNI', 'UHNI'];
const STATUSES = ['ALL', 'Active', 'Inactive', 'PENDING_KYC'];

// Display label — backend value stays PENDING_KYC, user sees "Pending"
function statusLabel(status: string): string {
  if (status === 'PENDING_KYC') return 'Pending';
  return status;
}
const PAGE_SIZE = 12;

export default function Clients() {
  const [clients, setClients] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [segmentFilter, setSegmentFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [page, setPage] = useState(0);

  useEffect(() => {
    loadClients();
  }, []);

  async function loadClients() {
    setLoading(true);
    try {
      const data = await getAllClients();
      setClients(data);
    } catch (e) {
    }
    setLoading(false);
  }

  // filter clients
  const filtered = clients.filter((c) => {
    if (segmentFilter !== 'ALL' && c.segment !== segmentFilter) return false;
    if (statusFilter !== 'ALL' && c.status !== statusFilter) return false;
    if (search) {
      const s = search.toLowerCase();
      if (
        !c.name.toLowerCase().includes(s) &&
        !(c.username || '').toLowerCase().includes(s) &&
        !String(c.clientId).includes(s)
      ) {
        return false;
      }
    }
    return true;
  });

  const pagedClients = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  function getSegmentClass(seg: string): string {
    if (seg === 'UHNI') return 'pill-warn';
    if (seg === 'HNI') return 'pill-info';
    return 'pill-success';
  }

  function getStatusClass(status: string): string {
    if (status === 'Active') return 'pill-success';
    if (status === 'PENDING_KYC') return 'pill-warn';
    return 'pill-danger';
  }

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Clients</h1>
          <p className="text-sm text-text-2">
            {filtered.length} of {clients.length} clients
          </p>
        </div>
        <Link to="/rm/onboard" className="btn btn-primary btn-sm">
          + Onboard new client
        </Link>
      </div>

      {/* filter bar */}
      <div className="panel mb-4">
        <div className="panel-b py-3">
          <div className="flex gap-3 items-center flex-wrap">
            <input
              className="input max-w-xs"
              type="text"
              placeholder="Search by name, username, ID..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />

            <div className="flex items-center gap-2">
              <span className="text-xs text-text-2 font-medium">Segment:</span>
              <select
                className="input max-w-[100px] py-1.5"
                value={segmentFilter}
                onChange={(e) => setSegmentFilter(e.target.value)}
              >
                {SEGMENTS.map((s) => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </div>

            <div className="flex items-center gap-2">
              <span className="text-xs text-text-2 font-medium">Status:</span>
              <select
                className="input max-w-[140px] py-1.5"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                {STATUSES.map((s) => (
                  <option key={s} value={s}>{statusLabel(s)}</option>
                ))}
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* clients table */}
      <div className="panel">
        {loading ? (
          <TableSkeleton rows={7} cols={7} />
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<Users size={26} />}
            title="No clients found"
            description="Try adjusting your search or filters."
          />
        ) : (
          <>
            <table className="w-full text-sm">
              <thead className="bg-surface">
                <tr>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">ID</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Name</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Username</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">DOB</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Segment</th>
                  <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
                  <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pagedClients.map((c) => (
                  <tr key={c.clientId} className="border-t border-border-hairline hover:bg-surface">
                    <td className="px-5 py-3 mono text-xs text-text-2">{c.clientId}</td>
                    <td className="px-5 py-3 font-medium">{c.name}</td>
                    <td className="px-5 py-3 mono text-xs text-text-2">{c.username || '-'}</td>
                    <td className="px-5 py-3 mono text-xs text-text-2">{c.dob || '-'}</td>
                    <td className="px-5 py-3">
                      <span className={'pill ' + getSegmentClass(c.segment)}>{c.segment}</span>
                    </td>
                    <td className="px-5 py-3">
                      <span className={'pill ' + getStatusClass(c.status)}>{statusLabel(c.status)}</span>
                    </td>
                    <td className="px-5 py-3 text-right">
                      <Link
                        to={'/rm/clients/' + c.clientId}
                        className="text-xs text-primary font-medium"
                      >
                        View →
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <Pagination
              page={page}
              total={filtered.length}
              pageSize={PAGE_SIZE}
              onChange={(p) => setPage(p)}
            />
          </>
        )}
      </div>
    </div>
  );
}
