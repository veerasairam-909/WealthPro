import { useState, useEffect, Fragment } from 'react';
import { getAllClients, getKycDocs, updateKycStatus } from '@/api/clients';
import { createNotification } from '@/api/notifications';
import { cachedFetch, parallelLimit } from '@/lib/fetchUtils';

// ─── Types ────────────────────────────────────────────────────────────────────

type KycStatus = 'Pending' | 'Verified' | 'Rejected';
type TabFilter = 'ALL' | 'PENDING' | 'VERIFIED' | 'REJECTED';

interface FlatDoc {
  kycId: number;
  clientId: number;
  clientName: string;
  documentType: string;
  documentRef: string;
  verifiedDate: string | null;
  status: KycStatus;
}

interface ActionMsg {
  id: number;
  msg: string;
  type: 'success' | 'error';
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

function statusPill(status: KycStatus): string {
  if (status === 'Verified') return 'pill-success';
  if (status === 'Pending') return 'pill-warn';
  return 'pill-danger';
}

const TAB_LABELS: { key: TabFilter; label: string }[] = [
  { key: 'PENDING', label: 'Pending' },
  { key: 'ALL', label: 'All' },
  { key: 'VERIFIED', label: 'Verified' },
  { key: 'REJECTED', label: 'Rejected' },
];

// ─── Component ────────────────────────────────────────────────────────────────

export default function KYCApproval() {
  const [docs, setDocs] = useState<FlatDoc[]>([]);
  const [loading, setLoading] = useState(true);
  const [tabFilter, setTabFilter] = useState<TabFilter>('PENDING');
  const [rejectingId, setRejectingId] = useState<number | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const [actionMsg, setActionMsg] = useState<ActionMsg | null>(null);

  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    setLoading(true);
    setDocs([]);
    try {
      const clients = await cachedFetch('clients', getAllClients);
      if (!Array.isArray(clients)) {
        setLoading(false);
        return;
      }

      const kycResults = await parallelLimit(
        clients.map((c: any) => () => getKycDocs(c.clientId)),
      );

      const flat: FlatDoc[] = [];
      for (let i = 0; i < clients.length; i++) {
        const c = clients[i];
        const result = kycResults[i];
        if (result.status === 'fulfilled' && Array.isArray(result.value)) {
          for (const d of result.value) {
            flat.push({
              kycId: d.kycId,
              clientId: c.clientId,
              clientName: c.name,
              documentType: d.documentType,
              documentRef: d.documentRefNumber ?? d.documentRef ?? '-',
              verifiedDate: d.verifiedDate ?? null,
              status: d.status as KycStatus,
            });
          }
        }
      }

      // Sort: Pending first, then Rejected, then Verified; within each group sort by client name
      const order: Record<KycStatus, number> = { Pending: 0, Rejected: 1, Verified: 2 };
      flat.sort((a, b) => {
        const diff = order[a.status] - order[b.status];
        if (diff !== 0) return diff;
        return a.clientName.localeCompare(b.clientName);
      });

      setDocs(flat);
    } catch (e) {
    }
    setLoading(false);
  }

  function showMsg(id: number, msg: string, type: 'success' | 'error') {
    setActionMsg({ id, msg, type });
    setTimeout(() => setActionMsg((prev) => (prev?.id === id ? null : prev)), 4000);
  }

  async function handleApprove(doc: FlatDoc) {
    try {
      await updateKycStatus(doc.kycId, 'Verified');
      await createNotification({
        userId: doc.clientId,
        message: `Your ${doc.documentType} document has been verified by compliance.`,
        category: 'Compliance',
      });
      setDocs((prev) =>
        prev.map((d) => (d.kycId === doc.kycId ? { ...d, status: 'Verified' } : d)),
      );
      showMsg(doc.kycId, 'Document approved and client notified.', 'success');
    } catch (e) {
      showMsg(doc.kycId, 'Failed to approve document. Please try again.', 'error');
    }
  }

