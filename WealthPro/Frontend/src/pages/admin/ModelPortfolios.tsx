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
    setEditingId(p.modelId ?? p.portfolioId);
    setName(p.name || '');
    setRiskClass(p.riskClass || 'BALANCED');
    setStatus(p.status || 'ACTIVE');
    setAllocationJson(p.weightsJson || '');   // backend field is weightsJson
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

    // validate JSON + suitability constraints
    if (allocationJson.trim()) {
      let parsed: Record<string, number>;
      try {
        parsed = JSON.parse(allocationJson);
      } catch {
        setFormError('Allocation JSON is not valid JSON. Example: {"EQUITY": 60, "BOND": 40}');
        return;
      }

      const assetClasses = Object.keys(parsed).map((k) => k.toUpperCase());

      // Fraction guard — catch common mistake of entering 0.6 instead of 60
      const values = Object.values(parsed).map(Number);
      const hasfractions = values.some((v) => v > 0 && v < 1);
      if (hasfractions) {
        setFormError(
          'Values must be whole percentages (e.g. 60), not decimals (e.g. 0.6). ' +
          'Example: {"BOND": 60, "MUTUAL_FUND": 25, "ETF": 15}'
        );
        return;
      }

      // Sum check
      const total = values.reduce((s, v) => s + v, 0);
      if (Math.round(total) !== 100) {
        setFormError(`Allocation percentages must sum to 100%. Current total: ${total}%`);
        return;
      }

      // Suitability rule constraints
      if (riskClass === 'CONSERVATIVE' && assetClasses.includes('EQUITY')) {
        setFormError(
          '⛔ Conservative portfolio cannot contain EQUITY — Conservative clients are blocked from buying equity by Suitability Rule 1. Remove EQUITY from the allocation.'
        );
        return;
      }
      if (riskClass === 'CONSERVATIVE' && assetClasses.includes('STRUCTURED')) {
        setFormError(
          '⛔ Conservative portfolio cannot contain STRUCTURED products — Structured products are UHNI-only (Rule 6). Remove STRUCTURED from the allocation.'
        );
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
        weightsJson: allocationJson.trim(),  // backend expects weightsJson
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
                const alloc = p.weightsJson ? parseAlloc(p.weightsJson) : null;

                // Detect stale/invalid allocation — Conservative portfolio
                // should never contain EQUITY or STRUCTURED (old data from before
                // backend validation was added). Flag those rows so Admin can fix them.
                const invalidAssets: string[] = [];
                if (p.riskClass === 'CONSERVATIVE' && alloc) {
                  const keys = Object.keys(alloc).map((k) => k.toUpperCase());
                  if (keys.includes('EQUITY'))     invalidAssets.push('EQUITY');
                  if (keys.includes('STRUCTURED')) invalidAssets.push('STRUCTURED');
                }
                const hasInvalidAlloc = invalidAssets.length > 0;

                return (
                  <tr key={p.portfolioId} className={'border-t border-border-hairline' + (hasInvalidAlloc ? ' bg-danger-soft/40' : '')}>
                    <td className="px-5 py-3">
                      <p className="font-medium">{p.name}</p>
                      {hasInvalidAlloc && (
                        <p className="text-xs text-danger font-medium mt-0.5">
                          ⚠ Contains {invalidAssets.join(', ')} — must be removed for Conservative. Click Edit to fix.
                        </p>
                      )}
                    </td>
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
                          {Object.entries(alloc).map(([asset, pct]) => {
                            const isInvalid = invalidAssets.includes((asset as string).toUpperCase());
                            return (
                              <span
                                key={asset}
                                className={
                                  'text-xs rounded px-2 py-0.5 border ' +
                                  (isInvalid
                                    ? 'bg-danger/10 border-danger/40 text-danger line-through'
                                    : 'bg-surface border-border')
                                }
                              >
                                <span className="font-medium">{asset}</span>
                                <span className="ml-1 opacity-70">{String(pct)}%</span>
                                {isInvalid && <span className="ml-1">⛔</span>}
                              </span>
                            );
                          })}
                        </div>
                      ) : (
                        <span className="text-text-3 text-xs italic">No allocation defined</span>
                      )}
                    </td>
                    <td className="px-5 py-3 text-right">
                      <div className="flex gap-2 justify-end">
                        <button onClick={() => openEdit(p)} className={'btn btn-sm ' + (hasInvalidAlloc ? 'btn-danger' : 'btn-ghost')}>
                          {hasInvalidAlloc ? '⚠ Fix' : 'Edit'}
                        </button>
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
                <label className="label block mb-1">Allocation <span className="text-text-3">(% per asset class, must sum to 100)</span></label>
                <textarea
                  className="input mono text-xs resize-none"
                  rows={3}
                  placeholder={'{"EQUITY": 60, "BOND": 30, "MUTUAL_FUND": 10}'}
                  value={allocationJson}
                  onChange={(e) => setAllocationJson(e.target.value)}
                />
                {/* Suitability hint — changes based on selected risk class */}
                {riskClass === 'CONSERVATIVE' && (
                  <div className="mt-1.5 bg-danger-soft border border-danger/20 rounded px-2 py-1.5 text-xs text-danger">
                    ⛔ Conservative portfolios cannot contain <strong>EQUITY</strong> or <strong>STRUCTURED</strong>.
                    Allowed: BOND, MUTUAL_FUND, ETF.
                  </div>
                )}
                {riskClass === 'BALANCED' && (
                  <p className="text-xs text-text-3 mt-1">
                    Allowed: EQUITY, BOND, MUTUAL_FUND, ETF. Avoid STRUCTURED (UHNI-only).
                  </p>
                )}
                {riskClass === 'AGGRESSIVE' && (
                  <p className="text-xs text-text-3 mt-1">
                    Allowed: EQUITY, BOND, MUTUAL_FUND, ETF. Avoid STRUCTURED unless targeting UHNI clients.
                  </p>
                )}
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
