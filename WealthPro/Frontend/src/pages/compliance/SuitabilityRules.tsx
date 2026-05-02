import { useState, useEffect } from 'react';
import {
  getAllSuitabilityRules,
  createSuitabilityRule,
  updateSuitabilityRule,
  deleteSuitabilityRule,
} from '@/api/suitabilityRules';
import { TableSkeleton } from '@/components/Skeleton';
import EmptyState from '@/components/EmptyState';
import { Scale } from 'lucide-react';

const STATUSES = ['ACTIVE', 'INACTIVE'];

export default function SuitabilityRules() {
  const [rules, setRules]       = useState<any[]>([]);
  const [loading, setLoading]   = useState(true);
  const [globalMsg, setGlobalMsg] = useState('');
  const [globalErr, setGlobalErr] = useState('');

  // form / modal
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [saving, setSaving]       = useState(false);
  const [formError, setFormError] = useState('');

  // fields
  const [description, setDescription] = useState('');
  const [expression, setExpression]   = useState('');
  const [status, setStatus]           = useState('ACTIVE');

  // search
  const [search, setSearch]           = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');

  useEffect(() => { load(); }, []);

  async function load() {
    setLoading(true);
    try {
      const data = await getAllSuitabilityRules();
      if (Array.isArray(data)) setRules(data);
    } catch { /* service may be down — show empty state */ }
    setLoading(false);
  }

  const filtered = rules.filter((r) => {
    if (statusFilter !== 'ALL' && r.status !== statusFilter) return false;
    if (search) {
      const q = search.toLowerCase();
      if (
        !(r.description || '').toLowerCase().includes(q) &&
        !String(r.ruleId).includes(q)
      ) return false;
    }
    return true;
  });

  // ── modal helpers ──────────────────────────────────────────────────────────
  function openCreate() {
    setEditingId(null);
    setDescription('');
    setExpression('');
    setStatus('ACTIVE');
    setFormError('');
    setShowModal(true);
  }

  function openEdit(r: any) {
    setEditingId(r.ruleId);
    setDescription(r.description || '');
    setExpression(r.expression || '');
    setStatus(r.status || 'ACTIVE');
    setFormError('');
    setShowModal(true);
  }

  function closeModal() { setShowModal(false); setFormError(''); }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!description.trim()) { setFormError('Description is required'); return; }
    if (!expression.trim())  { setFormError('Expression is required'); return; }

    setSaving(true);
    setFormError('');
    try {
      const payload = { description: description.trim(), expression: expression.trim(), status };
      if (editingId !== null) {
        await updateSuitabilityRule(editingId, payload);
      } else {
        await createSuitabilityRule(payload);
      }
      closeModal();
      load();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.response?.data || 'Save failed';
      setFormError(typeof msg === 'string' ? msg : JSON.stringify(msg));
    }
    setSaving(false);
  }

  async function handleDelete(r: any) {
    if (!confirm(`Delete rule "${r.description}"?`)) return;
    setGlobalErr('');
    try {
      await deleteSuitabilityRule(r.ruleId);
      setGlobalMsg('Rule deleted.');
      setTimeout(() => setGlobalMsg(''), 3000);
      load();
    } catch {
      setGlobalErr('Delete failed. Rule may be referenced by compliance breaches.');
      setTimeout(() => setGlobalErr(''), 4000);
    }
  }

  async function toggleStatus(r: any) {
    const next = r.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    try {
      await updateSuitabilityRule(r.ruleId, { status: next });
      setRules((prev) =>
        prev.map((x) => x.ruleId === r.ruleId ? { ...x, status: next } : x)
      );
    } catch {
      setGlobalErr('Status update failed.');
      setTimeout(() => setGlobalErr(''), 3000);
    }
  }

  const activeCount   = rules.filter((r) => r.status === 'ACTIVE').length;
  const inactiveCount = rules.filter((r) => r.status === 'INACTIVE').length;

  return (
    <div>
      {/* header */}
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Suitability Rules</h1>
          <p className="text-sm text-text-2">
            Define the pre-trade suitability expressions evaluated during order validation.
            Active rules are checked for every order.
          </p>
        </div>
        <button onClick={openCreate} className="btn btn-primary btn-sm">+ New Rule</button>
      </div>

      {globalMsg && <div className="pill pill-success block text-center mb-4 py-2">{globalMsg}</div>}
      {globalErr && <div className="pill pill-danger  block text-center mb-4 py-2">{globalErr}</div>}

      {/* KPI strip */}
      <div className="grid grid-cols-3 gap-3 mb-5">
        <div className="panel"><div className="panel-b py-3 text-center">
          <p className="label">Total Rules</p>
          <p className="text-2xl font-bold mono mt-1">{rules.length}</p>
        </div></div>
        <div className="panel"><div className="panel-b py-3 text-center">
          <p className="label">Active</p>
          <p className="text-2xl font-bold mono mt-1 text-success">{activeCount}</p>
        </div></div>
        <div className="panel"><div className="panel-b py-3 text-center">
          <p className="label">Inactive</p>
          <p className="text-2xl font-bold mono mt-1 text-text-3">{inactiveCount}</p>
        </div></div>
      </div>

      {/* filters */}
      <div className="panel mb-4">
        <div className="panel-b py-3 flex gap-3 items-center flex-wrap">
          <input
            className="input max-w-xs"
            type="text"
            placeholder="Search by description or ID..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <div className="flex gap-1 ml-auto">
            {(['ALL', 'ACTIVE', 'INACTIVE'] as const).map((s) => (
              <button
                key={s}
                onClick={() => setStatusFilter(s)}
                className={
                  'px-3 py-1.5 text-xs font-medium rounded border ' +
                  (statusFilter === s
                    ? 'bg-primary text-white border-primary'
                    : 'bg-white text-text-2 border-border')
                }
              >
                {s}
              </button>
            ))}
          </div>
          <span className="text-xs text-text-2">{filtered.length} rules</span>
        </div>
      </div>

      {/* rules table */}
      <div className="panel">
        {loading ? (
          <TableSkeleton rows={5} cols={5} />
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<Scale size={26} />}
            title="No suitability rules"
            description="Create rules to enforce pre-trade suitability checks on all orders."
            action={<button onClick={openCreate} className="btn btn-primary btn-sm">+ New Rule</button>}
          />
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">ID</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Description</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2 w-1/3">Expression</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((r: any) => (
                <tr key={r.ruleId} className="border-t border-border-hairline hover:bg-surface">
                  <td className="px-5 py-3 mono text-xs text-text-2">{r.ruleId}</td>
                  <td className="px-5 py-3 font-medium max-w-xs">
                    <p>{r.description}</p>
                  </td>
                  <td className="px-5 py-3">
                    <code className="text-xs bg-surface border border-border-hairline px-2 py-1 rounded font-mono break-all">
                      {r.expression}
                    </code>
                  </td>
                  <td className="px-5 py-3">
                    <button
                      onClick={() => toggleStatus(r)}
                      className={'pill cursor-pointer ' + (r.status === 'ACTIVE' ? 'pill-success' : 'pill-info')}
                      title="Click to toggle"
                    >
                      {r.status}
                    </button>
                  </td>
                  <td className="px-5 py-3 text-right">
                    <div className="flex gap-2 justify-end">
                      <button onClick={() => openEdit(r)} className="btn btn-ghost btn-sm">Edit</button>
                      <button onClick={() => handleDelete(r)} className="btn btn-danger btn-sm">Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* ── How expressions work ── */}
      <div className="panel mt-4">
        <div className="panel-h"><h3>Expression Syntax Reference</h3></div>
        <div className="panel-b">
          <p className="text-xs text-text-2 mb-3">
            Expressions are evaluated by the pre-trade check engine against each order's context.
            Use dot-notation to reference order and client fields.
          </p>
          <div className="grid md:grid-cols-2 gap-3 text-xs">
            {[
              { expr: 'client.riskClass == "CONSERVATIVE" && order.assetClass != "EQUITY"', label: 'Conservative clients cannot buy equities' },
              { expr: 'order.quantity * order.limitPrice <= client.cashBalance', label: 'Sufficient cash for limit order' },
              { expr: 'order.side != "SELL" || holding.quantity >= order.quantity', label: 'Cannot sell more than held quantity' },
              { expr: 'client.kycStatus == "Verified"', label: 'KYC must be verified before trading' },
            ].map((ex) => (
              <div key={ex.expr} className="bg-surface rounded-lg p-3 border border-border-hairline">
                <code className="block text-xs mono text-primary mb-1 break-all">{ex.expr}</code>
                <p className="text-text-2">{ex.label}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* ── Create / Edit Modal ── */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-lg w-full">
            <div className="px-6 py-4 border-b border-border-hairline flex items-center justify-between">
              <h3 className="font-semibold">{editingId !== null ? 'Edit Rule' : 'New Suitability Rule'}</h3>
              <button onClick={closeModal} className="text-text-3 text-xl leading-none">×</button>
            </div>

            <form onSubmit={handleSubmit} className="p-6">
              <div className="mb-3">
                <label className="label block mb-1">Description <span className="text-danger">*</span></label>
                <input
                  className="input"
                  type="text"
                  placeholder="e.g. Conservative clients cannot hold equity > 20%"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  autoFocus
                />
              </div>

              <div className="mb-3">
                <label className="label block mb-1">
                  Expression <span className="text-danger">*</span>
                  <span className="text-text-3 font-normal ml-1">— evaluated at pre-trade check time</span>
                </label>
                <textarea
                  className="input mono text-xs resize-none"
                  rows={3}
                  placeholder={'e.g. client.riskClass != "CONSERVATIVE" || order.assetClass != "EQUITY"'}
                  value={expression}
                  onChange={(e) => setExpression(e.target.value)}
                />
                <p className="text-xs text-text-3 mt-1">
                  Use dot-notation: client.*, order.*, holding.*. Boolean expression that must evaluate to true for the order to pass.
                </p>
              </div>

              <div className="mb-4">
                <label className="label block mb-1">Status</label>
                <div className="flex gap-2">
                  {STATUSES.map((st) => (
                    <button
                      key={st}
                      type="button"
                      onClick={() => setStatus(st)}
                      className={
                        'flex-1 py-2 text-sm rounded border font-medium ' +
                        (status === st
                          ? 'bg-primary text-white border-primary'
                          : 'bg-white text-text border-border')
                      }
                    >
                      {st}
                    </button>
                  ))}
                </div>
              </div>

              {formError && (
                <div className="pill pill-danger block text-center w-full mb-3">{formError}</div>
              )}

              <div className="flex justify-end gap-2">
                <button type="button" onClick={closeModal} className="btn btn-ghost">Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saving...' : editingId !== null ? 'Save Changes' : 'Create Rule'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
