import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllUsers, getAuditLogs } from '@/api/admin';

export default function AdminDashboard() {
  const [users, setUsers] = useState<any[]>([]);
  const [audits, setAudits] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      const userList = await getAllUsers();
      setUsers(userList);

      const auditList = await getAuditLogs({ limit: 1000 });
      setAudits(auditList);
    } catch (e) {
    }
    setLoading(false);
  }

  // calculate stats
  let totalClients = 0;
  for (let i = 0; i < users.length; i++) {
    if (String(users[i].roles).toUpperCase() === 'CLIENT') {
      totalClients++;
    }
  }
  const totalStaff = users.length - totalClients;

  // count today's audits
  const today = new Date().toISOString().slice(0, 10);
  let todayCount = 0;
  for (let i = 0; i < audits.length; i++) {
    if ((audits[i].timestamp || '').slice(0, 10) === today) {
      todayCount++;
    }
  }

  // recent 8
  const recent = audits.slice(0, 8);

  if (loading) {
    return <div className="p-10 text-center text-text-2">Loading...</div>;
  }

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Admin Dashboard</h1>
          <p className="text-sm text-text-2">Platform overview</p>
        </div>
        <Link to="/admin/users/register" className="btn btn-primary btn-sm">
          + Register staff
        </Link>
      </div>

      {/* KPI cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div className="panel">
          <div className="panel-b">
            <p className="label">Total Users</p>
            <p className="text-3xl font-semibold mono mt-1">{users.length}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Clients</p>
            <p className="text-3xl font-semibold mono mt-1">{totalClients}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Staff</p>
            <p className="text-3xl font-semibold mono mt-1">{totalStaff}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Audit events today</p>
            <p className="text-3xl font-semibold mono mt-1">{todayCount}</p>
          </div>
        </div>
      </div>

      {/* recent activity */}
      <div className="panel">
        <div className="panel-h">
          <h3>Recent Activity</h3>
          <Link to="/admin/audit" className="text-sm text-primary">View all</Link>
        </div>
        {recent.length === 0 ? (
          <div className="panel-b text-center text-text-2">No activity yet</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-2 text-xs uppercase font-medium text-text-2">Time</th>
                <th className="text-left px-5 py-2 text-xs uppercase font-medium text-text-2">User</th>
                <th className="text-left px-5 py-2 text-xs uppercase font-medium text-text-2">Method</th>
                <th className="text-left px-5 py-2 text-xs uppercase font-medium text-text-2">Path</th>
              </tr>
            </thead>
            <tbody>
              {recent.map((a: any) => (
                <tr key={a.id} className="border-t border-border-hairline">
                  <td className="px-5 py-2 text-xs text-text-2 mono">
                    {(a.timestamp || '').replace('T', ' ').slice(0, 19)}
                  </td>
                  <td className="px-5 py-2">{a.username}</td>
                  <td className="px-5 py-2 mono text-xs">{a.method}</td>
                  <td className="px-5 py-2 mono text-xs text-text-2">{a.path}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
