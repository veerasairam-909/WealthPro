import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  getClientById, updateClient,
  getKycDocs, uploadKyc, updateKycStatus,
  getRiskProfile, createRiskProfile,
} from '@/api/clients';
import { getGoalsByClientId, updateGoalStatus, deleteGoal } from '@/api/goals';
import {
  getRecommendationsByClientId,
  createRecommendation,
  deleteRecommendation,
  getAllModelPortfolios,
} from '@/api/recommendations';
import { getAccountsByClientId, createAccount } from '@/api/accounts';
import {
  getBalanceByAccountId,
  getCashLedgerByAccountId,
  createCashLedgerEntry,
} from '@/api/cashLedger';
import { useAuth } from '@/auth/store';

const TABS = ['Profile', 'KYC', 'Risk Profile', 'Goals', 'Recommendations', 'Account & Funds'];

export default function ClientDetail() {
  const { id } = useParams();
  const clientId = Number(id);

  const user = useAuth((s) => s.user);
  const isRM = user?.role === 'RM';

  const [activeTab, setActiveTab] = useState('Profile');

  const [client, setClient] = useState<any>(null);
  const [kycDocs, setKycDocs] = useState<any[]>([]);
  const [risk, setRisk] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  // goals tab
  const [goals, setGoals] = useState<any[]>([]);

  // recommendations tab
  const [recos, setRecos] = useState<any[]>([]);
  const [modelPortfolios, setModelPortfolios] = useState<any[]>([]);

  // account & funds tab
  const [account, setAccount] = useState<any>(null);
  const [cashBalance, setCashBalance] = useState<number>(0);
  const [ledgerEntries, setLedgerEntries] = useState<any[]>([]);

  useEffect(() => {
    loadAll();
  }, [clientId]);

  async function loadAll() {
    setLoading(true);
    try {
      // Fetch all data sources in parallel
      const [clientRes, kycRes, riskRes, goalsRes, recosRes, accountsRes, modelsRes] = await Promise.allSettled([
        getClientById(clientId),
        getKycDocs(clientId),
        getRiskProfile(clientId),
        getGoalsByClientId(clientId),
        getRecommendationsByClientId(clientId),
        getAccountsByClientId(clientId),
        getAllModelPortfolios(),
      ]);

      if (clientRes.status === 'fulfilled') setClient(clientRes.value);
      setKycDocs(kycRes.status === 'fulfilled' && Array.isArray(kycRes.value) ? kycRes.value : []);
      setRisk(riskRes.status === 'fulfilled' ? riskRes.value : null);
      setGoals(goalsRes.status === 'fulfilled' && Array.isArray(goalsRes.value) ? goalsRes.value : []);
      setRecos(recosRes.status === 'fulfilled' && Array.isArray(recosRes.value) ? recosRes.value : []);
      setModelPortfolios(modelsRes.status === 'fulfilled' && Array.isArray(modelsRes.value) ? modelsRes.value : []);

      // account & funds
      if (accountsRes.status === 'fulfilled' && Array.isArray(accountsRes.value) && accountsRes.value.length > 0) {
        const acc = accountsRes.value[0];
        setAccount(acc);
        // fetch balance and ledger in parallel
        const [balRes, ledgerRes] = await Promise.allSettled([
          getBalanceByAccountId(acc.accountId),
          getCashLedgerByAccountId(acc.accountId),
        ]);
        if (balRes.status === 'fulfilled') setCashBalance(Number(balRes.value) || 0);
        if (ledgerRes.status === 'fulfilled' && Array.isArray(ledgerRes.value)) {
          setLedgerEntries(ledgerRes.value);
        }
      } else {
        setAccount(null);
        setCashBalance(0);
        setLedgerEntries([]);
      }

    } catch (e) {
    }
    setLoading(false);
  }

  async function handleDeleteReco(recoId: number) {
    if (!confirm('Delete this recommendation?')) return;
    try {
      await deleteRecommendation(recoId);
      setRecos((prev) => prev.filter((r) => r.recoId !== recoId));
    } catch (e) {
    }
  }

  async function handleDeleteGoal(goalId: number) {
    if (!confirm('Delete this goal?')) return;
    try {
      await deleteGoal(goalId);
      setGoals((prev) => prev.filter((g) => g.goalId !== goalId));
    } catch (e) {
    }
  }

  if (loading) {
    return <div className="p-10 text-center text-text-2">Loading client...</div>;
  }

  if (!client) {
    return (
      <div>
        <p className="mb-3">Client not found</p>
        <Link to="/rm/clients" className="btn btn-ghost btn-sm">← Back to clients</Link>
      </div>
    );
  }

  // parse contactInfo if it's a JSON string
  let contact: any = {};
  try {
    if (typeof client.contactInfo === 'string') {
      contact = JSON.parse(client.contactInfo);
    } else if (client.contactInfo) {
      contact = client.contactInfo;
    }
  } catch (e) {
    contact = { raw: client.contactInfo };
  }

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <Link to="/rm/clients" className="text-sm text-text-2 mb-1 inline-block">
            ← All clients
          </Link>
          <h1 className="text-2xl font-semibold">{client.name}</h1>
          <p className="text-sm text-text-2">Client ID {client.clientId} · {client.segment} · {client.status}</p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-border mb-4">
        {TABS.map((t) => (
          <button
            key={t}
            onClick={() => setActiveTab(t)}
            className={
              'px-4 py-2 text-sm font-medium border-b-2 ' +
              (activeTab === t
                ? 'border-primary text-primary'
                : 'border-transparent text-text-2')
            }
          >
            {t}
          </button>
        ))}
      </div>

      {/* Profile tab */}
      {activeTab === 'Profile' && (
        <ProfileTab
          client={client}
          contact={contact}
          canEdit={isRM}
          clientId={clientId}
          kycDocs={kycDocs}
          risk={risk}
          onUpdated={loadAll}
        />
      )}

      {/* KYC tab */}
      {activeTab === 'KYC' && (
        <KycTab clientId={clientId} kycDocs={kycDocs} canEdit={isRM} onUpdated={loadAll} />
      )}

      {/* Risk tab */}
      {activeTab === 'Risk Profile' && (
        <RiskTab clientId={clientId} risk={risk} canEdit={isRM} onUpdated={loadAll} />
      )}

      {/* Goals tab */}
      {activeTab === 'Goals' && (
        <GoalsTab goals={goals} onDeleteGoal={handleDeleteGoal} />
      )}

      {/* Recommendations tab */}
      {activeTab === 'Recommendations' && (
        <RecommendationsTab
          recos={recos}
          modelPortfolios={modelPortfolios}
          clientRiskClass={risk?.riskClass || null}
          clientSegment={client?.segment || null}
          clientId={clientId}
          canEdit={isRM}
          onDelete={handleDeleteReco}
          onRecosUpdated={(updated) => setRecos(updated)}
        />
      )}

      {/* Account & Funds tab */}
      {activeTab === 'Account & Funds' && (
        <AccountFundsTab
          clientId={clientId}
          account={account}
          cashBalance={cashBalance}
          ledgerEntries={ledgerEntries}
          canEdit={isRM}
          onUpdated={loadAll}
        />
      )}
    </div>
  );
}

