import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllUsers, deleteUser } from '@/api/admin';

const ROLES = ['ALL', 'ADMIN', 'RM', 'DEALER', 'COMPLIANCE', 'CLIENT'];

export default function Users() {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [deleteUsername, setDeleteUsername] = useState('');

  useEffect(() => {
    loadUsers();
  }, []);

  async function loadUsers() {
    setLoading(true);
    try {
      const data = await getAllUsers();
      setUsers(data);
      // Cache username → userId so staff can discover their own ID without admin privileges
      if (Array.isArray(data) && data.length > 0) {
        // Log the first user's keys so we know the exact field names the API returns

        const map: Record<string, { userId: number; role: string }> = {};
        for (const u of data) {
          const uid =
            u.userId ?? u.user_id ?? u.uid ?? u.id ?? u.Id ?? u.UserId ??
            Object.values(u).find(
              (v) => typeof v === 'number' && Number.isInteger(v) && (v as number) > 0
            );
          const role = String(u.roles ?? u.role ?? '').replace('ROLE_', '').toUpperCase();
          if (u.username && uid !== undefined && uid !== null) {
            map[String(u.username)] = { userId: Number(uid), role };
          }
        }
        try { localStorage.setItem('wp_user_id_map', JSON.stringify(map)); } catch { /* storage full */ }
      }
    } catch (e) {
    }
    setLoading(false);
  }

  async function handleDelete() {
    if (!deleteUsername) return;
    try {
      await deleteUser(deleteUsername);
      setDeleteUsername('');
      loadUsers();
    } catch (e: any) {
      alert('Delete failed: ' + (e.response?.data?.message || e.message));
    }
  }

  // filter users
  const filteredUsers = users.filter((u) => {
    if (roleFilter !== 'ALL' && String(u.roles).toUpperCase() !== roleFilter) {
      return false;
    }
    if (search) {
      const s = search.toLowerCase();
      if (
        !u.username.toLowerCase().includes(s) &&
        !(u.name || '').toLowerCase().includes(s) &&
        !(u.email || '').toLowerCase().includes(s)
      ) {
        return false;
      }
    }
    return true;
  });

  function getRolePillClass(role: string): string {
    role = String(role).toUpperCase();
    if (role === 'ADMIN' || role === 'COMPLIANCE') return 'pill-warn';
    if (role === 'CLIENT') return 'pill-success';
    return 'pill-info';
  }

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Users</h1>
          <p className="text-sm text-text-2">{filteredUsers.length} of {users.length} users</p>
        </div>
        <Link to="/admin/users/register" className="btn btn-primary btn-sm">
          + Register staff
        </Link>
      </div>

      {/* filter bar */}
      <div className="panel mb-4">
        <div className="panel-b py-3">
          <div className="flex gap-3 items-center flex-wrap">
            <input
              className="input max-w-sm"
              type="text"
              placeholder="Search users..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            <div className="flex gap-1 ml-auto">
              {ROLES.map((r) => (
                <button
                  key={r}
                  onClick={() => setRoleFilter(r)}
                  className={
                    'px-3 py-1.5 text-xs font-medium rounded border ' +
                    (roleFilter === r
                      ? 'bg-primary text-white border-primary'
                      : 'bg-white text-text-2 border-border')
                  }
                >
                  {r}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* users table */}
      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading users...</div>
        ) : filteredUsers.length === 0 ? (
          <div className="panel-b text-center text-text-2 py-10">No users found</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Username</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Name</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Email</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Phone</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Role</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map((u) => (
                <tr key={u.username} className="border-t border-border-hairline">
                  <td className="px-5 py-3 mono text-xs">{u.username}</td>
                  <td className="px-5 py-3 font-medium">{u.name}</td>
                  <td className="px-5 py-3 text-text-2">{u.email}</td>
                  <td className="px-5 py-3 mono text-xs text-text-2">{u.phone}</td>
                  <td className="px-5 py-3">
                    <span className={'pill ' + getRolePillClass(u.roles)}>{String(u.roles).toUpperCase()}</span>
                  </td>
                  <td className="px-5 py-3 text-right">
                    {u.username === 'admin' ? (
                      <span className="text-xs text-text-3">protected</span>
                    ) : (
                      <button
                        onClick={() => setDeleteUsername(u.username)}
                        className="text-xs text-danger font-medium"
                      >
                        Delete
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* delete confirmation */}
      {deleteUsername && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-sm w-full p-6">
            <h3 className="text-lg font-semibold mb-2">Delete user?</h3>
            <p className="text-sm text-text-2 mb-4">
              Are you sure you want to delete <b>{deleteUsername}</b>? This cannot be undone.
            </p>
            <div className="flex justify-end gap-2">
              <button onClick={() => setDeleteUsername('')} className="btn btn-ghost btn-sm">
                Cancel
              </button>
              <button onClick={handleDelete} className="btn btn-danger btn-sm">
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
