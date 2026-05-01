import { useState, useEffect } from 'react';
import {
  getAllModelPortfolios,
  createModelPortfolio,
  updateModelPortfolio,
  deleteModelPortfolio,
} from '@/api/recommendations';

const RISK_CLASSES = ['CONSERVATIVE', 'BALANCED', 'AGGRESSIVE'];
const STATUSES = ['ACTIVE', 'INACTIVE'];

export default function ModelPortfolios() {
  const [portfolios, setPortfolios] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // form state
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [name, setName] = useState('');
  const [riskClass, setRiskClass] = useState('BALANCED');
  const [status, setStatus] = useState('ACTIVE');
  const [allocationJson, setAllocationJson] = useState('');
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');

  useEffect(() => {
    loadPortfolios();
  }, []);

  async function loadPortfolios() {
    setLoading(true);
    try {
      const data = await getAllModelPortfolios();
      if (Array.isArray(data)) setPortfolios(data);
    } catch (e) {
    }
    setLoading(false);
  }

  function openCreate() {
    setEditingId(null);
    setName('');
    setRiskClass('BALANCED');
    setStatus('ACTIVE');
    setAllocationJson('');
    setFormError('');
    setShowForm(true);
  }

  function openEdit(p: any) {
    setEditingId(p.portfolioId);
    setName(p.name || '');
    setRiskClass(p.riskClass || 'BALANCED');
    setStatus(p.status || 'ACTIVE');
    setAllocationJson(p.allocationJson || '');
    setFormError('');
    setShowForm(true);
  }

  function closeForm() {
    setShowForm(false);
    setFormError('');
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!name.trim()) {
      setFormError('Name is required');
      return;
    }

    // validate JSON if provided
    if (allocationJson.trim()) {
      try {
        JSON.parse(allocationJson);
      } catch {
        setFormError('Allocation JSON is not valid JSON. Example: {"EQUITY": 60, "BOND": 40}');
        return;
      }
    }

    setSaving(true);
    setFormError('');
    try {
      const payload = {
        name: name.trim(),
        riskClass,
        status,
        allocationJson: allocationJson.trim() || null,
      };

      if (editingId !== null) {
        await updateModelPortfolio(editingId, payload);
      } else {
        await createModelPortfolio(payload);
      }

      closeForm();
      loadPortfolios();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.response?.data || 'Save failed. Please try again.';
      setFormError(typeof msg === 'string' ? msg : JSON.stringify(msg));
    }
    setSaving(false);
  }

  async function handleDelete(p: any) {
    if (!confirm(`Delete model portfolio "${p.name}"? This cannot be undone.`)) return;
    try {
      await deleteModelPortfolio(p.portfolioId);
      loadPortfolios();
    } catch {
      setError('Delete failed. Please try again.');
      setTimeout(() => setError(''), 3000);
    }
  }

  function getRiskPill(rc: string) {
    if (rc === 'CONSERVATIVE') return 'pill-info';
    if (rc === 'BALANCED')     return 'pill-warn';
    return 'pill-danger';
  }

  function parseAlloc(raw: string) {
    try { return JSON.parse(raw); } catch { return null; }
  }

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Model Portfolios</h1>
          <p className="text-sm text-text-2">
            Manage model portfolios used by RMs to build client recommendations.
          </p>
        </div>
        <button onClick={openCreate} className="btn btn-primary btn-sm">+ New Portfolio</button>
      </div>

      {error && (
        <div className="pill pill-danger block mb-4 text-center">{error}</div>
      )}

      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading model portfolios...</div>
        ) : portfolios.length === 0 ? (
          <div className="panel-b text-center py-10">
            <p className="text-3xl mb-2">📊</p>
            <p className="font-semibold">No model portfolios yet</p>
            <p className="text-sm text-text-2 mt-1">
              Create a model portfolio to let RMs build client recommendations.
            </p>
            <button onClick={openCreate} className="btn btn-primary btn-sm mt-3">+ Create first portfolio</button>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Name</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Risk Class</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Allocation</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {portfolios.map((p: any) => {
                const alloc = p.allocationJson ? parseAlloc(p.allocationJson) : null;
                return (
                  <tr key={p.portfolioId} className="border-t border-border-hairline">
                    <td className="px-5 py-3 font-medium">{p.name}</td>
                    <td className="px-5 py-3">
                      <span className={'pill ' + getRiskPill(p.riskClass)}>{p.riskClass}</span>
                    </td>
                    <td className="px-5 py-3">
                      <span className={'pill ' + (p.status === 'ACTIVE' ? 'pill-success' : 'pill-info')}>
                        {p.status}
                      </span>
                    </td>
                    <td className="px-5 py-3">
                      {alloc ? (
                        <div className="flex flex-wrap gap-1">
                          {Object.entries(alloc).map(([asset, pct]) => (
                            <span key={asset} className="text-xs bg-surface border border-border rounded px-2 py-0.5">
                              <span className="font-medium">{asset}</span>
                              <span className="text-text-2 ml-1">{String(pct)}%</span>
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span className="text-text-3 text-xs italic">No allocation defined</span>
                      )}
                    </td>
                    <td className="px-5 py-3 text-right">
                      <div className="flex gap-2 justify-end">
                        <button onClick={() => openEdit(p)} className="btn btn-ghost btn-sm">Edit</button>
                        <button onClick={() => handleDelete(p)} className="btn btn-danger btn-sm">Delete</button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* Create / Edit modal */}
      {showForm && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="px-6 py-4 border-b border-border-hairline flex items-center justify-between">
              <h3 className="font-semibold">{editingId !== null ? 'Edit Model Portfolio' : 'New Model Portfolio'}</h3>
              <button onClick={closeForm} className="text-text-3 text-xl">×</button>
            </div>
            <form onSubmit={handleSubmit} className="p-6">
              <div className="mb-3">
                <label className="label block mb-1">Name</label>
                <input
                  className="input"
                  type="text"
                  placeholder="e.g. Balanced Growth Fund"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  autoFocus
                />
              </div>

              <div className="mb-3">
                <label className="label block mb-1">Risk Class</label>
                <select className="input" value={riskClass} onChange={(e) => setRiskClass(e.target.value)}>
                  {RISK_CLASSES.map((rc) => <option key={rc} value={rc}>{rc}</option>)}
                </select>
              </div>

              <div className="mb-3">
                <label className="label block mb-1">Status</label>
                <select className="input" value={status} onChange={(e) => setStatus(e.target.value)}>
                  {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
                </select>
              </div>

              <div className="mb-4">
                <label className="label block mb-1">Allocation JSON <span className="text-text-3">(optional)</span></label>
                <textarea
                  className="input mono text-xs resize-none"
                  rows={3}
                  placeholder={'{"EQUITY": 60, "BOND": 30, "CASH": 10}'}
                  value={allocationJson}
                  onChange={(e) => setAllocationJson(e.target.value)}
                />
                <p className="text-xs text-text-3 mt-1">Enter asset class percentages as JSON. Must sum to 100.</p>
              </div>

              {formError && (
                <div className="pill pill-danger block mb-3 text-center w-full">{formError}</div>
              )}

              <div className="flex justify-end gap-2">
                <button type="button" onClick={closeForm} className="btn btn-ghost">Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saving...' : editingId !== null ? 'Save Changes' : 'Create Portfolio'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
