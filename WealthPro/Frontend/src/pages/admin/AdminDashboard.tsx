import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';
import {
  Users, ShieldCheck, Activity, UserPlus,
  BarChart3, Scale, History, TrendingUp, TrendingDown,
  ArrowRight, Layers, CheckCircle, AlertTriangle,
} from 'lucide-react';
import { getAllUsers, getAuditLogs } from '@/api/admin';
import { CardSkeleton, TableSkeleton } from '@/components/Skeleton';

// ── colour map for HTTP methods ────────────────────────────────────────────
const METHOD_PILL: Record<string, string> = {
  GET:    'pill pill-info',
  POST:   'pill pill-success',
  PUT:    'pill pill-warn',
  PATCH:  'pill pill-warn',
  DELETE: 'pill pill-danger',
};

// ── donut chart colours ────────────────────────────────────────────────────
const ROLE_COLORS: Record<string, string> = {
  CLIENT:     '#387ED1',
  RM:         '#00B386',
  DEALER:     '#F4A41E',
  COMPLIANCE: '#EB5B3C',
  ADMIN:      '#8B5CF6',
};
const ROLE_ORDER = ['CLIENT', 'RM', 'DEALER', 'COMPLIANCE', 'ADMIN'];

// ── custom tooltip for bar chart ────────────────────────────────────────────
function BarTip({ active, payload, label }: any) {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-white border border-border rounded shadow-md px-3 py-2 text-xs">
      <p className="font-semibold mb-1">{label}</p>
      <p className="text-primary">{payload[0].value} events</p>
    </div>
  );
}

