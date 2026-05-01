import { useState, useEffect } from 'react';
import { getAllClients, getKycDocs, getRiskProfile } from '@/api/clients';
import { createNotification } from '@/api/notifications';
import { getAllUsers } from '@/api/admin';
import { getAccountsByClientId } from '@/api/accounts';
import { getBreachesByAccount, runComplianceScan, acknowledgeBreach, closeBreach } from '@/api/analytics';
import { cachedFetch, parallelLimit } from '@/lib/fetchUtils';

interface StaffEntry { username: string; userId: number; role: string; }

/**
 * Fetch all RM users for the notification dropdown.
 *
 * Primary:  GET /auth/users  (COMPLIANCE now has read access — SecurityConfig updated)
 *           Returns fresh list of every registered RM with their userId.
 *
 * Fallback: localStorage wp_user_id_map
 *           Written at every login and when admin visits Users page.
 *           Used if the API call fails (network error, etc.).
 */
async function loadStaff(): Promise<StaffEntry[]> {
  // ── 1. Try the API (COMPLIANCE can now GET /auth/users) ──────────────────
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

      // Also refresh the local map so other pages benefit
      try {
        const map = JSON.parse(localStorage.getItem('wp_user_id_map') || '{}');
        for (const s of staff) {
          map[s.username] = { userId: s.userId, role: s.role };
        }
        localStorage.setItem('wp_user_id_map', JSON.stringify(map));
      } catch { /* storage full */ }

      if (staff.length > 0) return staff;
    }
  } catch { /* fall through to localStorage */ }

  // ── 2. Fallback: localStorage map ────────────────────────────────────────
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

interface Breach {
  id: string; // clientId + type composite key
  clientId: number;
  name: string;
  segment: string;
  type: string;
  detail: string;
  severity: 'HIGH' | 'MEDIUM' | 'LOW';
}

type BreachStatus = 'OPEN' | 'ACKNOWLEDGED' | 'CLOSED';

const STORAGE_KEY = 'wp_breach_statuses';

function loadStatuses(): Record<string, BreachStatus> {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}');
  } catch { return {}; }
}

function saveStatuses(s: Record<string, BreachStatus>) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(s));
}

