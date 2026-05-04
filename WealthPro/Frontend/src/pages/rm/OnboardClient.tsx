import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { api } from '@/api/client';
import { updateClient } from '@/api/clients';
import { createAccount } from '@/api/accounts';

const SEGMENTS = ['Retail', 'HNI', 'UHNI'];

export default function OnboardClient() {
  // Login credentials
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  // Personal info
  const [name, setName] = useState('');
  const [dob, setDob] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [segment, setSegment] = useState('Retail');

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState('');

  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    // ── validation ─────────────────────────────────
    const trimmedUsername = username.trim();
    const trimmedName     = name.trim();
    const trimmedEmail    = email.trim().toLowerCase();

    if (!trimmedUsername || trimmedUsername.length < 4) {
      setError('Username must be at least 4 characters');
      return;
    }
    if (!/^[a-zA-Z0-9._-]+$/.test(trimmedUsername)) {
      setError('Username can only have letters, numbers, . _ -');
      return;
    }
    if (trimmedUsername.length > 30) {
      setError('Username must be 30 characters or fewer');
      return;
    }
    if (!password || password.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }
    if (!/(?=.*[a-z])/.test(password)) {
      setError('Password must contain at least one lowercase letter');
      return;
    }
    if (!/(?=.*[A-Z])/.test(password)) {
      setError('Password must contain at least one uppercase letter');
      return;
    }
    if (!/(?=.*\d)/.test(password)) {
      setError('Password must contain at least one number');
      return;
    }
    if (!/(?=.*[@#$%^&*!])/.test(password)) {
      setError('Password must contain at least one special character (@#$%^&*!)');
      return;
    }
    if (!trimmedName) {
      setError('Full name is required');
      return;
    }
    if (!/^[a-zA-Z ]+$/.test(trimmedName)) {
      setError('Name must contain only letters and spaces');
      return;
    }
    if (trimmedName.length > 100) {
      setError('Name must be 100 characters or fewer');
      return;
    }
    if (!dob) {
      setError('Please pick a date of birth');
      return;
    }
    const dobDate = new Date(dob);
    if (dobDate >= new Date()) {
      setError('Date of birth must be in the past');
      return;
    }
    // Must be at least 18 years old to open an investment account
    const minAgeDate = new Date();
    minAgeDate.setFullYear(minAgeDate.getFullYear() - 18);
    if (dobDate > minAgeDate) {
      setError('Client must be at least 18 years old');
      return;
    }
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!trimmedEmail) {
      setError('Email address is required');
      return;
    }
    if (!emailRegex.test(trimmedEmail)) {
      setError('Enter a valid email address (e.g. name@example.com)');
      return;
    }
    if (phone.length !== 10) {
      setError('Phone must be exactly 10 digits');
      return;
    }

    setError('');
    setLoading(true);

    try {
      // step 1: create the user account + stub client
      setStep('Creating user account...');
      await api.post('/auth/register/client', {
        username: trimmedUsername,
        password,
        name: trimmedName,
        email: trimmedEmail,
        phone,
      });

      // step 2: find the new client's id by username
      setStep('Linking client record...');
      const clientRes = await api.get('/api/clients/by-username/' + trimmedUsername);
      const newClient = clientRes.data;
      if (!newClient || !newClient.clientId) {
        throw new Error('Could not find newly created client');
      }

      // step 3: fill in DOB, segment, contact
      setStep('Saving profile details...');
      const contactInfo = JSON.stringify({ email: trimmedEmail, phone });
      await updateClient(newClient.clientId, {
        name,
        dob,
        contactInfo,
        segment,
        status: 'PENDING_KYC',
      });

      // step 4: create a PBOR investment account for the client
      // non-critical — if PBOR is down we still navigate; dealer can create it later
      setStep('Creating investment account...');
      try {
        await createAccount({
          clientId: newClient.clientId,
          accountType: 'INDIVIDUAL',
          baseCurrency: 'INR',
          status: 'ACTIVE',
        });
      } catch (pborErr) {
        // not blocking — dealer can create the account from order detail page
      }

      // go to detail page so RM can upload KYC + fill risk
      navigate('/rm/clients/' + newClient.clientId);
    } catch (err: any) {
      if (err.response?.status === 409) {
        setError('That username is already taken — choose another.');
      } else {
        setError(err.response?.data?.message || err.message || 'Onboarding failed');
      }
    }

    setLoading(false);
    setStep('');
  }

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Onboard New Client</h1>
          <p className="text-sm text-text-2">
            Step 1 of 3 — Basic info. After this you'll upload KYC docs and complete risk profile.
          </p>
        </div>
        <Link to="/rm/clients" className="btn btn-ghost btn-sm">← Back to clients</Link>
      </div>

      <div className="max-w-2xl panel">
        <div className="panel-h"><h3>Client information</h3></div>
        {/* autocomplete="off" on form + hidden dummy fields stop browsers from
            injecting the RM's saved credentials into the new-client fields */}
        <form onSubmit={handleSubmit} className="panel-b" autoComplete="off">
          {/* Dummy hidden fields absorb any browser credential autofill before the real inputs */}
          <input type="text" name="prevent_autofill_user" style={{ display: 'none' }} readOnly />
          <input type="password" name="prevent_autofill_pass" style={{ display: 'none' }} readOnly />

          {/* Login credentials section */}
          <h4 className="text-xs uppercase font-semibold text-text-2 mb-3">Login credentials</h4>
          <div className="grid grid-cols-2 gap-3 mb-4">
            <div>
              <label className="label block mb-1">Username</label>
              <input
                className="input mono"
                type="text"
                placeholder="e.g. rohan.verma"
                autoComplete="off"
                value={username}
                onChange={(e) => setUsername(e.target.value.trim())}
              />
            </div>
            <div>
              <label className="label block mb-1">Initial password</label>
              <input
                className="input"
                type="password"
                placeholder="Min 8 chars, upper, number, symbol"
                autoComplete="new-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <p className="text-xs text-text-3 mt-1">
                Requires uppercase, number &amp; special char. Share securely.
              </p>
            </div>
          </div>

          {/* Personal info section */}
          <h4 className="text-xs uppercase font-semibold text-text-2 mb-3 pt-2 border-t border-border-hairline mt-4">
            Personal information
          </h4>

          <div className="mb-3">
            <label className="label block mb-1">Full name</label>
            <input
              className="input"
              type="text"
              placeholder="e.g. Rohan Verma"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>

          <div className="grid grid-cols-2 gap-3 mb-3">
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
              <label className="label block mb-1">Segment</label>
              <select
                className="input"
                value={segment}
                onChange={(e) => setSegment(e.target.value)}
              >
                {SEGMENTS.map((s) => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3 mb-3">
            <div>
              <label className="label block mb-1">Email</label>
              <input
                className="input"
                type="email"
                placeholder="rohan@example.com"
                autoComplete="off"
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
          </div>

          <p className="text-xs text-text-3 mb-3 pt-2 border-t border-border-hairline mt-2">
            ⓘ Client starts as <b>Pending</b>. Activate them after their KYC and risk
            profile are completed.
          </p>

          {error && (
            <div className="pill pill-danger block mb-3 text-center w-full">{error}</div>
          )}
          {loading && step && (
            <div className="pill pill-info block mb-3 text-center w-full">{step}</div>
          )}

          <div className="flex justify-end gap-2 mt-4">
            <Link to="/rm/clients" className="btn btn-ghost">Cancel</Link>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating...' : 'Create client →'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
