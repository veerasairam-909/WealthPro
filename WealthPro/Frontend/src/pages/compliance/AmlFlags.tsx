import { useState, useEffect } from 'react';
import { getAllAmlFlags, createAmlFlag, reviewAmlFlag, deleteAmlFlag, requestClosureAmlFlag } from '@/api/amlFlags';
import { createNotification } from '@/api/notifications';
import { getAllUsers } from '@/api/admin';
import { useAuth } from '@/auth/store';

const FLAG_TYPES = ['SUSPICIOUS_TRANSACTION', 'HIGH_VALUE_TRANSFER', 'UNUSUAL_PATTERN', 'WATCHLIST_MATCH', 'MANUAL'];
const STATUSES   = ['OPEN', 'REVIEWED', 'CLOSED'];

interface StaffEntry { username: string; userId: number; role: string; }

async function loadStaff(): Promise<StaffEntry[]> {
  try {
    const data = await getAllUsers();
    if (Array.isArray(data) && data.length > 0) {
      const staff = (data as any[])
        .map((u) => ({
          username: String(u.username ?? ''),
          userId:   Number(u.userId ?? 0),
          role:     String(u.roles ?? u.role ?? '').replace('ROLE_', '').toUpperCase(),
        }))
        .filter((e) => e.userId > 0 && e.role === 'RM');

      try {
        const map = JSON.parse(localStorage.getItem('wp_user_id_map') || '{}');
        for (const s of staff) map[s.username] = { userId: s.userId, role: s.role };
        localStorage.setItem('wp_user_id_map', JSON.stringify(map));
      } catch { /* storage full */ }

      if (staff.length > 0) return staff;
    }
  } catch { /* fall through */ }

  try {
    const raw = localStorage.getItem('wp_user_id_map');
    if (!raw) return [];
    const map = JSON.parse(raw);
    return Object.entries(map)
      .map(([username, val]: [string, any]) => ({
        username,
        userId: typeof val === 'object' ? Number(val.userId) : Number(val),
        role:   typeof val === 'object' ? String(val.role)   : '',
      }))
      .filter((e) => e.userId > 0 && e.role === 'RM');
  } catch { return []; }
}