  async function handleRejectSubmit(doc: FlatDoc) {
    const reason = rejectReason.trim();
    if (!reason) return;
    try {
      await updateKycStatus(doc.kycId, 'Rejected');
      await createNotification({
        userId: doc.clientId,
        message: `Your ${doc.documentType} document was rejected. Reason: ${reason}. Please re-upload.`,
        category: 'Compliance',
      });
      setDocs((prev) =>
        prev.map((d) => (d.kycId === doc.kycId ? { ...d, status: 'Rejected' } : d)),
      );
      setRejectingId(null);
      setRejectReason('');
      showMsg(doc.kycId, 'Document rejected and client notified.', 'success');
    } catch (e) {
      showMsg(doc.kycId, 'Failed to reject document. Please try again.', 'error');
    }
  }

  function openReject(kycId: number) {
    setRejectingId(kycId);
    setRejectReason('');
  }

  function cancelReject() {
    setRejectingId(null);
    setRejectReason('');
  }

  // ── Derived counts ──────────────────────────────────────────────────────────

  const totalCount = docs.length;
  const pendingCount = docs.filter((d) => d.status === 'Pending').length;
  const verifiedCount = docs.filter((d) => d.status === 'Verified').length;
  const rejectedCount = docs.filter((d) => d.status === 'Rejected').length;

  const displayed = docs.filter((d) => {
    if (tabFilter === 'ALL') return true;
    if (tabFilter === 'PENDING') return d.status === 'Pending';
    if (tabFilter === 'VERIFIED') return d.status === 'Verified';
    if (tabFilter === 'REJECTED') return d.status === 'Rejected';
    return true;
  });

  function tabCount(key: TabFilter): number {
    if (key === 'ALL') return totalCount;
    if (key === 'PENDING') return pendingCount;
    if (key === 'VERIFIED') return verifiedCount;
    return rejectedCount;
  }

  // ── Render ──────────────────────────────────────────────────────────────────

