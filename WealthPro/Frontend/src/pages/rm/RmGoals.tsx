import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllClients } from '@/api/clients';
import { getGoalsByClientId } from '@/api/goals';
import { cachedFetch, parallelLimit } from '@/lib/fetchUtils';

interface GoalItem {
  client: any;
  goal: any;
}

const GOAL_TYPES = ['ALL', 'RETIREMENT', 'EDUCATION', 'WEALTH', 'CUSTOM'];
const STATUSES = ['ALL', 'ACTIVE', 'COMPLETED', 'PAUSED'];

export default function RmGoals() {
  const [items, setItems] = useState<GoalItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [search, setSearch] = useState('');

  useEffect(() => { loadAll(); }, []);

  async function loadAll() {
    setLoading(true);
    try {
      const clients = await cachedFetch('clients', getAllClients);
      if (!Array.isArray(clients)) { setLoading(false); return; }

      const goalResults = await parallelLimit(
        clients.map((c: any) => () => getGoalsByClientId(c.clientId))
      );

      const flat: GoalItem[] = [];
      for (let i = 0; i < clients.length; i++) {
        const r = goalResults[i];
        if (r.status === 'fulfilled' && Array.isArray(r.value)) {
          for (const goal of r.value) {
            flat.push({ client: clients[i], goal });
          }
        }
      }

      // Sort by priority ascending, then by target date
      flat.sort((a, b) => {
        const pa = Number(a.goal.priority) || 10;
        const pb = Number(b.goal.priority) || 10;
        if (pa !== pb) return pa - pb;
        return (a.goal.targetDate || '').localeCompare(b.goal.targetDate || '');
      });

      setItems(flat);
    } catch (e) {
    }
    setLoading(false);
  }

  function daysRemaining(dateStr: string) {
    const diff = new Date(dateStr).getTime() - Date.now();
    return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
  }

  function fmt(n: number) {
    return Number(n).toLocaleString('en-IN', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  }

  function getGoalIcon(type: string) {
    if (type === 'RETIREMENT') return '🏖️';
    if (type === 'EDUCATION') return '🎓';
    if (type === 'WEALTH') return '💰';
    return '🎯';
  }

  function getStatusPill(s: string) {
    if (s === 'COMPLETED') return 'pill-success';
    if (s === 'PAUSED') return 'pill-warn';
    return 'pill-info';
  }

  const displayed = items.filter((x) => {
    const matchType = typeFilter === 'ALL' || x.goal.goalType === typeFilter;
    const matchStatus = statusFilter === 'ALL' || x.goal.status === statusFilter;
    const matchSearch =
      !search ||
      x.client.name.toLowerCase().includes(search.toLowerCase()) ||
      (x.goal.goalType || '').toLowerCase().includes(search.toLowerCase());
    return matchType && matchStatus && matchSearch;
  });

  const activeCount = items.filter((x) => x.goal.status === 'ACTIVE').length;
  const completedCount = items.filter((x) => x.goal.status === 'COMPLETED').length;
  const overdueCount = items.filter((x) => {
    return x.goal.status === 'ACTIVE' && daysRemaining(x.goal.targetDate) === 0;
  }).length;

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">
            All Client Goals
            {overdueCount > 0 && (
              <span className="pill pill-danger ml-3">{overdueCount} overdue</span>
            )}
          </h1>
          <p className="text-sm text-text-2">
            Financial targets across all your clients — sorted by priority.
          </p>
        </div>
        <button onClick={loadAll} disabled={loading} className="btn btn-ghost btn-sm">
          {loading ? 'Loading...' : '↻ Refresh'}
        </button>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-3 gap-4 mb-4">
        <div className="panel">
          <div className="panel-b py-3">
            <p className="label text-xs">Total goals</p>
            <p className="text-xl font-semibold mt-0.5">{items.length}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b py-3">
            <p className="label text-xs">Active</p>
            <p className="text-xl font-semibold mt-0.5">{activeCount}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b py-3">
            <p className="label text-xs">Completed</p>
            <p className={'text-xl font-semibold mt-0.5 ' + (completedCount > 0 ? 'text-success' : '')}>
              {completedCount}
            </p>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="panel mb-4">
        <div className="panel-b py-3 space-y-2">
          <div className="flex gap-2 items-center flex-wrap">
            <input
              className="input max-w-xs"
              type="text"
              placeholder="Search by client name..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            <span className="ml-auto text-xs text-text-2">{displayed.length} goals shown</span>
          </div>
          <div className="flex gap-1 flex-wrap">
            <span className="text-xs text-text-2 self-center mr-1">Type:</span>
            {GOAL_TYPES.map((t) => (
              <button
                key={t}
                onClick={() => setTypeFilter(t)}
                className={
                  'px-2 py-1 text-xs font-medium rounded border ' +
                  (typeFilter === t
                    ? 'bg-primary text-white border-primary'
                    : 'bg-white text-text-2 border-border')
                }
              >
                {t}
              </button>
            ))}
            <span className="text-xs text-text-2 self-center ml-3 mr-1">Status:</span>
            {STATUSES.map((s) => (
              <button
                key={s}
                onClick={() => setStatusFilter(s)}
                className={
                  'px-2 py-1 text-xs font-medium rounded border ' +
                  (statusFilter === s
                    ? 'bg-primary text-white border-primary'
                    : 'bg-white text-text-2 border-border')
                }
              >
                {s}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Goals table */}
      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading goals...</div>
        ) : displayed.length === 0 ? (
          <div className="panel-b text-center py-10">
            <p className="text-3xl mb-2">🎯</p>
            <p className="font-semibold">
              {items.length === 0 ? 'No goals set yet' : 'No goals match your filters'}
            </p>
            <p className="text-sm text-text-2 mt-1">
              {items.length === 0
                ? 'Encourage clients to set their financial goals in the app.'
                : 'Try adjusting the type or status filter.'}
            </p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Client</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Goal</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Target (₹)</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Target date</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Days left</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Priority</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Action</th>
              </tr>
            </thead>
            <tbody>
              {displayed.map(({ client, goal }) => {
                const days = daysRemaining(goal.targetDate);
                const isOverdue = days === 0 && goal.status !== 'COMPLETED';
                return (
                  <tr key={goal.goalId} className={'border-t border-border-hairline ' + (isOverdue ? 'bg-danger/5' : '')}>
                    <td className="px-5 py-3">
                      <p className="font-medium">{client.name}</p>
                      <p className="text-xs text-text-2 mono">{client.clientId}</p>
                    </td>
                    <td className="px-5 py-3">
                      <span className="font-medium">
                        {getGoalIcon(goal.goalType)} {goal.goalType}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-right mono font-medium">
                      ₹{fmt(Number(goal.targetAmount))}
                    </td>
                    <td className="px-5 py-3 mono text-xs">{goal.targetDate}</td>
                    <td className={
                      'px-5 py-3 font-medium ' +
                      (isOverdue ? 'text-danger' : days < 180 ? 'text-warn' : 'text-text')
                    }>
                      {isOverdue ? 'Overdue' : days + 'd'}
                    </td>
                    <td className="px-5 py-3">
                      <span className="mono text-xs font-medium">P{goal.priority}</span>
                    </td>
                    <td className="px-5 py-3">
                      <span className={'pill ' + getStatusPill(goal.status)}>{goal.status}</span>
                    </td>
                    <td className="px-5 py-3 text-right">
                      <Link
                        to={'/rm/clients/' + client.clientId}
                        className="text-xs text-primary hover:underline"
                      >
                        View →
                      </Link>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
