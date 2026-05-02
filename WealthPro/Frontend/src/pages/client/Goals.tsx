import { useState, useEffect } from 'react';
import { useAuth } from '@/auth/store';
import { getGoalsByClientId, createGoal, deleteGoal, updateGoalStatus } from '@/api/goals';
import { getRecommendationsByClientId } from '@/api/recommendations';
import { getAccountsByClientId } from '@/api/accounts';
import { getHoldingsByAccountId } from '@/api/holdings';
import { getAllSecurities } from '@/api/securities';
import { TableSkeleton } from '@/components/Skeleton';
import EmptyState from '@/components/EmptyState';
import { Target } from 'lucide-react';

const GOAL_TYPES = ['RETIREMENT', 'EDUCATION', 'WEALTH', 'CUSTOM'];

export default function Goals() {
  const user = useAuth((s) => s.user);
  const clientId = user?.clientId;

  const [goals, setGoals] = useState<any[]>([]);
  const [recos, setRecos] = useState<any[]>([]);
  const [portfolioValue, setPortfolioValue] = useState(0);
  const [loading, setLoading] = useState(true);

  // create modal
  const [showModal, setShowModal] = useState(false);
  const [goalType, setGoalType] = useState('WEALTH');
  const [targetAmount, setTargetAmount] = useState('');
  const [targetDate, setTargetDate] = useState('');
  const [priority, setPriority] = useState('1');
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (clientId) loadAll();
  }, [clientId]);

  async function loadAll() {
    if (!clientId) return;
    setLoading(true);
    try {
      const [goalsRes, recosRes, accsRes, secsRes] = await Promise.allSettled([
        getGoalsByClientId(clientId),
        getRecommendationsByClientId(clientId),
        getAccountsByClientId(clientId),
        getAllSecurities(),
      ]);

      if (goalsRes.status === 'fulfilled' && Array.isArray(goalsRes.value)) {
        setGoals(goalsRes.value);
      }
      if (recosRes.status === 'fulfilled' && Array.isArray(recosRes.value)) {
        setRecos(recosRes.value);
      }

      // calculate portfolio value from holdings
      const secMap: { [id: number]: any } = {};
      if (secsRes.status === 'fulfilled' && Array.isArray(secsRes.value)) {
        for (let i = 0; i < secsRes.value.length; i++) {
          secMap[secsRes.value[i].securityId] = secsRes.value[i];
        }
      }

      if (accsRes.status === 'fulfilled' && Array.isArray(accsRes.value) && accsRes.value.length > 0) {
        try {
          const holdings = await getHoldingsByAccountId(accsRes.value[0].accountId);
          if (Array.isArray(holdings)) {
            let total = 0;
            for (let i = 0; i < holdings.length; i++) {
              const h = holdings[i];
              const sec = secMap[h.securityId];
              const qty = Number(h.quantity) || 0;
              const price = sec?.currentPrice ? Number(sec.currentPrice) : (Number(h.avgCost) || 0);
              total += qty * price;
            }
            setPortfolioValue(total);
          }
        } catch (e) {
          setPortfolioValue(0);
        }
      }
    } catch (e) {
    }
    setLoading(false);
  }

  function openModal() {
    setGoalType('WEALTH');
    setTargetAmount('');
    setTargetDate('');
    setPriority('1');
    setFormError('');
    setShowModal(true);
  }

  function closeModal() {
    setShowModal(false);
    setFormError('');
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!clientId) return;

    const amt = parseFloat(targetAmount);
    if (isNaN(amt) || amt < 1000) {
      setFormError('Target amount must be at least ₹1,000');
      return;
    }
    if (amt > 100_000_000) {
      setFormError('Target amount cannot exceed ₹10 Crore');
      return;
    }
    // cap to 2 decimal places
    if (!/^\d+(\.\d{1,2})?$/.test(targetAmount.trim())) {
      setFormError('Target amount can have at most 2 decimal places');
      return;
    }
    if (!targetDate) {
      setFormError('Target date is required');
      return;
    }
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const chosen = new Date(targetDate);
    if (chosen <= today) {
      setFormError('Target date must be a future date');
      return;
    }
    const maxDate = new Date();
    maxDate.setFullYear(maxDate.getFullYear() + 50);
    if (chosen > maxDate) {
      setFormError('Target date cannot be more than 50 years in the future');
      return;
    }
    const priRaw = parseInt(priority, 10);
    if (priority.trim() === '' || isNaN(priRaw) || priRaw < 1 || priRaw > 10) {
      setFormError('Priority must be a whole number between 1 and 10');
      return;
    }
    const pri = priRaw;
    setFormError('');
    setSubmitting(true);
    try {
      await createGoal({
        clientId,
        goalType,
        targetAmount: amt,
        targetDate,
        priority: pri,
        status: 'ACTIVE',
      });
      closeModal();
      loadAll();
    } catch (err: any) {
      const data = err.response?.data;
      setFormError(typeof data === 'string' ? data : (data?.message || 'Failed to create goal'));
    }
    setSubmitting(false);
  }

  async function handleDelete(goalId: number) {
    if (!confirm('Delete this goal?')) return;
    try {
      await deleteGoal(goalId);
      loadAll();
    } catch (e) {
    }
  }

  async function handleMarkComplete(g: any) {
    if (!confirm('Mark "' + g.goalType + '" goal as completed?')) return;
    try {
      await updateGoalStatus(g.goalId, {
        clientId,
        goalType: g.goalType,
        targetAmount: Number(g.targetAmount),
        targetDate: g.targetDate,
        priority: g.priority,
        status: 'COMPLETED',
      });
      loadAll();
    } catch (e) {
    }
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

  function getRecoPill(s: string) {
    if (s === 'APPROVED') return 'pill-success';
    if (s === 'REJECTED') return 'pill-danger';
    if (s === 'SUBMITTED') return 'pill-info';
    return 'pill-warn';
  }

  function daysRemaining(dateStr: string) {
    const diff = new Date(dateStr).getTime() - Date.now();
    return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
  }

  function fmt(n: number) {
    return n.toLocaleString('en-IN', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  }

  function renderProposal(json: string) {
    let p: any;
    try { p = JSON.parse(json); } catch { return <p className="text-sm text-text-2">{json}</p>; }

    if (p.text) return <p className="text-sm text-text-2">{p.text}</p>;

    if (p.summary && Object.keys(p).length === 1)
      return <p className="text-sm text-text-2">{p.summary}</p>;

    return (
      <div className="text-sm space-y-2">
        {p.summary && <p className="text-text-2">{p.summary}</p>}
        <div className="flex flex-wrap gap-2">
          {p.targetReturn !== undefined && (
            <span className="bg-surface border border-border px-2 py-0.5 rounded text-xs">
              Target return: <span className="font-medium">{p.targetReturn}%</span>
            </span>
          )}
          {p.timeHorizon && (
            <span className="bg-surface border border-border px-2 py-0.5 rounded text-xs">
              Horizon: <span className="font-medium">{p.timeHorizon}</span>
            </span>
          )}
          {p.SIP && (
            <span className="bg-surface border border-border px-2 py-0.5 rounded text-xs">
              SIP: <span className="font-medium">₹{Number(p.SIP).toLocaleString('en-IN')}/mo</span>
            </span>
          )}
        </div>
        {p.allocation && (
          <div>
            <p className="text-xs text-text-3 mb-1">Allocation</p>
            <div className="flex flex-wrap gap-1.5">
              {Object.entries(p.allocation).map(([k, v]) => (
                <span key={k} className="bg-primary-soft text-primary px-2 py-0.5 rounded text-xs font-medium">
                  {k} {String(v)}%
                </span>
              ))}
            </div>
          </div>
        )}
      </div>
    );
  }

  if (!clientId) return <div className="p-10 text-center text-text-2">Loading...</div>;

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">My Goals</h1>
          <p className="text-sm text-text-2">
            Your financial targets. Current portfolio value:{' '}
            <span className="font-semibold mono">₹{fmt(portfolioValue)}</span>
          </p>
        </div>
        <button onClick={openModal} className="btn btn-primary btn-sm">+ New goal</button>
      </div>

      {loading ? (
        <div className="panel"><TableSkeleton rows={3} cols={4} /></div>
      ) : goals.length === 0 ? (
        <div className="panel mb-4">
          <EmptyState
            icon={<Target size={28} />}
            title="No goals yet"
            description="Set your first financial goal — retirement, education, or wealth creation."
            action={
              <button onClick={openModal} className="btn btn-primary btn-sm">
                + Create your first goal
              </button>
            }
          />
        </div>
      ) : (
        <div className="grid md:grid-cols-2 xl:grid-cols-3 gap-4 mb-6">
          {goals.map((g: any) => {
            const days = daysRemaining(g.targetDate);
            const isOverdue = days === 0 && g.status !== 'COMPLETED';
            const target = Number(g.targetAmount);
            // progress = portfolio value as a share of this goal's target
            const progressPct = target > 0 ? Math.min(100, (portfolioValue / target) * 100) : 0;
            const isReached = portfolioValue >= target;

            return (
              <div key={g.goalId} className={'panel ' + (g.status === 'COMPLETED' ? 'opacity-75' : '')}>
                <div className="panel-b">
                  {/* header */}
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <span className="text-2xl">{getGoalIcon(g.goalType)}</span>
                      <div>
                        <p className="font-semibold">{g.goalType}</p>
                        <p className="text-xs text-text-2">Priority {g.priority}</p>
                      </div>
                    </div>
                    <span className={'pill ' + getStatusPill(g.status)}>{g.status}</span>
                  </div>

                  {/* target */}
                  <div className="bg-surface rounded-lg p-3 mb-3">
                    <div className="flex justify-between mb-1">
                      <p className="text-xs text-text-2">Target amount</p>
                      <p className="text-xs font-medium mono">{progressPct.toFixed(1)}%</p>
                    </div>
                    <p className="text-xl font-semibold mono mb-2">₹{fmt(target)}</p>
                    {/* progress bar */}
                    <div className="w-full bg-border rounded-full h-1.5">
                      <div
                        className={'h-1.5 rounded-full ' + (g.status === 'COMPLETED' ? 'bg-success' : isReached ? 'bg-success' : 'bg-primary')}
                        style={{ width: progressPct + '%' }}
                      />
                    </div>
                    <p className="text-xs text-text-3 mt-1">
                      ₹{fmt(portfolioValue)} of ₹{fmt(target)} saved
                    </p>
                  </div>

                  {/* date + days */}
                  <div className="flex justify-between text-sm mb-3">
                    <div>
                      <p className="label">Target date</p>
                      <p className="font-medium">{g.targetDate}</p>
                    </div>
                    <div className="text-right">
                      <p className="label">Days remaining</p>
                      <p className={'font-medium ' + (isOverdue ? 'text-danger' : days < 180 ? 'text-warn' : 'text-text')}>
                        {isOverdue ? 'Overdue' : days + ' days'}
                      </p>
                    </div>
                  </div>

                  {/* actions */}
                  <div className="flex items-center gap-3">
                    {isReached && g.status === 'ACTIVE' && (
                      <button
                        onClick={() => handleMarkComplete(g)}
                        className="btn btn-success btn-sm"
                      >
                        ✓ Mark complete
                      </button>
                    )}
                    <button
                      onClick={() => handleDelete(g.goalId)}
                      className="text-xs text-danger hover:underline ml-auto"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* recommendations from RM */}
      <div className="panel">
        <div className="panel-h">
          <h3>RM Recommendations ({recos.length})</h3>
        </div>
        {recos.length === 0 ? (
          <div className="panel-b text-center text-text-2 py-8 text-sm">
            No recommendations from your RM yet. Your RM will send suggestions based on your goals.
          </div>
        ) : (
          <div className="divide-y divide-border-hairline">
            {recos.map((r: any) => (
              <div key={r.recoId} className="panel-b">
                <div className="flex items-center gap-2 mb-2">
                  <span className={'pill ' + getRecoPill(r.status)}>{r.status}</span>
                  <span className="pill pill-info">{r.riskClass}</span>
                  <span className="text-xs text-text-3 ml-auto">{r.proposedDate}</span>
                </div>
                {renderProposal(r.proposalJson)}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* create goal modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="px-6 py-4 border-b border-border-hairline flex items-center justify-between">
              <h3 className="font-semibold">New Financial Goal</h3>
              <button onClick={closeModal} className="text-text-3 text-xl">×</button>
            </div>
            <form onSubmit={handleCreate} className="p-6">
              <div className="mb-3">
                <label className="label block mb-1">Goal type</label>
                <div className="grid grid-cols-2 gap-2">
                  {GOAL_TYPES.map((t) => (
                    <button
                      key={t}
                      type="button"
                      onClick={() => setGoalType(t)}
                      className={
                        'py-2 px-3 text-sm rounded border font-medium text-left ' +
                        (goalType === t
                          ? 'bg-primary text-white border-primary'
                          : 'bg-white text-text border-border')
                      }
                    >
                      {getGoalIcon(t)} {t}
                    </button>
                  ))}
                </div>
              </div>

              <div className="mb-3">
                <label className="label block mb-1">Target amount (₹)</label>
                <input
                  className="input mono"
                  type="number"
                  min="1000"
                  placeholder="e.g. 5000000"
                  value={targetAmount}
                  onChange={(e) => setTargetAmount(e.target.value)}
                  autoFocus
                />
                <p className="text-xs text-text-3 mt-1">Minimum ₹1,000</p>
              </div>

              <div className="mb-3">
                <label className="label block mb-1">Target date</label>
                <input
                  className="input"
                  type="date"
                  min={new Date().toISOString().slice(0, 10)}
                  value={targetDate}
                  onChange={(e) => setTargetDate(e.target.value)}
                />
              </div>

              <div className="mb-4">
                <label className="label block mb-1">Priority (1 = highest)</label>
                <input
                  className="input mono"
                  type="number"
                  min="1"
                  max="10"
                  placeholder="1–10"
                  value={priority}
                  onChange={(e) => setPriority(e.target.value)}
                />
              </div>

              {formError && (
                <div className="pill pill-danger block w-full text-center mb-3">{formError}</div>
              )}

              <div className="flex justify-end gap-2">
                <button type="button" onClick={closeModal} className="btn btn-ghost">Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Saving...' : 'Create goal'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