export default function AdminDashboard() {
  const [users, setUsers]   = useState<any[]>([]);
  const [audits, setAudits] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadData(); }, []);

  async function loadData() {
    try {
      const [userList, auditList] = await Promise.all([
        getAllUsers(),
        getAuditLogs({ limit: 1000 }),
      ]);
      if (Array.isArray(userList))  setUsers(userList);
      if (Array.isArray(auditList)) setAudits(auditList);
    } catch { /* services may be down — show zeros */ }
    setLoading(false);
  }

  // ── derived stats ──────────────────────────────────────────────────────────
  const roleCount: Record<string, number> = {};
  for (const u of users) {
    const r = String(u.roles || u.role || 'UNKNOWN').toUpperCase();
    roleCount[r] = (roleCount[r] || 0) + 1;
  }
  const totalClients = roleCount['CLIENT'] || 0;
  const totalStaff   = users.length - totalClients;

  const today = new Date().toISOString().slice(0, 10);
  const todayCount = audits.filter((a) => (a.timestamp || '').slice(0, 10) === today).length;

  // last-7-days bar chart data
  const last7: { date: string; label: string; count: number }[] = [];
  for (let i = 6; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    const iso   = d.toISOString().slice(0, 10);
    const label = d.toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric' });
    last7.push({ date: iso, label, count: 0 });
  }
  for (const a of audits) {
    const slot = last7.find((s) => s.date === (a.timestamp || '').slice(0, 10));
    if (slot) slot.count++;
  }

  // donut chart data
  const pieData = ROLE_ORDER
    .filter((r) => roleCount[r] > 0)
    .map((r) => ({ name: r, value: roleCount[r] }));

  // recent 6 activity
  const recent = audits.slice(0, 6);

  // ── KPI cards config ───────────────────────────────────────────────────────
  const kpis = [
    {
      label: 'Total Users',
      value: users.length,
      Icon: Users,
      color: 'text-primary',
      bg: 'bg-primary-soft',
      border: 'border-l-4 border-primary',
      trend: null,
    },
    {
      label: 'Clients',
      value: totalClients,
      Icon: CheckCircle,
      color: 'text-success',
      bg: 'bg-success-soft',
      border: 'border-l-4 border-success',
      trend: null,
    },
    {
      label: 'Staff Members',
      value: totalStaff,
      Icon: ShieldCheck,
      color: 'text-accent',
      bg: 'bg-accent-soft',
      border: 'border-l-4 border-accent',
      trend: null,
    },
    {
      label: "Today's Events",
      value: todayCount,
      Icon: Activity,
      color: 'text-warn',
      bg: 'bg-warn-soft',
      border: 'border-l-4 border-warn',
      trend: todayCount > 0 ? 'up' : null,
    },
  ];

  // ── quick-action cards ─────────────────────────────────────────────────────
  const actions = [
    { to: '/admin/users',            label: 'Manage Users',        Icon: Users,    desc: 'View, edit and remove users',        color: 'text-primary', bg: 'bg-primary-soft' },
    { to: '/admin/users/register',   label: 'Register Staff',       Icon: UserPlus, desc: 'Onboard a new staff member',          color: 'text-success', bg: 'bg-success-soft' },
    { to: '/admin/securities',       label: 'Securities Master',    Icon: Layers,   desc: 'Manage the product catalog',          color: 'text-warn',    bg: 'bg-warn-soft' },
    { to: '/admin/model-portfolios', label: 'Model Portfolios',     Icon: BarChart3, desc: 'Configure advisory model portfolios', color: 'text-accent',  bg: 'bg-accent-soft' },
    { to: '/admin/audit',            label: 'Audit Log',            Icon: History,  desc: 'Full platform activity trail',        color: 'text-text-2',  bg: 'bg-surface' },
  ];

  if (loading) {
    return (
      <div>
        <div className="flex justify-between items-end mb-5">
          <div>
            <h1 className="text-2xl font-semibold mb-1">Admin Dashboard</h1>
            <p className="text-sm text-text-2">Platform overview</p>
          </div>
        </div>
        <CardSkeleton count={4} />
        <div className="grid md:grid-cols-2 gap-4 mt-5">
          <div className="panel h-64"><div className="panel-b h-full"><div className="animate-pulse bg-surface-2 rounded h-full" /></div></div>
          <div className="panel h-64"><div className="panel-b h-full"><div className="animate-pulse bg-surface-2 rounded h-full" /></div></div>
        </div>
        <div className="panel mt-5"><TableSkeleton rows={5} cols={4} /></div>
      </div>
    );
  }

  return (
    <div>
      {/* ── header ── */}
      <div className="flex justify-between items-end mb-6">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Admin Dashboard</h1>
          <p className="text-sm text-text-2">Platform-wide overview · {new Date().toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</p>
        </div>
        <Link to="/admin/users/register" className="btn btn-primary btn-sm flex items-center gap-1.5">
          <UserPlus size={14} /> Register Staff
        </Link>
      </div>

      {/* ── KPI strip ── */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        {kpis.map(({ label, value, Icon, color, bg, border, trend }) => (
          <div key={label} className={`panel ${border}`}>
            <div className="panel-b py-4">
              <div className="flex items-start justify-between">
                <div>
                  <p className="label mb-1">{label}</p>
                  <p className={`text-3xl font-bold mono ${color}`}>{value}</p>
                </div>
                <div className={`w-10 h-10 rounded-lg ${bg} flex items-center justify-center ${color}`}>
                  <Icon size={18} />
                </div>
              </div>
              {trend === 'up' && (
                <div className="flex items-center gap-1 mt-2 text-xs text-success">
                  <TrendingUp size={12} /> Active today
                </div>
              )}
              {trend === 'down' && (
                <div className="flex items-center gap-1 mt-2 text-xs text-danger">
                  <TrendingDown size={12} /> Lower than yesterday
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* ── charts row ── */}
      <div className="grid md:grid-cols-5 gap-4 mb-6">
        {/* Audit activity bar chart — 3 cols */}
        <div className="panel md:col-span-3">
          <div className="panel-h">
            <div className="flex items-center gap-2">
              <Activity size={15} className="text-primary" />
              <h3>Audit Activity — Last 7 Days</h3>
            </div>
            <Link to="/admin/audit" className="text-sm text-primary flex items-center gap-1 hover:underline">
              View all <ArrowRight size={13} />
            </Link>
          </div>
          <div className="panel-b py-4">
            {last7.every((d) => d.count === 0) ? (
              <div className="flex flex-col items-center justify-center h-44 text-text-3 gap-2">
                <AlertTriangle size={24} />
                <p className="text-sm">No audit data yet</p>
              </div>
            ) : (
              <ResponsiveContainer width="100%" height={176}>
                <BarChart data={last7} margin={{ top: 4, right: 8, left: -24, bottom: 0 }}>
                  <XAxis dataKey="label" tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={false} tickLine={false} allowDecimals={false} />
                  <Tooltip content={<BarTip />} cursor={{ fill: 'rgba(56,126,209,0.06)' }} />
                  <Bar dataKey="count" fill="#387ED1" radius={[4, 4, 0, 0]} maxBarSize={36} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        {/* User role distribution donut — 2 cols */}
        <div className="panel md:col-span-2">
          <div className="panel-h">
            <div className="flex items-center gap-2">
              <Users size={15} className="text-primary" />
              <h3>User Breakdown</h3>
            </div>
            <span className="text-xs text-text-2 mono">{users.length} total</span>
          </div>
          <div className="panel-b py-2">
            {pieData.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-44 text-text-3 gap-2">
                <Users size={24} />
                <p className="text-sm">No users yet</p>
              </div>
            ) : (
              <ResponsiveContainer width="100%" height={200}>
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="45%"
                    innerRadius={52}
                    outerRadius={76}
                    paddingAngle={3}
                    dataKey="value"
                  >
                    {pieData.map((entry) => (
                      <Cell key={entry.name} fill={ROLE_COLORS[entry.name] || '#94a3b8'} />
                    ))}
                  </Pie>
                  <Legend
                    iconType="circle"
                    iconSize={8}
                    formatter={(v) => <span style={{ fontSize: 11, color: '#64748b' }}>{v}</span>}
                  />
                  <Tooltip
                    formatter={(val, name) => [(val ?? 0) + ' users', String(name)]}
                    contentStyle={{ fontSize: 12, borderRadius: 6, border: '1px solid #e2e8f0' }}
                  />
                </PieChart>
              </ResponsiveContainer>
            )}
            {/* role legend rows */}
            <div className="mt-1 space-y-1">
              {ROLE_ORDER.filter((r) => roleCount[r] > 0).map((r) => (
                <div key={r} className="flex items-center justify-between text-xs px-1">
                  <div className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ background: ROLE_COLORS[r] }} />
                    <span className="text-text-2">{r}</span>
                  </div>
                  <span className="mono font-medium">{roleCount[r]}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* ── quick actions ── */}
      <div className="mb-6">
        <h2 className="text-sm font-semibold text-text-2 uppercase tracking-wider mb-3">Quick Actions</h2>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
          {actions.map(({ to, label, Icon, desc, color, bg }) => (
            <Link
              key={to}
              to={to}
              className="panel hover:shadow-md transition-shadow group"
            >
              <div className="panel-b py-4 flex flex-col items-start gap-3">
                <div className={`w-9 h-9 rounded-lg ${bg} flex items-center justify-center ${color} group-hover:scale-110 transition-transform`}>
                  <Icon size={17} />
                </div>
                <div>
                  <p className="text-sm font-semibold leading-tight">{label}</p>
                  <p className="text-xs text-text-3 mt-0.5 leading-snug hidden md:block">{desc}</p>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </div>

      {/* ── recent activity ── */}
      <div className="panel">
        <div className="panel-h">
          <div className="flex items-center gap-2">
            <History size={15} className="text-text-2" />
            <h3>Recent Activity</h3>
          </div>
          <Link to="/admin/audit" className="text-sm text-primary flex items-center gap-1 hover:underline">
            View all <ArrowRight size={13} />
          </Link>
        </div>
        {recent.length === 0 ? (
          <div className="panel-b py-10 text-center">
            <Activity size={28} className="text-text-3 mx-auto mb-2" />
            <p className="text-sm text-text-2">No activity recorded yet</p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Time</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">User</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Method</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Path</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {recent.map((a: any, idx: number) => (
                <tr key={a.id ?? idx} className="border-t border-border-hairline hover:bg-surface">
                  <td className="px-5 py-3 text-xs text-text-2 mono whitespace-nowrap">
                    {(a.timestamp || '').replace('T', ' ').slice(0, 19)}
                  </td>
                  <td className="px-5 py-3">
                    <div className="flex items-center gap-2">
                      <div className="w-6 h-6 rounded-full bg-primary-soft text-primary flex items-center justify-center text-xs font-bold uppercase shrink-0">
                        {(a.username || '?').slice(0, 1)}
                      </div>
                      <span className="font-medium">{a.username || '—'}</span>
                    </div>
                  </td>
                  <td className="px-5 py-3">
                    <span className={METHOD_PILL[a.method] || 'pill pill-info'}>
                      {a.method || '—'}
                    </span>
                  </td>
                  <td className="px-5 py-3 mono text-xs text-text-2 max-w-[260px] truncate">
                    {a.path || '—'}
                  </td>
                  <td className="px-5 py-3 text-right">
                    {a.responseStatus ? (
                      <span className={`pill ${a.responseStatus < 400 ? 'pill-success' : 'pill-danger'}`}>
                        {a.responseStatus}
                      </span>
                    ) : (
                      <span className="text-text-3 text-xs">—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
