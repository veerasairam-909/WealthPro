import { useState, useEffect } from 'react';
import { getAllAmlFlags, createAmlFlag, reviewAmlFlag, deleteAmlFlag } from '@/api/amlFlags';

const FLAG_TYPES = ['SUSPICIOUS_TRANSACTION', 'HIGH_VALUE_TRANSFER', 'UNUSUAL_PATTERN', 'WATCHLIST_MATCH', 'MANUAL'];
const STATUSES   = ['OPEN', 'REVIEWED', 'CLEARED', 'ESCALATED'];

export default function AmlFlags() {
  const [flags, setFlags]         = useState<any[]>([]);
  const [loading, setLoading]     = useState(true);
  const [filterStatus, setFilter] = useState('');
  const [error, setError]         = useState('');

  // create form
  const [showCreate, setShowCreate]     = useState(false);
  const [clientId, setClientId]         = useState('');
  const [flagType, setFlagType]         = useState('SUSPICIOUS_TRANSACTION');
  const [description, setDescription]   = useState('');
  const [notes, setNotes]               = useState('');
  const [saving, setSaving]             = useState(false);
  const [formError, setFormError]       = useState('');

  // review modal
  const [reviewFlag, setReviewFlag]     = useState<any | null>(null);
  const [reviewStatus, setReviewStatus] = useState('REVIEWED');
  const [reviewNotes, setReviewNotes]   = useState('');
  const [reviewSaving, setReviewSaving] = useState(false);

  useEffect(() => {
    loadFlags();
  }, [filterStatus]);

  async function loadFlags() {
    setLoading(true);
    setError('');
    try {
      const data = await getAllAmlFlags(filterStatus || undefined);
      if (Array.isArray(data)) setFlags(data);
    } catch {
      setError('Failed to load AML flags.');
    }
    setLoading(false);
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!clientId.trim() || isNaN(Number(clientId))) {
      setFormError('Valid Client ID is required');
      return;
    }
    if (!description.trim()) {
      setFormError('Description is required');
      return;
    }
    setSaving(true);
    setFormError('');
    try {
      await createAmlFlag({
        clientId: Number(clientId),
        flagType,
        description: description.trim(),
        notes: notes.trim() || undefined,
      });
      setShowCreate(false);
      setClientId('');
      setDescription('');
      setNotes('');
      loadFlags();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.response?.data || 'Failed to create flag.';
      setFormError(typeof msg === 'string' ? msg : JSON.stringify(msg));
    }
    setSaving(false);
  }

  async function handleReview(e: React.FormEvent) {
    e.preventDefault();
    if (!reviewFlag) return;
    setReviewSaving(true);
    try {
      await reviewAmlFlag(reviewFlag.amlFlagId, {
        status: reviewStatus,
        notes: reviewNotes.trim() || undefined,
      });
      setReviewFlag(null);
      setError('');
      loadFlags();
    } catch {
      setError('Failed to update flag.');
    }
    setReviewSaving(false);
  }

  async function handleDelete(f: any) {
    if (!confirm(`Delete AML flag #${f.amlFlagId}? This cannot be undone.`)) return;
    try {
      await deleteAmlFlag(f.amlFlagId);
      loadFlags();
    } catch {
      setError('Failed to delete flag.');
      setTimeout(() => setError(''), 3000);
    }
  }

  function statusPill(s: string) {
    if (s === 'OPEN')      return 'pill-danger';
    if (s === 'ESCALATED') return 'pill-warn';
    if (s === 'CLEARED')   return 'pill-success';
    return 'pill-info';
  }

  function typePill(t: string) {
    if (t === 'WATCHLIST_MATCH')       return 'pill-danger';
    if (t === 'HIGH_VALUE_TRANSFER')   return 'pill-warn';
    if (t === 'SUSPICIOUS_TRANSACTION') return 'pill-warn';
    return 'pill-info';
  }

  const openCount      = flags.filter((f) => f.status === 'OPEN').length;
  const escalatedCount = flags.filter((f) => f.status === 'ESCALATED').length;

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">AML Flags</h1>
          <p className="text-sm text-text-2">
            Anti-money laundering flags raised against clients.
          </p>
        </div>
        <button onClick={() => setShowCreate(true)} className="btn btn-primary btn-sm">
          + Raise Flag
        </button>
      </div>

      {error && (
        <div className="pill pill-danger block mb-4 text-center">{error}</div>
      )}

      {/* Summary pills */}
      <div className="grid grid-cols-3 gap-4 mb-5">
        <div className="panel">
          <div className="panel-b text-center">
            <p className="text-2xl font-bold text-danger">{openCount}</p>
            <p className="label mt-1">Open</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b text-center">
            <p className="text-2xl font-bold text-warn">{escalatedCount}</p>
            <p className="label mt-1">Escalated</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b text-center">
            <p className="text-2xl font-bold text-text">{flags.length}</p>
            <p className="label mt-1">Total</p>
          </div>
        </div>
      </div>

      {/* Filter bar */}
      <div className="flex gap-3 mb-4 flex-wrap">
        <button
          onClick={() => setFilter('')}
          className={'btn btn-sm ' + (!filterStatus ? 'btn-primary' : 'btn-ghost')}
        >
          All
        </button>
        {STATUSES.map((s) => (
          <button
            key={s}
            onClick={() => setFilter(s)}
            className={'btn btn-sm ' + (filterStatus === s ? 'btn-primary' : 'btn-ghost')}
          >
            {s}
          </button>
        ))}
      </div>

      <div className="panel">
        {loading ? (
          <div className="panel-b text-center py-10 text-text-2">Loading AML flags...</div>
        ) : flags.length === 0 ? (
          <div className="panel-b text-center py-10">
            <p className="text-3xl mb-2">🛡️</p>
            <p className="font-semibold">No AML flags found</p>
            <p className="text-sm text-text-2 mt-1">
              {filterStatus ? `No flags with status "${filterStatus}".` : 'No AML flags have been raised yet.'}
            </p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Flag ID</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Client</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Type</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Description</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Flagged</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {flags.map((f: any) => (
                <tr key={f.amlFlagId} className="border-t border-border-hairline">
                  <td className="px-5 py-3 mono text-xs text-text-3">#{f.amlFlagId}</td>
                  <td className="px-5 py-3 font-medium">Client #{f.clientId}</td>
                  <td className="px-5 py-3">
                    <span className={'pill ' + typePill(f.flagType)}>
                      {f.flagType?.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="px-5 py-3 max-w-xs truncate text-text-2">{f.description}</td>
                  <td className="px-5 py-3">
                    <span className={'pill ' + statusPill(f.status)}>{f.status}</span>
                  </td>
                  <td className="px-5 py-3 text-text-2">
                    {f.flaggedDate
                      ? new Date(f.flaggedDate).toLocaleDateString('en-IN', {
                          day: '2-digit', month: 'short', year: 'numeric',
                        })
                      : '—'}
                  </td>
                  <td className="px-5 py-3 text-right">
                    <div className="flex gap-2 justify-end">
                      {f.status !== 'CLEARED' && (
                        <button
                          onClick={() => {
                            setReviewFlag(f);
                            setReviewStatus('REVIEWED');
                            setReviewNotes(f.notes || '');
                          }}
                          className="btn btn-ghost btn-sm"
                        >
                          Review
                        </button>
                      )}
                      <button onClick={() => handleDelete(f)} className="btn btn-danger btn-sm">
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Create Flag Modal */}
      {showCreate && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="px-6 py-4 border-b border-border-hairline flex items-center justify-between">
              <h3 className="font-semibold">Raise AML Flag</h3>
              <button onClick={() => setShowCreate(false)} className="text-text-3 text-xl">×</button>
            </div>
            <form onSubmit={handleCreate} className="p-6">
              <div className="mb-3">
                <label className="label block mb-1">Client ID</label>
                <input
                  className="input"
                  type="number"
                  placeholder="e.g. 42"
                  value={clientId}
                  onChange={(e) => setClientId(e.target.value)}
                  autoFocus
                />
              </div>
              <div className="mb-3">
                <label className="label block mb-1">Flag Type</label>
                <select className="input" value={flagType} onChange={(e) => setFlagType(e.target.value)}>
                  {FLAG_TYPES.map((t) => (
                    <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
                  ))}
                </select>
              </div>
              <div className="mb-3">
                <label className="label block mb-1">Description</label>
                <textarea
                  className="input resize-none"
                  rows={3}
                  placeholder="Describe the suspicious activity..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                />
              </div>
              <div className="mb-4">
                <label className="label block mb-1">Notes <span className="text-text-3">(optional)</span></label>
                <textarea
                  className="input resize-none"
                  rows={2}
                  placeholder="Additional notes..."
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                />
              </div>
              {formError && (
                <div className="pill pill-danger block mb-3 text-center w-full">{formError}</div>
              )}
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setShowCreate(false)} className="btn btn-ghost">Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Raising...' : 'Raise Flag'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Review Modal */}
      {reviewFlag && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="px-6 py-4 border-b border-border-hairline flex items-center justify-between">
              <h3 className="font-semibold">Review AML Flag #{reviewFlag.amlFlagId}</h3>
              <button onClick={() => setReviewFlag(null)} className="text-text-3 text-xl">×</button>
            </div>
            <form onSubmit={handleReview} className="p-6">
              <div className="mb-3 bg-surface rounded-lg p-3 text-sm">
                <p className="label">Client #{reviewFlag.clientId}</p>
                <p className="mt-1">{reviewFlag.description}</p>
              </div>
              <div className="mb-3">
                <label className="label block mb-1">Update Status</label>
                <select
                  className="input"
                  value={reviewStatus}
                  onChange={(e) => setReviewStatus(e.target.value)}
                >
                  <option value="REVIEWED">REVIEWED</option>
                  <option value="CLEARED">CLEARED</option>
                  <option value="ESCALATED">ESCALATED</option>
                </select>
              </div>
              <div className="mb-4">
                <label className="label block mb-1">Notes <span className="text-text-3">(optional)</span></label>
                <textarea
                  className="input resize-none"
                  rows={3}
                  placeholder="Add review notes..."
                  value={reviewNotes}
                  onChange={(e) => setReviewNotes(e.target.value)}
                />
              </div>
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setReviewFlag(null)} className="btn btn-ghost">Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={reviewSaving}>
                  {reviewSaving ? 'Saving...' : 'Save Review'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
