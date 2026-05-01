import { useState } from 'react';
import { useNavigate, Navigate, Link } from 'react-router-dom';
import { login } from '@/api/auth';
import { useAuth, getHomeForRole } from '@/auth/store';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);

  const setToken = useAuth((s) => s.setToken);
  const user     = useAuth((s) => s.user);
  const navigate = useNavigate();

  if (user) return <Navigate to={getHomeForRole(user.role)} />;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const trimmedUsername = username.trim();
    if (!trimmedUsername || !password) { setError('Please enter username and password'); return; }
    setLoading(true);
    setError('');
    try {
      const token = await login(trimmedUsername, password);
      setToken(token);
      const newUser = useAuth.getState().user;
      if (newUser) navigate(getHomeForRole(newUser.role));
    } catch (err: any) {
      setError(
        err.response?.status === 401
          ? 'Invalid username or password'
          : 'Login failed. Please try again.'
      );
    }
    setLoading(false);
  }

  return (
    <div style={{ minHeight: '100vh', background: '#F4F6F8', display: 'flex', flexDirection: 'column' }}>

      {/* ── Top bar with logo + back link ────────────────────────────────────── */}
      <header style={{
        background: '#FFFFFF',
        borderBottom: '1px solid #E5E7EB',
        height: 56,
        display: 'flex', alignItems: 'center',
        padding: '0 24px',
        justifyContent: 'space-between',
      }}>
        {/* Logo — links back to landing page */}
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: 10, textDecoration: 'none' }}>
          <div style={{
            width: 32, height: 32, borderRadius: 8,
            background: 'linear-gradient(135deg, #387ED1, #00B386)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 15, fontWeight: 700, color: 'white',
          }}>W</div>
          <span style={{ fontSize: 16, fontWeight: 700, color: '#1A1F36', letterSpacing: '-0.3px' }}>
            WealthPro
          </span>
        </Link>

        {/* Back to home */}
        <Link to="/" style={{
          display: 'flex', alignItems: 'center', gap: 6,
          fontSize: 13, color: '#5C6B82', textDecoration: 'none',
          fontWeight: 500, transition: 'color 0.2s',
        }}
          onMouseEnter={e => (e.currentTarget.style.color = '#387ED1')}
          onMouseLeave={e => (e.currentTarget.style.color = '#5C6B82')}
        >
          ← Back to home
        </Link>
      </header>

      {/* ── Main content ─────────────────────────────────────────────────────── */}
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24 }}>
        <div style={{
          width: '100%', maxWidth: 820,
          background: '#FFFFFF',
          border: '1px solid #E5E7EB',
          borderRadius: 16,
          overflow: 'hidden',
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          boxShadow: '0 4px 24px rgba(26,31,54,0.08)',
        }}>

          {/* ── Left panel: brand ── */}
          <div style={{
            background: 'linear-gradient(150deg, #2C68B0 0%, #387ED1 60%, #3A8FD4 100%)',
            padding: '48px 40px',
            display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'flex-start',
          }}>
            <h2 style={{
              fontSize: 26, fontWeight: 800, color: '#FFFFFF',
              lineHeight: 1.25, marginBottom: 14, letterSpacing: '-0.4px',
            }}>
              Manage your wealth with confidence.
            </h2>
            <p style={{ fontSize: 13, color: 'rgba(255,255,255,0.7)', lineHeight: 1.75, marginBottom: 32 }}>
              Track portfolios, plan goals, and execute orders — all in one SEBI-registered platform.
            </p>

            {/* Trust badges */}
            <div style={{ paddingTop: 24, borderTop: '1px solid rgba(255,255,255,0.15)', width: '100%' }}>
              <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                {['SEBI Registered', 'KYC Compliant', '99.9% Uptime'].map((b) => (
                  <span key={b} style={{
                    fontSize: 10, fontWeight: 600, padding: '3px 8px', borderRadius: 4,
                    background: 'rgba(255,255,255,0.1)',
                    border: '1px solid rgba(255,255,255,0.18)',
                    color: 'rgba(255,255,255,0.7)',
                  }}>
                    {b}
                  </span>
                ))}
              </div>
            </div>
          </div>

          {/* ── Right panel: form ── */}
          <div style={{ padding: '48px 40px', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
            <h3 style={{ fontSize: 22, fontWeight: 700, color: '#1A1F36', marginBottom: 6 }}>
              Welcome back
            </h3>
            <p style={{ fontSize: 13, color: '#5C6B82', marginBottom: 28 }}>
              Sign in to access your dashboard
            </p>

            <form onSubmit={handleSubmit}>
              <div style={{ marginBottom: 16 }}>
                <label style={{ display: 'block', fontSize: 11, fontWeight: 600, color: '#5C6B82', marginBottom: 6 }}>
                  USERNAME
                </label>
                <input
                  className="input"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Enter your username"
                  autoFocus
                  style={{ fontSize: 13 }}
                />
              </div>

              <div style={{ marginBottom: 20 }}>
                <label style={{ display: 'block', fontSize: 11, fontWeight: 600, color: '#5C6B82', marginBottom: 6 }}>
                  PASSWORD
                </label>
                <input
                  className="input"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  style={{ fontSize: 13 }}
                />
              </div>

              {error && (
                <div style={{
                  background: '#FCEAE5', border: '1px solid #F5C5BB',
                  borderRadius: 8, padding: '9px 14px',
                  fontSize: 13, color: '#EB5B3C', marginBottom: 16,
                  display: 'flex', alignItems: 'center', gap: 8,
                }}>
                  <span>⚠</span> {error}
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                style={{
                  width: '100%', padding: '11px 0',
                  borderRadius: 8, fontSize: 14, fontWeight: 600,
                  background: loading ? '#BDD6F5' : '#387ED1',
                  color: 'white', border: 'none', cursor: loading ? 'not-allowed' : 'pointer',
                  transition: 'background 0.2s',
                }}
                onMouseEnter={e => { if (!loading) e.currentTarget.style.background = '#2C68B0'; }}
                onMouseLeave={e => { if (!loading) e.currentTarget.style.background = '#387ED1'; }}
              >
                {loading ? 'Signing in...' : 'Sign in →'}
              </button>
            </form>

            {/* Contact RM note */}
            <p style={{ fontSize: 12, color: '#98A2B3', textAlign: 'center', marginTop: 20 }}>
              Don't have an account?{' '}
              <span style={{ color: '#387ED1', fontWeight: 500 }}>Contact your relationship manager.</span>
            </p>

          </div>
        </div>
      </div>
    </div>
  );
}
