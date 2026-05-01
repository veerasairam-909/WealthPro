import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { registerStaff } from '@/api/auth';
import { api } from '@/api/client';

const ROLES = ['RM', 'DEALER', 'COMPLIANCE'];

export default function RegisterStaff() {
  const [role, setRole] = useState('RM');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // for live username check
  const [usernameStatus, setUsernameStatus] = useState(''); // '', 'checking', 'taken', 'available'

  const navigate = useNavigate();

  // check if username is already taken
  async function checkUsername() {
    if (!username || username.length < 4) {
      setUsernameStatus('');
      return;
    }
    setUsernameStatus('checking');
    try {
      const res = await api.get('/auth/users/' + username);
      if (res.data && res.data.username) {
        setUsernameStatus('taken');
      }
    } catch (err: any) {
      // 404 means username is available
      if (err.response?.status === 404) {
        setUsernameStatus('available');
      } else {
        setUsernameStatus('');
      }
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    // validation
    const trimmedUsername = username.trim();
    const trimmedName     = name.trim();
    const trimmedEmail    = email.trim().toLowerCase();

    if (!trimmedUsername || trimmedUsername.length < 4) {
      setError('Username must be at least 4 characters');
      return;
    }
    if (!/^[a-zA-Z0-9._-]+$/.test(trimmedUsername)) {
      setError('Username can only contain letters, numbers, . _ -');
      return;
    }
    if (trimmedUsername.length > 30) {
      setError('Username must be 30 characters or fewer');
      return;
    }
    if (usernameStatus === 'taken') {
      setError('This username is already taken — please choose another');
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
      setError('Name can only contain letters and spaces');
      return;
    }
    if (trimmedName.length > 100) {
      setError('Name must be 100 characters or fewer');
      return;
    }
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
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
      await registerStaff({
        username: trimmedUsername,
        password: password,
        name: trimmedName,
        email: trimmedEmail,
        phone: phone,
        roles: role,
      });
      navigate('/admin/users');
    } catch (err: any) {
      if (err.response?.status === 409) {
        setError('Username already exists');
      } else {
        setError(err.response?.data?.message || 'Failed to create user');
      }
    }

    setLoading(false);
  }

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Register Staff</h1>
          <p className="text-sm text-text-2">Add a new staff member</p>
        </div>
        <Link to="/admin/users" className="btn btn-ghost btn-sm">← Back</Link>
      </div>

      <div className="max-w-2xl panel">
        <div className="panel-h"><h3>New User Details</h3></div>
        <form onSubmit={handleSubmit} className="panel-b">

          {/* role buttons */}
          <div className="mb-4">
            <label className="label block mb-2">Role</label>
            <div className="grid grid-cols-4 gap-2">
              {ROLES.map((r) => (
                <button
                  key={r}
                  type="button"
                  onClick={() => setRole(r)}
                  className={
                    'py-2 px-3 text-sm rounded border font-medium ' +
                    (role === r
                      ? 'bg-primary text-white border-primary'
                      : 'bg-white text-text border-border')
                  }
                >
                  {r}
                </button>
              ))}
            </div>
          </div>

          <div className="mb-3">
            <label className="label block mb-1">Full name</label>
            <input
              className="input"
              type="text"
              placeholder="e.g. Anita Sharma"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>

          <div className="grid grid-cols-2 gap-3 mb-3">
            <div>
              <label className="label block mb-1">Email</label>
              <input
                className="input"
                type="email"
                placeholder="anita@wealthpro.com"
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

          <div className="grid grid-cols-2 gap-3 mb-3">
            <div>
              <label className="label block mb-1">Username</label>
              <input
                className="input"
                type="text"
                placeholder="e.g. anita.s"
                value={username}
                onChange={(e) => {
                  setUsername(e.target.value.trim());
                  setUsernameStatus('');
                }}
                onBlur={checkUsername}
              />
              {/* show status below username field */}
              {usernameStatus === 'checking' && (
                <p className="text-xs text-text-2 mt-1">Checking...</p>
              )}
              {usernameStatus === 'taken' && (
                <p className="text-xs text-danger mt-1">✗ Username already taken</p>
              )}
              {usernameStatus === 'available' && (
                <p className="text-xs text-success mt-1">✓ Username available</p>
              )}
            </div>
            <div>
              <label className="label block mb-1">Password</label>
              <input
                className="input"
                type="password"
                placeholder="Min 8 chars, uppercase, number, symbol"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <p className="text-xs text-text-3 mt-1">Requires uppercase, lowercase, number &amp; special char</p>
            </div>
          </div>

          {error && (
            <div className="pill pill-danger block mb-3 text-center w-full">
              {error}
            </div>
          )}

          <div className="flex justify-end gap-2 mt-5">
            <Link to="/admin/users" className="btn btn-ghost">Cancel</Link>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating...' : 'Create User'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