  return (
    <div>
      {/* Header */}
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">KYC Approval Queue</h1>
          <p className="text-sm text-text-2">
            Review and approve or reject pending KYC documents across all clients.
          </p>
        </div>
        <button onClick={loadAll} disabled={loading} className="btn btn-ghost btn-sm">
          {loading ? 'Loading...' : '↻ Refresh'}
        </button>
      </div>

      {/* KPI cards */}
      <div className="grid grid-cols-4 gap-4 mb-5">
        <div className="panel">
          <div className="panel-b">
            <p className="label">Total Documents</p>
            <p className="text-3xl font-bold mt-1">{loading ? '—' : totalCount}</p>
            <p className="text-xs text-text-3 mt-1">across all clients</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Pending Review</p>
            <p className={'text-3xl font-bold mt-1 ' + (pendingCount > 0 ? 'text-warn' : 'text-success')}>
              {loading ? '—' : pendingCount}
            </p>
            <p className="text-xs text-text-3 mt-1">awaiting decision</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Verified</p>
            <p className="text-3xl font-bold mt-1 text-success">{loading ? '—' : verifiedCount}</p>
            <p className="text-xs text-text-3 mt-1">approved documents</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Rejected</p>
            <p className={'text-3xl font-bold mt-1 ' + (rejectedCount > 0 ? 'text-danger' : 'text-text-2')}>
              {loading ? '—' : rejectedCount}
            </p>
            <p className="text-xs text-text-3 mt-1">require re-upload</p>
          </div>
        </div>
      </div>

      {/* Tab filter */}
      <div className="panel mb-4">
        <div className="panel-b py-3 flex gap-2 items-center flex-wrap">
          {TAB_LABELS.map(({ key, label }) => (
            <button
              key={key}
              onClick={() => setTabFilter(key)}
              className={
                'px-3 py-1.5 text-xs font-medium rounded border ' +
                (tabFilter === key
                  ? 'bg-primary text-white border-primary'
                  : 'bg-white text-text-2 border-border')
              }
            >
              {label}
              <span className="ml-1 opacity-70">({tabCount(key)})</span>
            </button>
          ))}
          <span className="ml-auto text-xs text-text-2">
            {displayed.length} document{displayed.length !== 1 ? 's' : ''} shown
          </span>
        </div>
      </div>

      {/* Table */}
      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">
            <p className="text-3xl mb-2">🔍</p>
            <p>Loading KYC documents...</p>
          </div>
        ) : tabFilter === 'PENDING' && pendingCount === 0 ? (
          <div className="panel-b text-center py-12">
            <p className="text-4xl mb-3">✅</p>
            <p className="font-semibold text-success">All KYC documents are up to date</p>
            <p className="text-sm text-text-2 mt-1">No documents are awaiting review.</p>
          </div>
        ) : displayed.length === 0 ? (
          <div className="panel-b text-center text-text-2 py-10">
            No documents match the selected filter.
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Client</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Doc Type</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Reference</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">
                  Uploaded / Verified Date
                </th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {displayed.map((doc) => (
                <Fragment key={doc.kycId}>
                  {/* Main row */}
                  <tr
                    className={
                      'border-t border-border-hairline' +
                      (rejectingId === doc.kycId ? ' bg-surface' : '')
                    }
                  >
                    <td className="px-5 py-3">
                      <p className="font-medium">{doc.clientName}</p>
                      <p className="text-xs text-text-2 mono">{doc.clientId}</p>
                    </td>
                    <td className="px-5 py-3 font-medium">{doc.documentType}</td>
                    <td
                      className="px-5 py-3 mono text-xs text-text-2 max-w-[180px] truncate"
                      title={doc.documentRef}
                    >
                      {doc.documentRef}
                    </td>
                    <td className="px-5 py-3 mono text-xs text-text-2">
                      {doc.verifiedDate ?? <span className="text-text-3 italic">—</span>}
                    </td>
                    <td className="px-5 py-3">
                      <span className={'pill ' + statusPill(doc.status)}>{doc.status}</span>
                    </td>
                    <td className="px-5 py-3 text-right">
                      {/* Inline toast */}
                      {actionMsg && actionMsg.id === doc.kycId ? (
                        <span
                          className={
                            'text-xs font-medium ' +
                            (actionMsg.type === 'success' ? 'text-success' : 'text-danger')
                          }
                        >
                          {actionMsg.msg}
                        </span>
                      ) : doc.status === 'Pending' ? (
                        <div className="flex gap-2 justify-end">
                          <button
                            onClick={() => handleApprove(doc)}
                            className="btn btn-success btn-sm"
                          >
                            Approve
                          </button>
                          <button
                            onClick={() =>
                              rejectingId === doc.kycId
                                ? cancelReject()
                                : openReject(doc.kycId)
                            }
                            className="btn btn-danger btn-sm"
                          >
                            {rejectingId === doc.kycId ? 'Cancel' : 'Reject'}
                          </button>
                        </div>
                      ) : (
                        <span className="text-xs text-text-3 italic">No actions</span>
                      )}
                    </td>
                  </tr>

                  {/* Inline rejection form — expands below the row */}
                  {rejectingId === doc.kycId && (
                    <tr className="border-t border-border-hairline bg-surface">
                      <td colSpan={6} className="px-5 py-4">
                        <div className="flex flex-col gap-2 max-w-xl">
                          <p className="text-xs font-medium text-text-2">
                            Rejection reason for{' '}
                            <span className="font-semibold">{doc.documentType}</span>{' '}
                            ({doc.clientName})
                          </p>
                          <textarea
                            className="input resize-none text-sm"
                            rows={2}
                            placeholder="Enter reason for rejection (required)..."
                            value={rejectReason}
                            onChange={(e) => setRejectReason(e.target.value)}
                            autoFocus
                          />
                          <div className="flex gap-2 mt-1">
                            <button
                              onClick={() => handleRejectSubmit(doc)}
                              disabled={!rejectReason.trim()}
                              className="btn btn-danger btn-sm"
                            >
                              Confirm Rejection
                            </button>
                            <button onClick={cancelReject} className="btn btn-ghost btn-sm">
                              Cancel
                            </button>
                          </div>
                        </div>
                      </td>
                    </tr>
                  )}
                </Fragment>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