// ─── PROFILE TAB ─────────────────────────────
function ProfileTab(props: {
  client: any;
  contact: any;
  canEdit: boolean;
  clientId: number;
  kycDocs: any[];
  risk: any;
  onUpdated: () => void;
}) {
  const { client, contact, canEdit, clientId, kycDocs, risk, onUpdated } = props;
  const [editing, setEditing] = useState(false);

  // editable fields - prefilled with current values
  const [name, setName] = useState(client.name || '');
  const [dob, setDob] = useState(client.dob || '');
  const [email, setEmail] = useState(contact.email || '');
  const [phone, setPhone] = useState(contact.phone || '');
  const [segment, setSegment] = useState(client.segment);
  const [status, setStatus] = useState(client.status);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  // activation prerequisites
  const hasVerifiedKyc = kycDocs.some((d) => d.status === 'Verified');
  const hasRiskProfile = !!risk;
  const canActivate    = hasVerifiedKyc && hasRiskProfile;

  function startEdit() {
    // reset all fields to current values when entering edit mode
    setName(client.name || '');
    setDob(client.dob || '');
    setEmail(contact.email || '');
    setPhone(contact.phone || '');
    setSegment(client.segment);
    setStatus(client.status);
    setError('');
    setEditing(true);
  }

  async function save() {
    const trimmedName  = name.trim();
    const trimmedEmail = email.trim().toLowerCase();
    const trimmedPhone = phone.trim();

    // backend requires all fields - validate before sending
    if (!trimmedName) {
      setError('Full name is required');
      return;
    }
    if (!/^[a-zA-Z ]+$/.test(trimmedName)) {
      setError('Name can only contain letters and spaces');
      return;
    }
    if (trimmedName.length > 100) {
      setError('Name must be 100 characters or fewer');
      return;
    }
    if (!dob) {
      setError('Date of birth is required');
      return;
    }
    if (new Date(dob) >= new Date()) {
      setError('Date of birth must be in the past');
      return;
    }
    if (!trimmedEmail && !trimmedPhone) {
      setError('Provide at least an email or phone number');
      return;
    }
    if (trimmedEmail) {
      const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      if (!emailRegex.test(trimmedEmail)) {
        setError('Enter a valid email address (e.g. name@example.com)');
        return;
      }
    }
    if (trimmedPhone && trimmedPhone.length !== 10) {
      setError('Phone must be exactly 10 digits');
      return;
    }

    // activation guard: cannot move to Active without verified KYC + risk profile
    if (status === 'Active' && client.status !== 'Active') {
      if (!hasVerifiedKyc) {
        setError('Cannot activate: at least one KYC document must be verified first.');
        return;
      }
      if (!hasRiskProfile) {
        setError('Cannot activate: risk profile must be completed first.');
        return;
      }
    }

    setError('');
    setSaving(true);
    try {
      // store contact as a JSON string so it round-trips cleanly
      const contactInfo = JSON.stringify({ email: trimmedEmail, phone: trimmedPhone });
      await updateClient(clientId, {
        name: trimmedName,
        dob: dob,
        contactInfo: contactInfo,
        segment: segment,
        status: status,
      });
      setEditing(false);
      onUpdated();
    } catch (e: any) {
      setError(e.response?.data?.message || 'Failed to update');
    }
    setSaving(false);
  }

  // VIEW mode
  if (!editing) {
    return (
      <div className="panel">
        <div className="panel-h">
          <h3>Profile information</h3>
          {canEdit && (
            <button onClick={startEdit} className="btn btn-ghost btn-sm">Edit</button>
          )}
        </div>
        <div className="panel-b">
          <div className="grid grid-cols-2 gap-x-8 gap-y-4 max-w-2xl">
            <Field label="Full name" value={client.name} />
            <Field label="Client ID" value={client.clientId} />
            <Field label="Date of birth" value={client.dob} />
            <Field label="Username" value={client.username || '—'} />
            <Field label="Email" value={contact.email || '—'} />
            <Field label="Phone" value={contact.phone || '—'} />
            <Field label="Segment" value={client.segment} />
            <Field label="Status" value={client.status === 'PENDING_KYC' ? 'Pending' : client.status} />
          </div>

          {/* Activation checklist - only shown for PENDING_KYC clients */}
          {canEdit && client.status === 'PENDING_KYC' && (
            <div className="mt-5 pt-4 border-t border-border-hairline">
              <p className="text-xs uppercase font-semibold text-text-2 mb-2">
                Activation checklist
              </p>
              <ul className="text-sm space-y-1.5">
                <li className={client.dob ? 'text-success' : 'text-text-2'}>
                  {client.dob ? '✓' : '○'} Profile complete (name, DOB, contact)
                </li>
                <li className={hasVerifiedKyc ? 'text-success' : 'text-text-2'}>
                  {hasVerifiedKyc ? '✓' : '○'} At least one KYC document verified
                </li>
                <li className={hasRiskProfile ? 'text-success' : 'text-text-2'}>
                  {hasRiskProfile ? '✓' : '○'} Risk profile completed
                </li>
              </ul>
              {canActivate && client.dob ? (
                <p className="text-xs text-success mt-3">
                  ✓ All prerequisites met — you can change Status to <b>Active</b>.
                </p>
              ) : (
                <p className="text-xs text-warn mt-3">
                  ⚠ Complete all items above before activating this client.
                </p>
              )}
            </div>
          )}
        </div>
      </div>
    );
  }

  // EDIT mode
  return (
    <div className="panel">
      <div className="panel-h"><h3>Edit profile</h3></div>
      <div className="panel-b">
        <div className="grid grid-cols-2 gap-x-6 gap-y-3 max-w-2xl">
          <div>
            <label className="label block mb-1">Full name</label>
            <input
              className="input"
              type="text"
              placeholder="e.g. Rohan Verma"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>

          <div>
            <label className="label block mb-1">Date of birth</label>
            <input
              className="input"
              type="date"
              value={dob}
              onChange={(e) => setDob(e.target.value)}
              max={new Date().toISOString().split('T')[0]}
            />
          </div>

          <div>
            <label className="label block mb-1">Email</label>
            <input
              className="input"
              type="email"
              placeholder="rohan@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div>
            <label className="label block mb-1">Phone</label>
            <input
              className="input"
              type="text"
              placeholder="9123456789"
              value={phone}
              onChange={(e) => setPhone(e.target.value.replace(/\D/g, '').slice(0, 10))}
            />
          </div>

          <div>
            <label className="label block mb-1">Segment</label>
            <select className="input" value={segment} onChange={(e) => setSegment(e.target.value)}>
              <option>Retail</option>
              <option>HNI</option>
              <option>UHNI</option>
            </select>
          </div>

          <div>
            <label className="label block mb-1">Status</label>
            <select className="input" value={status} onChange={(e) => setStatus(e.target.value)}>
              {/* Disable "Active" when prerequisites aren't met (only for clients
                  who aren't already Active). */}
              <option value="Active" disabled={!canActivate && client.status !== 'Active'}>
                Active{!canActivate && client.status !== 'Active' ? ' (KYC + risk required)' : ''}
              </option>
              <option value="Inactive">Inactive</option>
              <option value="PENDING_KYC">Pending</option>
            </select>
            {!canActivate && client.status !== 'Active' && (
              <p className="text-xs text-text-3 mt-1">
                Verify KYC and complete risk profile before activating.
              </p>
            )}
          </div>
        </div>

        {error && (
          <div className="pill pill-danger block mt-4 text-center w-full">{error}</div>
        )}

        <div className="flex gap-2 mt-5">
          <button onClick={save} disabled={saving} className="btn btn-primary btn-sm">
            {saving ? 'Saving...' : 'Save changes'}
          </button>
          <button onClick={() => setEditing(false)} className="btn btn-ghost btn-sm">
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}

function Field({ label, value }: { label: string; value: any }) {
  return (
    <div>
      <p className="label">{label}</p>
      <p className="font-medium">{value || '—'}</p>
    </div>
  );
}

// ─── KYC TAB (RM uploads & verifies) ─────────
function KycTab(props: { clientId: number; kycDocs: any[]; canEdit: boolean; onUpdated: () => void }) {
  const { clientId, kycDocs, canEdit, onUpdated } = props;
  const [docType, setDocType] = useState('AADHAAR');
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  async function handleUpload(e: React.FormEvent) {
    e.preventDefault();
    if (!file) {
      setError('Please choose a file');
      return;
    }
    const MAX_SIZE_MB = 5;
    if (file.size > MAX_SIZE_MB * 1024 * 1024) {
      setError('File size must be 5 MB or less');
      return;
    }
    const allowed = ['image/jpeg', 'image/png', 'image/jpg', 'application/pdf'];
    if (!allowed.includes(file.type)) {
      setError('Only JPEG, PNG, or PDF files are accepted');
      return;
    }
    setError('');
    setUploading(true);
    try {
      await uploadKyc(clientId, docType, file);
      setFile(null);
      const fileInput = document.getElementById('kyc-file') as HTMLInputElement;
      if (fileInput) fileInput.value = '';
      onUpdated();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Upload failed');
    }
    setUploading(false);
  }

  async function markVerified(kycId: number) {
    try {
      await updateKycStatus(kycId, 'Verified');
      onUpdated();
    } catch (err: any) {
      alert('Failed: ' + (err.response?.data?.message || err.message));
    }
  }

  return (
    <div className="space-y-4">
      {/* Upload form (RM only) */}
      {canEdit && (
        <div className="panel">
          <div className="panel-h"><h3>Upload KYC document</h3></div>
          <form onSubmit={handleUpload} className="panel-b">
            <div className="grid grid-cols-2 gap-3 mb-3">
              <div>
                <label className="label block mb-1">Document type</label>
                <select className="input" value={docType} onChange={(e) => setDocType(e.target.value)}>
                  <option value="AADHAAR">Aadhaar Card</option>
                  <option value="PAN">PAN Card</option>
                  <option value="PASSPORT">Passport</option>
                </select>
              </div>
              <div>
                <label className="label block mb-1">Document file</label>
                <input
                  id="kyc-file"
                  className="input"
                  type="file"
                  accept="image/*,.pdf"
                  onChange={(e) => setFile(e.target.files?.[0] || null)}
                />
              </div>
            </div>

            {error && (
              <div className="pill pill-danger block mb-3 text-center w-full">{error}</div>
            )}

            <button type="submit" className="btn btn-primary btn-sm" disabled={uploading}>
              {uploading ? 'Uploading...' : 'Upload document'}
            </button>
          </form>
        </div>
      )}

      {/* Existing docs */}
      <div className="panel">
        <div className="panel-h">
          <h3>Uploaded documents ({kycDocs.length})</h3>
        </div>
        {kycDocs.length === 0 ? (
          <div className="panel-b text-center text-text-2 py-8">No documents uploaded yet.</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">KYC ID</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Type</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Reference</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Verified Date</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Expiry Date</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
                {canEdit && <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Action</th>}
              </tr>
            </thead>
            <tbody>
              {kycDocs.map((d) => (
                <tr key={d.kycId} className={'border-t border-border-hairline' + (d.status === 'Expired' ? ' bg-danger/5' : '')}>
                  <td className="px-5 py-3 mono text-xs">{d.kycId}</td>
                  <td className="px-5 py-3 font-medium">{d.documentType}</td>
                  <td className="px-5 py-3 mono text-xs text-text-2 truncate max-w-xs">
                    {d.documentRefNumber || d.documentRef || '-'}
                  </td>
                  <td className="px-5 py-3 mono text-xs text-text-2">{d.verifiedDate || '-'}</td>
                  <td className="px-5 py-3 mono text-xs text-text-2">{d.expiryDate || '-'}</td>
                  <td className="px-5 py-3">
                    <span className={
                      'pill ' +
                      (d.status === 'Verified' ? 'pill-success' :
                       d.status === 'Pending'  ? 'pill-warn'    :
                       d.status === 'Expired'  ? 'pill-danger'  : 'pill-danger')
                    }>{d.status}</span>
                  </td>
                  {canEdit && (
                    <td className="px-5 py-3 text-right">
                      {d.status === 'Pending' && (
                        <button
                          onClick={() => markVerified(d.kycId)}
                          className="text-xs text-success font-medium"
                        >
                          Mark verified
                        </button>
                      )}
                      {d.status === 'Expired' && (
                        <span className="text-xs text-danger italic">Re-upload required</span>
                      )}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

// ─── GOALS TAB (RM view — read only + delete) ────────────────
function GoalsTab(props: { goals: any[]; onDeleteGoal: (id: number) => void }) {
  const { goals, onDeleteGoal } = props;

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

  function daysRemaining(dateStr: string) {
    const diff = new Date(dateStr).getTime() - Date.now();
    return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
  }

  function fmt(n: number) {
    return Number(n).toLocaleString('en-IN', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  }

  if (goals.length === 0) {
    return (
      <div className="panel">
        <div className="panel-b text-center py-12">
          <p className="text-3xl mb-2">🎯</p>
          <p className="font-semibold">No goals set yet</p>
          <p className="text-sm text-text-2 mt-1">
            Client hasn't created any financial goals yet.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div>
      <p className="text-sm text-text-2 mb-4">{goals.length} goal{goals.length !== 1 ? 's' : ''} set by this client.</p>
      <div className="grid md:grid-cols-2 xl:grid-cols-3 gap-4">
        {goals.map((g: any) => {
          const days = daysRemaining(g.targetDate);
          const isOverdue = days === 0 && g.status !== 'COMPLETED';
          return (
            <div key={g.goalId} className="panel">
              <div className="panel-b">
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

                <div className="bg-surface rounded-lg p-3 mb-3">
                  <p className="text-xs text-text-2 mb-0.5">Target amount</p>
                  <p className="text-xl font-semibold mono">₹{fmt(Number(g.targetAmount))}</p>
                </div>

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

                <button
                  onClick={() => onDeleteGoal(g.goalId)}
                  className="text-xs text-danger hover:underline"
                >
                  Delete goal
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

// ─── RECOMMENDATIONS TAB (RM picks model portfolio, client reviews) ────────
function RecommendationsTab(props: {
  recos: any[];
  modelPortfolios: any[];
  clientRiskClass: string | null;
  clientSegment: string | null;
  clientId: number;
  canEdit: boolean;
  onDelete: (id: number) => void;
  onRecosUpdated: (recos: any[]) => void;
}) {
  const { recos, modelPortfolios, clientRiskClass, clientSegment, clientId, canEdit, onDelete, onRecosUpdated } = props;

  // form state — all self-contained
  const [selectedModelId, setSelectedModelId] = useState<number | null>(null);
  const [customNotes, setCustomNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [msg, setMsg] = useState('');

  // split portfolios: matching risk class first, then others
  // NOTE: Wealthpro service uses Title Case ("Balanced") while GoalsAdvisory uses
  // UPPER_CASE ("BALANCED") — compare case-insensitively to handle both
  const activePortfolios = modelPortfolios.filter((p) => p.status === 'ACTIVE');
  const normalizedClientRisk = clientRiskClass?.toUpperCase() ?? null;
  const matchingPortfolios = activePortfolios.filter(
    (p) => normalizedClientRisk && p.riskClass?.toUpperCase() === normalizedClientRisk,
  );
  const otherPortfolios = activePortfolios.filter(
    (p) => !normalizedClientRisk || p.riskClass?.toUpperCase() !== normalizedClientRisk,
  );

  const selectedPortfolio = activePortfolios.find((p) => p.modelId === selectedModelId) || null;

  function parseWeights(json: string) {
    try { return JSON.parse(json); } catch { return null; }
  }

  function getRiskBadge(rc: string) {
    const u = rc?.toUpperCase();
    if (u === 'CONSERVATIVE') return 'pill-info';
    if (u === 'BALANCED') return 'pill-warn';
    return 'pill-danger';
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setMsg('');

    if (!selectedPortfolio) {
      setError('Please select a model portfolio first.');
      return;
    }

    // Hard block: portfolio risk class must match the client's risk class.
    // A Conservative client must not receive an Aggressive portfolio — the
    // resulting orders will always be rejected by pre-trade SUITABILITY checks.
    if (normalizedClientRisk && selectedPortfolio.riskClass?.toUpperCase() !== normalizedClientRisk) {
      setError(
        `Cannot send this recommendation — "${selectedPortfolio.name}" is a ` +
        `${selectedPortfolio.riskClass} portfolio but this client is ${clientRiskClass}. ` +
        `Please select a portfolio that matches the client's risk class.`
      );
      return;
    }

    // Layer 2 — weights-level suitability check.
    // Even if the risk class tag matches, validate that the actual allocation
    // doesn't contain asset classes that are blocked for this client's
    // risk class or segment (mirrors Suitability Rules 1 & 6).
    const weightsPreCheck = parseWeights(selectedPortfolio.weightsJson);
    if (weightsPreCheck) {
      const assetClasses = Object.keys(weightsPreCheck).map((k: string) => k.toUpperCase());

      // Rule 1 — Conservative clients cannot buy EQUITY
      if (normalizedClientRisk === 'CONSERVATIVE' && assetClasses.includes('EQUITY')) {
        setError(
          `⛔ Cannot send — "${selectedPortfolio.name}" contains EQUITY but this client is Conservative. ` +
          `Conservative clients are blocked from buying equity (Suitability Rule 1). ` +
          `Ask Admin to remove EQUITY from this portfolio's allocation.`
        );
        return;
      }

      // Rule 6 — STRUCTURED products are restricted to UHNI clients only
      if (clientSegment?.toUpperCase() !== 'UHNI' && assetClasses.includes('STRUCTURED')) {
        setError(
          `⛔ Cannot send — "${selectedPortfolio.name}" contains STRUCTURED products but this client is not UHNI. ` +
          `Structured products are restricted to UHNI clients only (Suitability Rule 6). ` +
          `Ask Admin to remove STRUCTURED from this portfolio's allocation.`
        );
        return;
      }
    }

    const weights = parseWeights(selectedPortfolio.weightsJson);
    const proposalJson = JSON.stringify({
      modelPortfolioId: selectedPortfolio.modelId,
      name: selectedPortfolio.name,
      riskClass: selectedPortfolio.riskClass,
      allocation: weights,
      notes: customNotes.trim() || undefined,
    });

    setSubmitting(true);
    try {
      await createRecommendation({
        clientId,
        riskClass: selectedPortfolio.riskClass,
        proposalJson,
        proposedDate: new Date().toISOString().slice(0, 10),
        status: 'SUBMITTED',
      });
      setMsg(`Recommendation based on "${selectedPortfolio.name}" submitted to client.`);
      setSelectedModelId(null);
      setCustomNotes('');
      const updated = await getRecommendationsByClientId(clientId);
      onRecosUpdated(Array.isArray(updated) ? updated : []);
    } catch (err: any) {
      const d = err.response?.data;
      setError(typeof d === 'string' ? d : (d?.message || 'Failed to create recommendation'));
    }
    setSubmitting(false);
  }

  function getStatusPill(s: string) {
    if (s === 'APPROVED') return 'pill-success';
    if (s === 'REJECTED') return 'pill-danger';
    if (s === 'SUBMITTED') return 'pill-info';
    return 'pill-warn';
  }

  function renderProposal(json: string) {
    let p: any;
    try { p = JSON.parse(json); } catch { return <p className="text-sm text-text-2">{json}</p>; }
    if (p.text) return <p className="text-sm text-text-2">{p.text}</p>;

    // new structured format from model portfolio
    return (
      <div className="text-sm space-y-2">
        {p.name && (
          <p className="font-medium text-text">
            📊 {p.name}
          </p>
        )}
        {p.allocation && (
          <div className="flex flex-wrap gap-1.5">
            {Object.entries(p.allocation).map(([asset, pct]) => (
              <span key={asset} className="bg-primary-soft text-primary px-2 py-0.5 rounded text-xs font-medium">
                {asset} {String(pct)}%
              </span>
            ))}
          </div>
        )}
        {p.notes && (
          <p className="text-text-2 text-xs border-l-2 border-border pl-2 mt-1">{p.notes}</p>
        )}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Create form — RM only */}
      {canEdit && (
        <div className="panel">
          <div className="panel-h">
            <h3>New recommendation</h3>
            {clientRiskClass && (
              <span className={'pill ' + getRiskBadge(clientRiskClass)}>
                Client: {clientRiskClass}
              </span>
            )}
          </div>
          <form onSubmit={handleSubmit} className="panel-b space-y-5">

            {/* No risk profile warning */}
            {!clientRiskClass && (
              <div className="bg-warn-soft border border-warn/30 rounded-lg p-3 text-sm text-warn font-medium">
                ⚠ This client doesn't have a risk profile yet. Complete the Risk Profile tab before sending a recommendation.
              </div>
            )}

            {/* No active portfolios warning */}
            {activePortfolios.length === 0 && (
              <div className="bg-surface border border-border rounded-lg p-4 text-center text-sm text-text-2">
                No active model portfolios found. Ask your Admin to create model portfolios first.
              </div>
            )}

            {/* Matching portfolios */}
            {matchingPortfolios.length > 0 && (
              <div>
                <p className="label mb-2">
                  ✅ Portfolios matching client's risk class ({clientRiskClass})
                </p>
                <div className="grid gap-2">
                  {matchingPortfolios.map((p) => {
                    const weights = parseWeights(p.weightsJson);
                    const isSelected = selectedModelId === p.modelId;

                    // Determine which asset classes in this portfolio are
                    // blocked for this client (stale data from before backend fix).
                    // Conservative → EQUITY and STRUCTURED are blocked.
                    const blockedInPortfolio: string[] = [];
                    if (weights && normalizedClientRisk === 'CONSERVATIVE') {
                      const keys = Object.keys(weights).map((k) => k.toUpperCase());
                      if (keys.includes('EQUITY'))     blockedInPortfolio.push('EQUITY');
                      if (keys.includes('STRUCTURED')) blockedInPortfolio.push('STRUCTURED');
                    }
                    const hasBlockedAssets = blockedInPortfolio.length > 0;

                    return (
                      <button
                        key={p.modelId}
                        type="button"
                        onClick={() => { if (!hasBlockedAssets) setSelectedModelId(isSelected ? null : p.modelId); }}
                        className={
                          'w-full text-left rounded-lg border-2 p-3 transition-all ' +
                          (hasBlockedAssets
                            ? 'border-danger/40 bg-danger-soft cursor-not-allowed'
                            : isSelected
                            ? 'border-primary bg-primary-soft'
                            : 'border-border bg-white hover:border-primary/40 hover:bg-surface')
                        }
                        title={hasBlockedAssets
                          ? `This portfolio contains ${blockedInPortfolio.join(', ')} which is not allowed for Conservative clients. Ask Admin to fix the allocation.`
                          : undefined}
                      >
                        <div className="flex items-center justify-between mb-2">
                          <span className="font-semibold text-sm">{p.name}</span>
                          <div className="flex items-center gap-2">
                            <span className={'pill text-xs ' + getRiskBadge(p.riskClass)}>
                              {p.riskClass}
                            </span>
                            {hasBlockedAssets && (
                              <span className="text-xs text-danger font-semibold">⚠ Invalid allocation</span>
                            )}
                            {isSelected && !hasBlockedAssets && (
                              <span className="text-primary text-xs font-bold">✓ Selected</span>
                            )}
                          </div>
                        </div>

                        {/* Blocked asset warning banner inside the card */}
                        {hasBlockedAssets && (
                          <div className="text-xs text-danger bg-white border border-danger/30 rounded px-2 py-1.5 mb-2">
                            ⛔ This portfolio contains <strong>{blockedInPortfolio.join(', ')}</strong> which{' '}
                            Conservative clients cannot buy (Suitability Rule 1).{' '}
                            Ask Admin to remove {blockedInPortfolio.join(' & ')} from this portfolio's allocation.
                          </div>
                        )}

                        {weights && (
                          <div className="flex flex-wrap gap-1.5">
                            {Object.entries(weights).map(([asset, pct]) => {
                              const isBlocked = blockedInPortfolio.includes(asset.toUpperCase());
                              return (
                                <span
                                  key={asset}
                                  className={
                                    'rounded px-2 py-0.5 text-xs border ' +
                                    (isBlocked
                                      ? 'bg-danger/10 border-danger/40 text-danger line-through'
                                      : 'bg-white border-border')
                                  }
                                >
                                  <span className="font-medium">{asset}</span>
                                  <span className="ml-1 opacity-70">{String(pct)}%</span>
                                  {isBlocked && <span className="ml-1">⛔</span>}
                                </span>
                              );
                            })}
                          </div>
                        )}
                      </button>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Other portfolios (risk class mismatch) — shown for reference only, cannot be selected */}
            {otherPortfolios.length > 0 && (
              <div>
                <p className="label mb-1 text-text-3">
                  ⛔ Incompatible portfolios — cannot send to this client
                </p>
                <p className="text-xs text-text-3 mb-2">
                  These portfolios have a different risk class from the client ({clientRiskClass}).
                  Sending them would cause all orders to be rejected by pre-trade checks.
                </p>
                <div className="grid gap-2">
                  {otherPortfolios.map((p) => {
                    const weights = parseWeights(p.weightsJson);
                    return (
                      <div
                        key={p.modelId}
                        className="w-full text-left rounded-lg border-2 p-3 border-border bg-surface opacity-50 cursor-not-allowed select-none"
                        title={`Cannot select — ${p.riskClass} portfolio does not match client's ${clientRiskClass} risk class`}
                      >
                        <div className="flex items-center justify-between mb-2">
                          <span className="font-semibold text-sm text-text-2">{p.name}</span>
                          <div className="flex items-center gap-2">
                            <span className={'pill text-xs ' + getRiskBadge(p.riskClass)}>
                              {p.riskClass}
                            </span>
                            <span className="text-xs text-danger font-medium">⛔ Blocked</span>
                          </div>
                        </div>
                        {weights && (
                          <div className="flex flex-wrap gap-1.5">
                            {Object.entries(weights).map(([asset, pct]) => (
                              <span
                                key={asset}
                                className="bg-white border border-border rounded px-2 py-0.5 text-xs"
                              >
                                <span className="font-medium">{asset}</span>
                                <span className="text-text-2 ml-1">{String(pct)}%</span>
                              </span>
                            ))}
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Selected portfolio preview + notes */}
            {selectedPortfolio && (
              <div className="bg-surface rounded-lg p-4 border border-border">
                <p className="text-xs font-semibold text-text-2 uppercase mb-2">Selected portfolio preview</p>
                <p className="font-semibold mb-1">{selectedPortfolio.name}</p>
                <div className="flex flex-wrap gap-1.5 mb-3">
                  {Object.entries(parseWeights(selectedPortfolio.weightsJson) || {}).map(([asset, pct]) => (
                    <span key={asset} className="bg-primary-soft text-primary px-2 py-0.5 rounded text-xs font-medium">
                      {asset} {String(pct)}%
                    </span>
                  ))}
                </div>
                <label className="label block mb-1">
                  Additional notes for client <span className="text-text-3">(optional)</span>
                </label>
                <textarea
                  className="input resize-none"
                  rows={3}
                  placeholder="e.g. Based on your 5-year wealth goal, I suggest this allocation. We can review quarterly..."
                  value={customNotes}
                  onChange={(e) => setCustomNotes(e.target.value)}
                />
              </div>
            )}

            {error && <div className="pill pill-danger block text-center w-full">{error}</div>}
            {msg   && <div className="pill pill-success block text-center w-full">{msg}</div>}

            <button
              type="submit"
              className="btn btn-primary btn-sm"
              disabled={submitting || !selectedPortfolio}
            >
              {submitting ? 'Submitting...' : 'Submit recommendation to client'}
            </button>
          </form>
        </div>
      )}

      {/* Existing recommendations history */}
      <div className="panel">
        <div className="panel-h">
          <h3>Sent recommendations ({recos.length})</h3>
        </div>
        {recos.length === 0 ? (
          <div className="panel-b text-center text-text-2 py-8">
            No recommendations sent yet.
          </div>
        ) : (
          <div className="divide-y divide-border-hairline">
            {recos.map((r: any) => (
              <div key={r.recoId} className="panel-b">
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-text-3 text-xs mono">
                      REC-{String(r.recoId).padStart(4, '0')}
                    </span>
                    <span className={'pill ' + getStatusPill(r.status)}>{r.status}</span>
                    <span className={'pill ' + getRiskBadge(r.riskClass)}>{r.riskClass}</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="text-xs text-text-3">{r.proposedDate}</span>
                    {canEdit && (
                      <button
                        onClick={() => onDelete(r.recoId)}
                        className="text-xs text-danger hover:underline"
                      >
                        Delete
                      </button>
                    )}
                  </div>
                </div>
                {renderProposal(r.proposalJson)}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

// ─── ACCOUNT & FUNDS TAB (RM creates PBOR account and deposits funds) ──────
function AccountFundsTab(props: {
  clientId: number;
  account: any;
  cashBalance: number;
  ledgerEntries: any[];
  canEdit: boolean;
  onUpdated: () => void;
}) {
  const { clientId, account, cashBalance, ledgerEntries, canEdit, onUpdated } = props;

  const [creatingAccount, setCreatingAccount] = useState(false);
  const [createError, setCreateError] = useState('');

  const [depositAmount, setDepositAmount] = useState('');
  const [depositNarrative, setDepositNarrative] = useState('');
  const [depositing, setDepositing] = useState(false);
  const [depositError, setDepositError] = useState('');
  const [depositMsg, setDepositMsg] = useState('');

  function fmt(n: number) {
    return Number(n).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  async function handleCreateAccount() {
    setCreateError('');
    setCreatingAccount(true);
    try {
      await createAccount({
        clientId,
        accountType: 'INDIVIDUAL',
        baseCurrency: 'INR',
        status: 'ACTIVE',
      });
      onUpdated();
    } catch (err: any) {
      setCreateError(err.response?.data?.message || 'Failed to create account');
    }
    setCreatingAccount(false);
  }

  async function handleDeposit(e: React.FormEvent) {
    e.preventDefault();
    setDepositError('');
    setDepositMsg('');
    const amt = parseFloat(depositAmount);
    if (!depositAmount || isNaN(amt) || amt <= 0) {
      setDepositError('Enter a valid positive amount');
      return;
    }
    if (amt > 10_000_000) {
      setDepositError('Maximum single deposit is ₹1 Crore');
      return;
    }
    setDepositing(true);
    try {
      await createCashLedgerEntry({
        accountId: account.accountId,
        amount: amt,
        txnType: 'CREDIT',
        narrative: depositNarrative.trim() || 'RM fund deposit',
      });
      setDepositMsg(`₹${fmt(amt)} deposited successfully.`);
      setDepositAmount('');
      setDepositNarrative('');
      onUpdated();
    } catch (err: any) {
      setDepositError(err.response?.data?.message || 'Deposit failed');
    }
    setDepositing(false);
  }

  // No account yet
  if (!account) {
    return (
      <div className="space-y-4">
        <div className="panel">
          <div className="panel-b text-center py-12">
            <p className="text-3xl mb-3">🏦</p>
            <p className="font-semibold text-lg mb-1">No PBOR account found</p>
            <p className="text-sm text-text-2 mb-5">
              This client doesn't have a portfolio account yet. Create one to enable cash deposits and order placement.
            </p>
            {canEdit && (
              <>
                {createError && (
                  <p className="text-danger text-sm mb-3">{createError}</p>
                )}
                <button
                  onClick={handleCreateAccount}
                  disabled={creatingAccount}
                  className="btn btn-primary"
                >
                  {creatingAccount ? 'Creating...' : 'Create Portfolio Account'}
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Account summary card */}
      <div className="panel">
        <div className="panel-h">
          <h3>Portfolio Account</h3>
          <span className="pill pill-success">{account.status || 'ACTIVE'}</span>
        </div>
        <div className="panel-b">
          <div className="grid grid-cols-3 gap-6">
            <div>
              <p className="label">Account ID</p>
              <p className="font-semibold mono text-lg">{account.accountId}</p>
            </div>
            <div>
              <p className="label">Account type</p>
              <p className="font-medium">{account.accountType || 'INDIVIDUAL'}</p>
            </div>
            <div>
              <p className="label">Base currency</p>
              <p className="font-medium">{account.baseCurrency || 'INR'}</p>
            </div>
          </div>

          {/* Balance highlight */}
          <div className="mt-5 bg-surface rounded-xl p-5 flex items-center gap-4">
            <div className="text-3xl">💰</div>
            <div>
              <p className="text-xs text-text-2 uppercase font-semibold tracking-wider mb-0.5">
                Available Cash Balance
              </p>
              <p className={
                'text-3xl font-bold mono ' +
                (cashBalance > 0 ? 'text-success' : cashBalance < 0 ? 'text-danger' : 'text-text-2')
              }>
                ₹{fmt(cashBalance)}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Deposit funds panel (RM only) */}
      {canEdit && (
        <div className="panel">
          <div className="panel-h">
            <h3>Add Funds</h3>
            <p className="text-sm text-text-2">Credit cash to client's portfolio account</p>
          </div>
          <form onSubmit={handleDeposit} className="panel-b">
            <div className="grid grid-cols-2 gap-4 mb-4">
              <div>
                <label className="label block mb-1">Deposit amount (₹)</label>
                <input
                  className="input"
                  type="number"
                  min="1"
                  max="10000000"
                  step="0.01"
                  placeholder="e.g. 500000"
                  value={depositAmount}
                  onKeyDown={(e) => {
                    // Block minus key and 'e' (scientific notation) in number inputs
                    if (e.key === '-' || e.key === 'e' || e.key === 'E') {
                      e.preventDefault();
                    }
                  }}
                  onChange={(e) => {
                    const val = e.target.value;
                    // Block negative values — only accept empty or non-negative numbers
                    if (val === '' || Number(val) >= 0) {
                      setDepositAmount(val);
                    }
                  }}
                />
                <p className="text-xs text-text-3 mt-1">Min ₹1 · Max ₹1,00,00,000</p>
              </div>
              <div>
                <label className="label block mb-1">Narrative / Reference (optional)</label>
                <input
                  className="input"
                  type="text"
                  placeholder="e.g. Initial funding, Bank transfer ref 123"
                  value={depositNarrative}
                  onChange={(e) => setDepositNarrative(e.target.value)}
                  maxLength={200}
                />
              </div>
            </div>

            {depositError && (
              <div className="pill pill-danger block mb-3 text-center w-full">{depositError}</div>
            )}
            {depositMsg && (
              <div className="pill pill-success block mb-3 text-center w-full">{depositMsg}</div>
            )}

            <button type="submit" className="btn btn-primary btn-sm" disabled={depositing}>
              {depositing ? 'Processing...' : '+ Deposit Funds'}
            </button>
          </form>
        </div>
      )}

      {/* Transaction history */}
      <div className="panel">
        <div className="panel-h">
          <h3>Transaction History</h3>
          <span className="text-xs text-text-2">{ledgerEntries.length} entries</span>
        </div>
        {ledgerEntries.length === 0 ? (
          <div className="panel-b text-center text-text-2 py-10">
            No transactions yet.
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">ID</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Type</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Amount (₹)</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Narrative</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Date</th>
              </tr>
            </thead>
            <tbody>
              {[...ledgerEntries].reverse().map((entry: any) => (
                <tr key={entry.ledgerId || entry.id} className="border-t border-border-hairline hover:bg-surface transition-colors">
                  <td className="px-5 py-3 mono text-xs text-text-2">{entry.ledgerId || entry.id}</td>
                  <td className="px-5 py-3">
                    <span className={
                      'pill ' +
                      (entry.txnType === 'CREDIT' ? 'pill-success' : 'pill-danger')
                    }>
                      {entry.txnType}
                    </span>
                  </td>
                  <td className={
                    'px-5 py-3 mono text-right font-semibold ' +
                    (entry.txnType === 'CREDIT' ? 'text-success' : 'text-danger')
                  }>
                    {entry.txnType === 'CREDIT' ? '+' : '-'}₹{fmt(Number(entry.amount))}
                  </td>
                  <td className="px-5 py-3 text-text-2 max-w-xs truncate">
                    {entry.narrative || entry.description || '—'}
                  </td>
                  <td className="px-5 py-3 mono text-xs text-text-2">
                    {entry.txnDate || entry.transactionDate || entry.createdDate || '—'}
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

// ─── RISK PROFILE TAB (RM fills on behalf of client) ────────
function RiskTab(props: { clientId: number; risk: any; canEdit: boolean; onUpdated: () => void }) {
  const { clientId, risk, canEdit, onUpdated } = props;

  const [answers, setAnswers] = useState<{ [k: string]: string }>({
    q1: '', q2: '', q3: '', q4: '', q5: '',
  });
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const questions = [
    { id: 'q1', text: 'What is your investment time horizon?',
      options: { A: 'Less than 1 year', B: '1-3 years', C: '3-5 years', D: 'More than 5 years' } },
    { id: 'q2', text: 'How would you react to a 20% drop in your portfolio in one month?',
      options: { A: 'Sell everything immediately', B: 'Sell some holdings', C: 'Hold and wait', D: 'Buy more at lower prices' } },
    { id: 'q3', text: 'What is your primary investment goal?',
      options: { A: 'Capital preservation', B: 'Steady income', C: 'Balanced growth', D: 'Aggressive growth' } },
    { id: 'q4', text: 'How much investment experience do you have?',
      options: { A: 'None', B: 'Basic (mutual funds)', C: 'Intermediate (stocks, ETFs)', D: 'Advanced (derivatives, structured products)' } },
    { id: 'q5', text: 'What percent of your savings are you willing to invest?',
      options: { A: 'Less than 25%', B: '25-50%', C: '50-75%', D: 'More than 75%' } },
  ];

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    for (let i = 0; i < questions.length; i++) {
      if (!answers[questions[i].id]) {
        setError('Please answer all 5 questions');
        return;
      }
    }
    setError('');
    setSaving(true);
    try {
      await createRiskProfile(clientId, {
        answers: answers,
        assessedDate: new Date().toISOString().split('T')[0],
      });
      onUpdated();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save risk profile');
    }
    setSaving(false);
  }

  // Existing risk profile - show results
  if (risk) {
    let storedAnswers: any = {};
    try {
      if (typeof risk.questionnaireJSON === 'string') {
        storedAnswers = JSON.parse(risk.questionnaireJSON);
      } else if (risk.answers) {
        storedAnswers = risk.answers;
      }
    } catch (e) {
      storedAnswers = {};
    }

    return (
      <div className="panel">
        <div className="panel-h"><h3>Risk Profile</h3></div>
        <div className="panel-b">
          <div className="grid grid-cols-3 gap-4 mb-6">
            <div>
              <p className="label">Risk class</p>
              <p className="text-2xl font-semibold mt-1">{risk.riskClass}</p>
            </div>
            <div>
              <p className="label">Risk score</p>
              <p className="text-2xl font-semibold mono mt-1">{risk.riskScore}</p>
            </div>
            <div>
              <p className="label">Assessed on</p>
              <p className="font-medium mt-1">{risk.assessedDate || '-'}</p>
            </div>
          </div>

          <h4 className="text-sm font-semibold mb-3 mt-4">Recorded answers</h4>
          <table className="w-full text-sm">
            <tbody>
              {questions.map((q) => (
                <tr key={q.id} className="border-b border-border-hairline">
                  <td className="py-2 pr-4 align-top w-12 mono text-text-2 text-xs">{q.id}</td>
                  <td className="py-2 pr-4 align-top text-text-2">{q.text}</td>
                  <td className="py-2 align-top font-medium">{storedAnswers[q.id] || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  }

  // No risk profile yet - non-RM viewers see empty state
  if (!canEdit) {
    return (
      <div className="panel">
        <div className="panel-b text-center py-10">
          <p className="text-3xl mb-2">📋</p>
          <p className="font-semibold">Risk profile not yet completed</p>
          <p className="text-sm text-text-2 mt-1">
            The relationship manager hasn't completed the risk assessment for this client.
          </p>
        </div>
      </div>
    );
  }

  // RM sees the questionnaire
  return (
    <div className="panel">
      <div className="panel-h"><h3>Risk Assessment Questionnaire</h3></div>
      <form onSubmit={handleSubmit} className="panel-b">
        <p className="text-sm text-text-2 mb-5">
          Walk through these 5 questions with the client. Score and risk class will be auto-calculated.
        </p>

        {questions.map((q) => (
          <div key={q.id} className="mb-5 pb-4 border-b border-border-hairline">
            <p className="font-medium mb-2">
              <span className="mono text-text-2 text-xs mr-2">{q.id.toUpperCase()}.</span>
              {q.text}
            </p>
            <div className="grid grid-cols-1 gap-1.5">
              {Object.entries(q.options).map(([key, val]) => (
                <label key={key} className="flex items-center gap-2 cursor-pointer text-sm">
                  <input
                    type="radio"
                    name={q.id}
                    value={key}
                    checked={answers[q.id] === key}
                    onChange={() => setAnswers({ ...answers, [q.id]: key })}
                  />
                  <span className="font-medium mono text-xs w-4">{key}.</span>
                  <span>{val}</span>
                </label>
              ))}
            </div>
          </div>
        ))}

        {error && (
          <div className="pill pill-danger block mb-3 text-center w-full">{error}</div>
        )}

        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Saving...' : 'Submit risk assessment'}
        </button>
      </form>
    </div>
  );
}
