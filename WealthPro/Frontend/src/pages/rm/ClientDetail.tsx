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
  updateRecommendationStatus,
  deleteRecommendation,
} from '@/api/recommendations';
import { useAuth } from '@/auth/store';

const TABS = ['Profile', 'KYC', 'Risk Profile', 'Goals', 'Recommendations'];
const RISK_CLASSES = ['CONSERVATIVE', 'BALANCED', 'AGGRESSIVE'];

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
  const [recoRiskClass, setRecoRiskClass] = useState('BALANCED');
  const [recoProposal, setRecoProposal] = useState('');
  const [recoSubmitting, setRecoSubmitting] = useState(false);
  const [recoError, setRecoError] = useState('');
  const [recoMsg, setRecoMsg] = useState('');

  useEffect(() => {
    loadAll();
  }, [clientId]);

  async function loadAll() {
    setLoading(true);
    try {
      // Fetch all 5 data sources in parallel instead of one-by-one
      const [clientRes, kycRes, riskRes, goalsRes, recosRes] = await Promise.allSettled([
        getClientById(clientId),
        getKycDocs(clientId),
        getRiskProfile(clientId),
        getGoalsByClientId(clientId),
        getRecommendationsByClientId(clientId),
      ]);

      if (clientRes.status === 'fulfilled') setClient(clientRes.value);
      setKycDocs(kycRes.status === 'fulfilled' && Array.isArray(kycRes.value) ? kycRes.value : []);
      setRisk(riskRes.status === 'fulfilled' ? riskRes.value : null);
      setGoals(goalsRes.status === 'fulfilled' && Array.isArray(goalsRes.value) ? goalsRes.value : []);
      setRecos(recosRes.status === 'fulfilled' && Array.isArray(recosRes.value) ? recosRes.value : []);

    } catch (e) {
    }
    setLoading(false);
  }

  async function handleCreateReco(e: React.FormEvent) {
    e.preventDefault();
    setRecoError('');
    setRecoMsg('');
    if (!recoProposal.trim()) {
      setRecoError('Proposal text is required');
      return;
    }
    setRecoSubmitting(true);
    try {
      await createRecommendation({
        clientId,
        riskClass: recoRiskClass,
        proposalJson: JSON.stringify({ text: recoProposal }),
        proposedDate: new Date().toISOString().slice(0, 10),
        status: 'SUBMITTED',
      });
      setRecoMsg('Recommendation submitted to client.');
      setRecoProposal('');
      const r = await getRecommendationsByClientId(clientId);
      setRecos(Array.isArray(r) ? r : []);
    } catch (err: any) {
      const d = err.response?.data;
      setRecoError(typeof d === 'string' ? d : (d?.message || 'Failed to create recommendation'));
    }
    setRecoSubmitting(false);
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
          riskClass={recoRiskClass}
          proposal={recoProposal}
          submitting={recoSubmitting}
          error={recoError}
          msg={recoMsg}
          canEdit={isRM}
          onRiskClassChange={setRecoRiskClass}
          onProposalChange={setRecoProposal}
          onSubmit={handleCreateReco}
          onDelete={handleDeleteReco}
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
            <Field label="Status" value={client.status} />
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
              <option value="PENDING_KYC">PENDING_KYC</option>
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
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
                {canEdit && <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Action</th>}
              </tr>
            </thead>
            <tbody>
              {kycDocs.map((d) => (
                <tr key={d.kycId} className="border-t border-border-hairline">
                  <td className="px-5 py-3 mono text-xs">{d.kycId}</td>
                  <td className="px-5 py-3 font-medium">{d.documentType}</td>
                  <td className="px-5 py-3 mono text-xs text-text-2 truncate max-w-xs">
                    {d.documentRefNumber || d.documentRef || '-'}
                  </td>
                  <td className="px-5 py-3 mono text-xs text-text-2">{d.verifiedDate || '-'}</td>
                  <td className="px-5 py-3">
                    <span className={
                      'pill ' +
                      (d.status === 'Verified' ? 'pill-success' :
                       d.status === 'Pending' ? 'pill-warn' : 'pill-danger')
                    }>{d.status}</span>
                  </td>
                  {canEdit && (
                    <td className="px-5 py-3 text-right">
                      {d.status !== 'Verified' && (
                        <button
                          onClick={() => markVerified(d.kycId)}
                          className="text-xs text-success font-medium"
                        >
                          Mark verified
                        </button>
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

// ─── RECOMMENDATIONS TAB (RM creates, client reviews) ────────
function RecommendationsTab(props: {
  recos: any[];
  riskClass: string;
  proposal: string;
  submitting: boolean;
  error: string;
  msg: string;
  canEdit: boolean;
  onRiskClassChange: (v: string) => void;
  onProposalChange: (v: string) => void;
  onSubmit: (e: React.FormEvent) => void;
  onDelete: (id: number) => void;
}) {
  const { recos, riskClass, proposal, submitting, error, msg, canEdit,
    onRiskClassChange, onProposalChange, onSubmit, onDelete } = props;

  function getStatusPill(s: string) {
    if (s === 'APPROVED') return 'pill-success';
    if (s === 'REJECTED') return 'pill-danger';
    if (s === 'SUBMITTED') return 'pill-info';
    return 'pill-warn';
  }

  function renderProposal(json: string) {
    let p: any;
    try { p = JSON.parse(json); } catch { return <p className="text-sm text-text-2">{json}</p>; }

    // plain text proposal (our format)
    if (p.text) return <p className="text-sm text-text-2">{p.text}</p>;

    // single-field summary
    if (p.summary && Object.keys(p).length === 1)
      return <p className="text-sm text-text-2">{p.summary}</p>;

    // structured proposal — render as readable chips
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

  return (
    <div className="space-y-4">
      {/* Create form — RM only */}
      {canEdit && (
        <div className="panel">
          <div className="panel-h"><h3>New recommendation</h3></div>
          <form onSubmit={onSubmit} className="panel-b">
            <p className="text-sm text-text-2 mb-4">
              Write an investment proposal for this client based on their goals and risk profile.
            </p>

            <div className="mb-3">
              <label className="label block mb-1">Risk class</label>
              <div className="flex gap-2">
                {RISK_CLASSES.map((r) => (
                  <button
                    key={r}
                    type="button"
                    onClick={() => onRiskClassChange(r)}
                    className={
                      'flex-1 py-2 px-3 text-sm rounded border font-medium ' +
                      (riskClass === r
                        ? 'bg-primary text-white border-primary'
                        : 'bg-white text-text-2 border-border')
                    }
                  >
                    {r}
                  </button>
                ))}
              </div>
            </div>

            <div className="mb-4">
              <label className="label block mb-1">Investment proposal</label>
              <textarea
                className="input"
                rows={4}
                placeholder="e.g. Based on the client's BALANCED risk profile and 5-year WEALTH goal of ₹50L, I recommend a 60% equity / 30% debt / 10% gold allocation via SBI Bluechip Fund, HDFC Bank, and Kotak Gold Fund..."
                value={proposal}
                onChange={(e) => onProposalChange(e.target.value)}
              />
            </div>

            {error && <div className="pill pill-danger block mb-3 text-center w-full">{error}</div>}
            {msg && <div className="pill pill-success block mb-3 text-center w-full">{msg}</div>}

            <button type="submit" className="btn btn-primary btn-sm" disabled={submitting}>
              {submitting ? 'Submitting...' : 'Submit recommendation'}
            </button>
          </form>
        </div>
      )}

      {/* Existing recommendations */}
      <div className="panel">
        <div className="panel-h">
          <h3>Recommendations ({recos.length})</h3>
        </div>
        {recos.length === 0 ? (
          <div className="panel-b text-center text-text-2 py-8">
            No recommendations yet.
          </div>
        ) : (
          <div className="divide-y divide-border-hairline">
            {recos.map((r: any) => (
              <div key={r.recoId} className="panel-b">
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-text-3 text-xs mono">REC-{String(r.recoId).padStart(4, '0')}</span>
                    <span className={'pill ' + getStatusPill(r.status)}>{r.status}</span>
                    <span className="pill pill-info">{r.riskClass}</span>
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