export default function Breaches() {
  const [breaches, setBreaches]           = useState<Breach[]>([]);
  const [statuses, setStatuses]           = useState<Record<string, BreachStatus>>(loadStatuses);
  const [loading, setLoading]             = useState(true);
  const [lastRefresh, setLastRefresh]     = useState('');
  const [severityFilter, setSeverityFilter] = useState('ALL');
  const [statusFilter, setStatusFilter]   = useState<'OPEN' | 'ALL' | 'CLOSED'>('OPEN');

  // backend analytics breaches
  const [backendBreaches, setBackendBreaches] = useState<any[]>([]);
  const [backendLoading, setBackendLoading]   = useState(false);
  const [scanningAccountId, setScanningAccountId] = useState<number | null>(null);

  // notify modal state
  const [notifyBreach, setNotifyBreach]   = useState<Breach | null>(null);
  const [notifyUserId, setNotifyUserId]   = useState('');
  const [notifyMsg, setNotifyMsg]         = useState('');
  const [notifySending, setNotifySending] = useState(false);
  const [notifyDone, setNotifyDone]       = useState('');
  const [staffList, setStaffList]         = useState<StaffEntry[]>([]);
  const [staffLoading, setStaffLoading]   = useState(false);

  useEffect(() => { loadAll(); }, []);

  async function loadAll() {
    setLoading(true);
    setBreaches([]);
    try {
      // Cache clients for 60 s — reused by KYCApproval, RiskMonitor etc.
      const clients = await cachedFetch('clients', getAllClients);
      if (!Array.isArray(clients)) { setLoading(false); return; }

      // Cap concurrency at 6 to avoid overwhelming the gateway
      const [kycResults, riskResults] = await Promise.all([
        parallelLimit(clients.map((c: any) => () => getKycDocs(c.clientId))),
        parallelLimit(clients.map((c: any) => () => getRiskProfile(c.clientId))),
      ]);

      const found: Breach[] = [];

      for (let i = 0; i < clients.length; i++) {
        const c = clients[i];
        const kycRes = kycResults[i];
        const riskRes = riskResults[i];

        if (c.status === 'PENDING_KYC') {
          found.push({ id: `${c.clientId}_kyc_pending`, clientId: c.clientId, name: c.name, segment: c.segment, type: 'KYC Pending', detail: 'Client account created but KYC verification is incomplete.', severity: 'HIGH' });
        } else if (kycRes.status === 'fulfilled' && Array.isArray(kycRes.value)) {
          const docs = kycRes.value as any[];
          const hasVerified = docs.some((d) => d.status === 'Verified');
          const hasPending  = docs.some((d) => d.status === 'Pending');
          if (!hasVerified && docs.length > 0) {
            found.push({ id: `${c.clientId}_kyc_unverified`, clientId: c.clientId, name: c.name, segment: c.segment, type: 'KYC Unverified', detail: hasPending ? 'Documents uploaded but none have been verified yet.' : 'KYC documents exist but verification status is unknown.', severity: 'HIGH' });
          } else if (docs.length === 0 && c.status === 'Active') {
            found.push({ id: `${c.clientId}_kyc_missing`, clientId: c.clientId, name: c.name, segment: c.segment, type: 'No KYC Documents', detail: 'Active client has no KYC documents on record.', severity: 'HIGH' });
          }
        }

        const hasRisk = riskRes.status === 'fulfilled' && riskRes.value !== null && riskRes.value !== undefined;
        if (!hasRisk && c.status === 'Active') {
          found.push({ id: `${c.clientId}_risk_missing`, clientId: c.clientId, name: c.name, segment: c.segment, type: 'Missing Risk Profile', detail: 'Client is Active but has no risk assessment on record.', severity: 'MEDIUM' });
        }

        if (c.status === 'Inactive') {
          found.push({ id: `${c.clientId}_inactive`, clientId: c.clientId, name: c.name, segment: c.segment, type: 'Inactive Account', detail: 'Account is inactive — verify reason and ensure data retention compliance.', severity: 'LOW' });
        }
      }

      const order = { HIGH: 0, MEDIUM: 1, LOW: 2 };
      found.sort((a, b) => order[a.severity] - order[b.severity]);
      setBreaches(found);
      setLastRefresh(new Date().toLocaleTimeString());
    } catch (e) {
    }
    setLoading(false);
  }

  async function loadBackendBreaches() {
    setBackendLoading(true);
    try {
      const clients = await cachedFetch('clients', getAllClients);
      if (!Array.isArray(clients)) { setBackendLoading(false); return; }

      // get accounts for each client, then fetch breaches per account
      const accountResults = await parallelLimit(
        clients.map((c: any) => () => getAccountsByClientId(c.clientId).catch(() => []))
      );

      const allBreaches: any[] = [];
      for (let i = 0; i < clients.length; i++) {
        const result = accountResults[i];
        if (result.status === 'fulfilled' && Array.isArray(result.value)) {
          for (const acc of result.value) {
            try {
              const breaches = await getBreachesByAccount(acc.accountId);
              if (Array.isArray(breaches)) {
                for (const b of breaches) {
                  allBreaches.push({ ...b, clientName: clients[i].name });
                }
              }
            } catch { /* account might have no breaches */ }
          }
        }
      }

      allBreaches.sort((a, b) => {
        const order: Record<string, number> = { HIGH: 0, MEDIUM: 1, LOW: 2 };
        return (order[a.severity] ?? 9) - (order[b.severity] ?? 9);
      });
      setBackendBreaches(allBreaches);
    } catch (e) {
    }
    setBackendLoading(false);
  }

  async function handleRunScan(accountId: number) {
    setScanningAccountId(accountId);
    try {
      await runComplianceScan(accountId);
      // reload backend breaches after scan
      await loadBackendBreaches();
    } catch (e) {
    }
    setScanningAccountId(null);
  }

  async function handleAcknowledge(breachId: number) {
    try {
      await acknowledgeBreach(breachId);
      setBackendBreaches((prev) =>
        prev.map((b) => b.breachId === breachId ? { ...b, status: 'ACKNOWLEDGED' } : b)
      );
    } catch (e) {
    }
  }

  async function handleClose(breachId: number) {
    try {
      await closeBreach(breachId);
      setBackendBreaches((prev) =>
        prev.map((b) => b.breachId === breachId ? { ...b, status: 'CLOSED' } : b)
      );
    } catch (e) {
    }
  }

  function getStatus(b: Breach): BreachStatus {
    return statuses[b.id] || 'OPEN';
  }

  function setBreachStatus(b: Breach, status: BreachStatus) {
    const updated = { ...statuses, [b.id]: status };
    setStatuses(updated);
    saveStatuses(updated);
  }

  async function openNotifyModal(b: Breach) {
    setNotifyBreach(b);
    setNotifyDone('');
    setNotifyMsg(`Compliance alert: ${b.type} breach detected for client ${b.name} (ID: ${b.clientId}). Breach detail: ${b.detail} Please take immediate action.`);
    // Load RM list from API (COMPLIANCE now has read access), fall back to localStorage
    setStaffLoading(true);
    const staff = await loadStaff();
    setStaffList(staff);
    setNotifyUserId(staff.length > 0 ? String(staff[0].userId) : '');
    setStaffLoading(false);
  }

  async function sendNotification() {
    if (!notifyBreach || !notifyUserId || !notifyMsg.trim()) return;
    setNotifySending(true);
    try {
      await createNotification({ userId: Number(notifyUserId), message: notifyMsg.trim(), category: 'Compliance' });
      setNotifyDone('Notification sent successfully.');
      setTimeout(() => setNotifyBreach(null), 1500);
    } catch {
      setNotifyDone('Failed to send notification. Please try again.');
    }
    setNotifySending(false);
  }

  const severities = ['ALL', 'HIGH', 'MEDIUM', 'LOW'];

  // Apply filters
  const filtered = breaches.filter((b) => {
    const s = getStatus(b);
    if (statusFilter === 'OPEN'   && s === 'CLOSED') return false;
    if (statusFilter === 'CLOSED' && s !== 'CLOSED') return false;
    if (severityFilter !== 'ALL'  && b.severity !== severityFilter) return false;
    return true;
  });

  const highCount   = breaches.filter((b) => b.severity === 'HIGH').length;
  const medCount    = breaches.filter((b) => b.severity === 'MEDIUM').length;
  const lowCount    = breaches.filter((b) => b.severity === 'LOW').length;
  const openCount   = breaches.filter((b) => getStatus(b) === 'OPEN').length;
  const ackCount    = breaches.filter((b) => getStatus(b) === 'ACKNOWLEDGED').length;
  const closedCount = breaches.filter((b) => getStatus(b) === 'CLOSED').length;

  function getSeverityPill(s: string) {
    if (s === 'HIGH')   return 'pill-danger';
    if (s === 'MEDIUM') return 'pill-warn';
    return 'pill-info';
  }

  function getStatusPill(s: BreachStatus) {
    if (s === 'OPEN')         return 'pill-danger';
    if (s === 'ACKNOWLEDGED') return 'pill-warn';
    return 'pill-success';
  }

  function getTypeIcon(type: string) {
    if (type.includes('KYC'))      return '🪪';
    if (type.includes('Risk'))     return '📋';
    if (type.includes('Inactive')) return '⏸';
    return '⚠';
  }

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Compliance Breaches</h1>
          <p className="text-sm text-text-2">
            Active compliance issues requiring review and resolution.
            {lastRefresh && <span className="text-text-3 ml-2">Last refreshed: {lastRefresh}</span>}
          </p>
        </div>
        <button onClick={loadAll} disabled={loading} className="btn btn-ghost btn-sm">
          {loading ? 'Scanning...' : '↻ Refresh'}
        </button>
      </div>

      {/* KPI cards */}
      <div className="grid grid-cols-3 md:grid-cols-6 gap-3 mb-5">
        <div className="panel"><div className="panel-b text-center">
          <p className="label">Total</p>
          <p className={'text-2xl font-bold mt-1 ' + (breaches.length > 0 ? 'text-danger' : 'text-success')}>{loading ? '—' : breaches.length}</p>
        </div></div>
        <div className="panel"><div className="panel-b text-center">
          <p className="label">High</p>
          <p className={'text-2xl font-bold mt-1 ' + (highCount > 0 ? 'text-danger' : 'text-success')}>{loading ? '—' : highCount}</p>
        </div></div>
        <div className="panel"><div className="panel-b text-center">
          <p className="label">Medium</p>
          <p className={'text-2xl font-bold mt-1 ' + (medCount > 0 ? 'text-warn' : 'text-success')}>{loading ? '—' : medCount}</p>
        </div></div>
        <div className="panel"><div className="panel-b text-center">
          <p className="label">Open</p>
          <p className={'text-2xl font-bold mt-1 ' + (openCount > 0 ? 'text-danger' : 'text-success')}>{loading ? '—' : openCount}</p>
        </div></div>
        <div className="panel"><div className="panel-b text-center">
          <p className="label">Acknowledged</p>
          <p className="text-2xl font-bold mt-1 text-warn">{loading ? '—' : ackCount}</p>
        </div></div>
        <div className="panel"><div className="panel-b text-center">
          <p className="label">Closed</p>
          <p className="text-2xl font-bold mt-1 text-success">{loading ? '—' : closedCount}</p>
        </div></div>
      </div>

      {/* Filters */}
      <div className="panel mb-4">
        <div className="panel-b py-3 flex gap-3 items-center flex-wrap">
          <div className="flex gap-2 items-center">
            <span className="text-xs text-text-2 font-medium">Status:</span>
            {(['OPEN', 'ALL', 'CLOSED'] as const).map((s) => (
              <button key={s} onClick={() => setStatusFilter(s)}
                className={'px-3 py-1.5 text-xs font-medium rounded border ' + (statusFilter === s ? 'bg-primary text-white border-primary' : 'bg-white text-text-2 border-border')}>
                {s}
              </button>
            ))}
          </div>
          <div className="flex gap-2 items-center">
            <span className="text-xs text-text-2 font-medium">Severity:</span>
            {severities.map((s) => (
              <button key={s} onClick={() => setSeverityFilter(s)}
                className={'px-3 py-1.5 text-xs font-medium rounded border ' + (severityFilter === s ? 'bg-primary text-white border-primary' : 'bg-white text-text-2 border-border')}>
                {s}{s !== 'ALL' && ` (${breaches.filter((b) => b.severity === s).length})`}
              </button>
            ))}
          </div>
          <span className="ml-auto text-xs text-text-2">{filtered.length} issues shown</span>
        </div>
      </div>

      {/* Breach table */}
      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">
            <p className="text-3xl mb-2">🔍</p>
            <p>Scanning clients for compliance issues...</p>
          </div>
        ) : filtered.length === 0 ? (
          <div className="panel-b text-center py-10">
            <p className="text-4xl mb-2">✅</p>
            <p className="font-semibold">No breaches match current filters</p>
            <p className="text-sm text-text-2 mt-1">Adjust filters or refresh to scan again.</p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Client</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Segment</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Breach Type</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Details</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Severity</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((b) => {
                const bStatus = getStatus(b);
                return (
                  <tr key={b.id} className="border-t border-border-hairline">
                    <td className="px-5 py-3">
                      <p className="font-medium">{b.name}</p>
                      <p className="text-xs text-text-3 mono">{b.clientId}</p>
                    </td>
                    <td className="px-5 py-3 text-text-2">{b.segment}</td>
                    <td className="px-5 py-3 font-medium">{getTypeIcon(b.type)} {b.type}</td>
                    <td className="px-5 py-3 text-text-2 max-w-xs text-xs">{b.detail}</td>
                    <td className="px-5 py-3"><span className={'pill ' + getSeverityPill(b.severity)}>{b.severity}</span></td>
                    <td className="px-5 py-3"><span className={'pill ' + getStatusPill(bStatus)}>{bStatus}</span></td>
                    <td className="px-5 py-3">
                      <div className="flex gap-1.5 justify-end flex-wrap">
                        {bStatus === 'OPEN' && (
                          <button onClick={() => setBreachStatus(b, 'ACKNOWLEDGED')} className="btn btn-ghost btn-sm">
                            Acknowledge
                          </button>
                        )}
                        {bStatus === 'ACKNOWLEDGED' && (
                          <button onClick={() => setBreachStatus(b, 'CLOSED')} className="btn btn-success btn-sm">
                            Close
                          </button>
                        )}
                        {bStatus === 'CLOSED' && (
                          <button onClick={() => setBreachStatus(b, 'OPEN')} className="btn btn-ghost btn-sm text-xs">
                            Reopen
                          </button>
                        )}
                        <button onClick={() => openNotifyModal(b)} className="btn btn-primary btn-sm">
                          Notify RM
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* ── Analytics Backend Breaches ── */}
      <div className="panel mt-5">
        <div className="panel-h">
          <div>
            <h3>Analytics Compliance Breaches</h3>
            <p className="text-xs text-text-2 mt-0.5">
              Portfolio concentration & exposure violations detected by the analytics engine.
            </p>
          </div>
          <div className="flex gap-2">
            <button
              onClick={loadBackendBreaches}
              disabled={backendLoading}
              className="btn btn-ghost btn-sm"
            >
              {backendLoading ? 'Loading...' : '↻ Load'}
            </button>
          </div>
        </div>

        {!backendLoading && backendBreaches.length === 0 ? (
          <div className="panel-b text-center text-text-2 py-8 text-sm">
            <p>Click "Load" to fetch portfolio-level compliance breaches from the analytics service.</p>
            <p className="text-xs text-text-3 mt-1">
              These check portfolio concentration, exposure limits, and restricted security violations.
            </p>
          </div>
        ) : backendLoading ? (
          <div className="panel-b text-center text-text-2 py-8">Loading analytics breaches...</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Client</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Account</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Description</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Severity</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Detected</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {backendBreaches.map((b: any) => (
                <tr key={b.breachId} className="border-t border-border-hairline">
                  <td className="px-5 py-3 font-medium">{b.clientName || '—'}</td>
                  <td className="px-5 py-3 mono text-xs text-text-2">{b.accountId}</td>
                  <td className="px-5 py-3 text-text-2 text-xs max-w-xs">{b.description}</td>
                  <td className="px-5 py-3">
                    <span className={'pill ' + getSeverityPill(b.severity)}>{b.severity}</span>
                  </td>
                  <td className="px-5 py-3">
                    <span className={'pill ' + (b.status === 'OPEN' ? 'pill-danger' : b.status === 'ACKNOWLEDGED' ? 'pill-warn' : 'pill-success')}>
                      {b.status}
                    </span>
                  </td>
                  <td className="px-5 py-3 mono text-xs text-text-2">
                    {b.detectedAt ? b.detectedAt.slice(0, 10) : '—'}
                  </td>
                  <td className="px-5 py-3 text-right">
                    <div className="flex gap-1.5 justify-end flex-wrap">
                      {b.status === 'OPEN' && (
                        <button onClick={() => handleAcknowledge(b.breachId)} className="btn btn-ghost btn-sm">
                          Acknowledge
                        </button>
                      )}
                      {b.status === 'ACKNOWLEDGED' && (
                        <button onClick={() => handleClose(b.breachId)} className="btn btn-success btn-sm">
                          Close
                        </button>
                      )}
                      <button
                        onClick={() => handleRunScan(b.accountId)}
                        disabled={scanningAccountId === b.accountId}
                        className="btn btn-primary btn-sm"
                      >
                        {scanningAccountId === b.accountId ? 'Scanning...' : 'Re-scan'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Notify RM modal */}
      {notifyBreach && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-lg w-full">
            <div className="px-6 py-4 border-b border-border-hairline flex items-center justify-between">
              <div>
                <h3 className="font-semibold">Send Compliance Alert</h3>
                <p className="text-xs text-text-2 mt-0.5">
                  Notify RM about <span className="font-medium">{notifyBreach.name}</span>
                </p>
              </div>
              <button onClick={() => setNotifyBreach(null)} className="text-text-3 text-xl">×</button>
            </div>
            <div className="p-6">
              {/* Recipient — loaded from API (COMPLIANCE can now GET /auth/users) */}
              <div className="mb-4">
                <label className="label block mb-1">Notify</label>
                {staffLoading ? (
                  <p className="text-sm text-text-2 py-2">Loading staff list...</p>
                ) : staffList.length > 0 ? (
                  <select
                    className="input"
                    value={notifyUserId}
                    onChange={(e) => setNotifyUserId(e.target.value)}
                  >
                    {staffList.map((s) => (
                      <option key={s.userId} value={s.userId}>
                        {s.username} — {s.role}
                      </option>
                    ))}
                  </select>
                ) : (
                  <>
                    <input
                      className="input mono"
                      type="number"
                      min="1"
                      placeholder="Enter recipient user ID"
                      value={notifyUserId}
                      onChange={(e) => setNotifyUserId(e.target.value)}
                    />
                    <p className="text-xs text-text-3 mt-1">
                      ⓘ No RM / Advisor accounts found. Ask Admin to register staff users.
                    </p>
                  </>
                )}
              </div>

              {/* Message */}
              <div className="mb-4">
                <label className="label block mb-1">Message</label>
                <textarea
                  className="input"
                  rows={4}
                  value={notifyMsg}
                  onChange={(e) => setNotifyMsg(e.target.value)}
                  style={{ resize: 'vertical' }}
                />
              </div>

              {notifyDone && (
                <div className={'pill block mb-3 text-center w-full ' + (notifyDone.includes('success') ? 'pill-success' : 'pill-danger')}>
                  {notifyDone}
                </div>
              )}

              <div className="flex justify-end gap-2">
                <button onClick={() => setNotifyBreach(null)} className="btn btn-ghost">Cancel</button>
                <button onClick={sendNotification} disabled={notifySending || !notifyUserId} className="btn btn-primary">
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