export default function AmlFlags() {
  const user = useAuth((s) => s.user);
  const isRM         = user?.role === 'RM';
  const isCompliance = user?.role === 'COMPLIANCE' || user?.role === 'ADMIN';

  const [flags, setFlags]         = useState<any[]>([]);
  const [loading, setLoading]     = useState(true);
  const [filterStatus, setFilter] = useState('');
  const [error, setError]         = useState('');

  // ── create form ──────────────────────────────────────────────────────────
  const [showCreate, setShowCreate]   = useState(false);
  const [clientId, setClientId]       = useState('');
  const [flagType, setFlagType]       = useState('SUSPICIOUS_TRANSACTION');
  const [description, setDescription] = useState('');
  const [notes, setNotes]             = useState('');
  const [saving, setSaving]           = useState(false);
  const [formError, setFormError]     = useState('');

  // ── review modal (compliance only) ───────────────────────────────────────
  const [reviewFlag, setReviewFlag]     = useState<any | null>(null);
  const [reviewStatus, setReviewStatus] = useState('REVIEWED');
  const [reviewNotes, setReviewNotes]   = useState('');
  const [reviewSaving, setReviewSaving] = useState(false);

  // ── notify RM modal (compliance only) ────────────────────────────────────
  const [notifyFlag, setNotifyFlag]       = useState<any | null>(null);
  const [staffList, setStaffList]         = useState<StaffEntry[]>([]);
  const [staffLoading, setStaffLoading]   = useState(false);
  const [notifyUserId, setNotifyUserId]   = useState('');
  const [notifyMsg, setNotifyMsg]         = useState('');
  const [notifySending, setNotifySending] = useState(false);
  const [notifyDone, setNotifyDone]       = useState('');

  useEffect(() => { loadFlags(); }, [filterStatus]);

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

  // ── create flag ───────────────────────────────────────────────────────────
  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!clientId.trim() || isNaN(Number(clientId)) || Number(clientId) <= 0 || !Number.isInteger(Number(clientId))) {
      setFormError('Client ID must be a positive whole number (e.g. 42)');
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
        clientId:       Number(clientId),
        flagType,
        description:    description.trim(),
        notes:          notes.trim() || undefined,
        raisedByUserId: user?.userId ?? undefined,
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

  // ── compliance: review / update status ───────────────────────────────────
  async function handleReview(e: React.FormEvent) {
    e.preventDefault();
    if (!reviewFlag) return;
    setReviewSaving(true);
    try {
      await reviewAmlFlag(reviewFlag.amlFlagId, {
        status: reviewStatus,
        notes:  reviewNotes.trim() || undefined,
      });
      setReviewFlag(null);
      loadFlags();
    } catch {
      setError('Failed to update flag.');
      setTimeout(() => setError(''), 3000);
    }
    setReviewSaving(false);
  }

  // ── RM: request closure (notifies compliance — does NOT change status) ────
  const [closureRequestedId, setClosureRequestedId] = useState<number | null>(null);

  async function handleRequestClosure(f: any) {
    try {
      await requestClosureAmlFlag(f.amlFlagId);
      setClosureRequestedId(f.amlFlagId);
      setTimeout(() => setClosureRequestedId(null), 3000);
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to send closure request.';
      setError(typeof msg === 'string' ? msg : 'Failed to send closure request.');
      setTimeout(() => setError(''), 4000);
    }
  }

  // ── compliance: notify RM modal ───────────────────────────────────────────
  async function openNotifyModal(f: any) {
    setNotifyFlag(f);
    setNotifyDone('');
    setNotifyMsg(
      `AML Alert: A flag (${f.flagType?.replace(/_/g, ' ')}) has been raised for Client ${f.clientId}. ` +
      `Description: ${f.description}. Please review and take action.`
    );
    setStaffLoading(true);
    const staff = await loadStaff();
    setStaffList(staff);
    setNotifyUserId(staff.length > 0 ? String(staff[0].userId) : '');
    setStaffLoading(false);
  }

  async function sendNotification() {
    if (!notifyFlag || !notifyUserId || !notifyMsg.trim()) return;
    setNotifySending(true);
    try {
      await createNotification({
        userId:   Number(notifyUserId),
        message:  notifyMsg.trim(),
        category: 'Compliance',
      });
      setNotifyDone('Notification sent successfully.');
      setTimeout(() => setNotifyFlag(null), 1500);
    } catch {
      setNotifyDone('Failed to send notification. Please try again.');
    }
    setNotifySending(false);
  }

  // ── compliance: delete ────────────────────────────────────────────────────
  async function handleDelete(f: any) {
    try {
      await deleteAmlFlag(f.amlFlagId);
      loadFlags();
    } catch {
      setError('Failed to delete flag.');
      setTimeout(() => setError(''), 3000);
    }
  }

  // ── helpers ───────────────────────────────────────────────────────────────
  function statusPill(s: string) {
    if (s === 'OPEN')     return 'pill-danger';
    if (s === 'REVIEWED') return 'pill-info';
    if (s === 'CLOSED')   return 'pill-success';
    return 'pill-info';
  }

  function typePill(t: string) {
    if (t === 'WATCHLIST_MATCH')        return 'pill-danger';
    if (t === 'HIGH_VALUE_TRANSFER')    return 'pill-warn';
    if (t === 'SUSPICIOUS_TRANSACTION') return 'pill-warn';
    return 'pill-info';
  }

  // Valid next statuses compliance can move to
  function reviewOptions(currentStatus: string) {
    if (currentStatus === 'OPEN')     return ['REVIEWED', 'CLOSED'];
    if (currentStatus === 'REVIEWED') return ['CLOSED'];
    return ['CLOSED'];
  }

  const openCount     = flags.filter((f) => f.status === 'OPEN').length;
  const reviewedCount = flags.filter((f) => f.status === 'REVIEWED').length;
  const closedCount   = flags.filter((f) => f.status === 'CLOSED').length;

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">AML Flags</h1>
          <p className="text-sm text-text-2">Anti-money laundering flags raised against clients.</p>
        </div>
        {isCompliance && (
          <button onClick={() => setShowCreate(true)} className="btn btn-primary btn-sm">
            + Raise Flag
          </button>
        )}
      </div>

      {error && <div className="pill pill-danger block mb-4 text-center">{error}</div>}

      {/* Summary tiles */}
      <div className="grid grid-cols-4 gap-4 mb-5">
        <div className="panel">
          <div className="panel-b text-center">
            <p className="text-2xl font-bold text-danger">{openCount}</p>
            <p className="label mt-1">Open</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b text-center">
            <p className="text-2xl font-bold text-primary">{reviewedCount}</p>
            <p className="label mt-1">Reviewed</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b text-center">
            <p className="text-2xl font-bold text-success">{closedCount}</p>
            <p className="label mt-1">Closed</p>
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

      {/* Flags table */}
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
                  <td className="px-5 py-3 mono text-xs text-text-3">{f.amlFlagId}</td>
                  <td className="px-5 py-3 font-medium">Client {f.clientId}</td>
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
                    <div className="flex gap-2 justify-end flex-wrap">

                      {/* COMPLIANCE: Review/Close button — not shown for already CLOSED flags */}
                      {isCompliance && f.status !== 'CLOSED' && (
                        <button
                          onClick={() => {
                            const opts = reviewOptions(f.status);
                            setReviewFlag(f);
                            setReviewStatus(opts[0]);
                            setReviewNotes(f.notes || '');
                          }}
                          className="btn btn-ghost btn-sm"
                        >
                          {f.status === 'REVIEWED' ? 'Close' : 'Review'}
                        </button>
                      )}

                      {/* COMPLIANCE: Notify RM — only for OPEN and REVIEWED flags */}
                      {isCompliance && f.status !== 'CLOSED' && (
                        <button
                          onClick={() => openNotifyModal(f)}
                          className="btn btn-ghost btn-sm"
                        >
                          Notify RM
                        </button>
                      )}

                      {/* RM: Request Closure — notifies compliance, does not change status */}
                      {isRM && f.status !== 'CLOSED' && (
                        closureRequestedId === f.amlFlagId ? (
                          <span className="pill pill-success text-xs">Compliance notified ✓</span>
                        ) : (
                          <button
                            onClick={() => handleRequestClosure(f)}
                            className="btn btn-primary btn-sm"
                          >
                            Request Closure
                          </button>
                        )
                      )}

                      {/* COMPLIANCE / ADMIN: Delete */}
                      {isCompliance && (
                        <button onClick={() => handleDelete(f)} className="btn btn-danger btn-sm">
                          Delete
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* ── Create Flag Modal ──────────────────────────────────────────────── */}
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
                  min="1"
                  step="1"
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
              {formError && <div className="pill pill-danger block mb-3 text-center w-full">{formError}</div>}
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

      {/* ── Review / Close Modal (compliance) ────────────────────────────── */}
      {reviewFlag && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="px-6 py-4 border-b border-border-hairline flex items-center justify-between">
              <h3 className="font-semibold">
                {reviewFlag.status === 'REVIEWED'
                  ? `Close AML Flag ${reviewFlag.amlFlagId}`
                  : `Review AML Flag ${reviewFlag.amlFlagId}`}
              </h3>
              <button onClick={() => setReviewFlag(null)} className="text-text-3 text-xl">×</button>
            </div>
            <form onSubmit={handleReview} className="p-6">
              <div className="mb-3 bg-surface rounded-lg p-3 text-sm">
                <p className="label">Client {reviewFlag.clientId}</p>
                <p className="mt-1">{reviewFlag.description}</p>
              </div>
              <div className="mb-3">
                <label className="label block mb-1">Update Status</label>
                <select
                  className="input"
                  value={reviewStatus}
                  onChange={(e) => setReviewStatus(e.target.value)}
                >
                  {reviewOptions(reviewFlag.status).map((s) => (
                    <option key={s} value={s}>{s}</option>
                  ))}
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
                  {reviewSaving ? 'Saving...' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── Notify RM Modal (compliance) ──────────────────────────────────── */}
      {notifyFlag && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="px-6 py-4 border-b border-border-hairline flex items-center justify-between">
              <h3 className="font-semibold">Notify RM — Flag {notifyFlag.amlFlagId}</h3>
              <button onClick={() => setNotifyFlag(null)} className="text-text-3 text-xl">×</button>
            </div>
            <div className="p-6">
              <div className="mb-3">
                <label className="label block mb-1">Select RM</label>
                {staffLoading ? (
                  <p className="text-sm text-text-3">Loading RM list...</p>
                ) : staffList.length === 0 ? (
                  <p className="text-sm text-danger">No RM users found.</p>
                ) : (
                  <select
                    className="input"
                    value={notifyUserId}
                    onChange={(e) => setNotifyUserId(e.target.value)}
                  >
                    {staffList.map((s) => (
                      <option key={s.userId} value={s.userId}>
                        {s.username} (ID: {s.userId})
                      </option>
                    ))}
                  </select>
                )}
              </div>
              <div className="mb-4">
                <label className="label block mb-1">Message</label>
                <textarea
                  className="input resize-none"
                  rows={4}
                  value={notifyMsg}
                  onChange={(e) => setNotifyMsg(e.target.value)}
                />
              </div>
              {notifyDone && (
                <div className={`pill block mb-3 text-center w-full ${notifyDone.includes('success') ? 'pill-success' : 'pill-danger'}`}>
                  {notifyDone}
                </div>
              )}
              <div className="flex justify-end gap-2">
                <button onClick={() => setNotifyFlag(null)} className="btn btn-ghost">Cancel</button>
                <button
                  onClick={sendNotification}
                  className="btn btn-primary"
                  disabled={notifySending || staffList.length === 0 || !notifyUserId}
                >
                  {notifySending ? 'Sending...' : 'Send Notification'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
