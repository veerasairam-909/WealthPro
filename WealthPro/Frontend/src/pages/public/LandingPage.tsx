import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

// ── Ticker data (mirrors our seeded securities) ───────────────────────────────
const TICKERS = [
  { symbol: 'HDFCBANK',  price: '₹1,648.75', change: '+1.24%', up: true  },
  { symbol: 'RELIANCE',  price: '₹2,923.40', change: '+0.82%', up: true  },
  { symbol: 'INFY',      price: '₹1,587.20', change: '-0.31%', up: false },
  { symbol: 'TATASTEEL', price: '₹142.35',   change: '+2.14%', up: true  },
  { symbol: 'NIFTYBEES', price: '₹237.80',   change: '+0.53%', up: true  },
  { symbol: 'ICICIBOND', price: '₹1,023.50', change: '-0.09%', up: false },
  { symbol: 'LTFIN2027', price: '₹998.00',   change: '+0.15%', up: true  },
  { symbol: 'NIFTY 50',  price: '23,721.45', change: '+0.58%', up: true  },
  { symbol: 'SENSEX',    price: '78,245.31', change: '+0.67%', up: true  },
];

const FEATURES = [
  {
    icon: '📊',
    title: 'Real-Time Portfolio Tracking',
    desc: 'Monitor holdings, P&L, and unrealised gains across equities, debt, ETFs, and mutual funds in one unified dashboard.',
  },
  {
    icon: '🤖',
    title: 'RM-Powered Recommendations',
    desc: 'Receive personalised investment proposals from your certified relationship manager, tailored to your risk profile and goals.',
  },
  {
    icon: '🎯',
    title: 'Goal-Based Investing',
    desc: 'Define retirement, education, or wealth targets. Track progress with live portfolio mapping and milestone notifications.',
  },
  {
    icon: '🔒',
    title: 'Bank-Grade Security',
    desc: 'Full KYC verification, role-based access control, and a complete compliance audit trail keep your data safe.',
  },
  {
    icon: '⚡',
    title: 'Seamless Order Execution',
    desc: 'Place, route, fill, and allocate orders through a structured dealer workflow with real-time status updates.',
  },
  {
    icon: '📋',
    title: 'Multi-Role Platform',
    desc: 'Purpose-built screens for clients, relationship managers, advisors, dealers, and compliance — one regulated platform.',
  },
];

const STEPS = [
  { n: '01', title: 'Create & Onboard',  desc: 'Register with your RM, complete KYC verification, and get your investment account activated.' },
  { n: '02', title: 'Set Your Goals',    desc: 'Define retirement, education, or wealth targets with your time horizon and priority.' },
  { n: '03', title: 'Get RM Advice',     desc: 'Your relationship manager analyses your risk profile and sends personalised investment recommendations.' },
  { n: '04', title: 'Grow Your Wealth',  desc: 'Execute orders, track performance, and watch your portfolio move toward your financial goals.' },
];

const TESTIMONIALS = [
  {
    initials: 'AR',
    name: 'Amit Rawal',
    role: 'HNI Client · Mumbai',
    stars: 5,
    quote: '"WealthPro gave me complete visibility over my portfolio. The goal tracking feature has transformed how I think about investing."',
  },
  {
    initials: 'PS',
    name: 'Priya Sharma',
    role: 'Retail Investor · Bangalore',
    stars: 5,
    quote: `"My RM's recommendations are delivered directly in the app. I can approve, review, and track everything — no more WhatsApp forwards."`,
  },
  {
    initials: 'RK',
    name: 'Rajesh Kumar',
    role: 'Relationship Manager',
    stars: 5,
    quote: '"The platform lets me manage 50+ clients efficiently. KYC, risk profiles, goals, and order routing — all in one place."',
  },
];

// ── SVG Portfolio Chart (static — no draw animation) ─────────────────────────
function PortfolioChart() {
  const pathD = 'M 0 90 C 20 85, 40 92, 60 80 C 80 68, 100 75, 120 62 C 140 50, 160 58, 180 44 C 200 32, 220 40, 240 28 C 260 18, 280 22, 300 10';
  const areaD = pathD + ' L 300 110 L 0 110 Z';

  return (
    <svg viewBox="0 0 300 110" className="w-full h-full" preserveAspectRatio="none">
      <defs>
        <linearGradient id="chartGrad" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#387ED1" stopOpacity="0.15" />
          <stop offset="100%" stopColor="#387ED1" stopOpacity="0" />
        </linearGradient>
        <linearGradient id="lineGrad" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0%" stopColor="#387ED1" />
          <stop offset="100%" stopColor="#00B386" />
        </linearGradient>
      </defs>
      {[27, 54, 81].map((y) => (
        <line key={y} x1="0" y1={y} x2="300" y2={y} stroke="#E5E7EB" strokeWidth="1" />
      ))}
      <path d={areaD} fill="url(#chartGrad)" />
      <path d={pathD} fill="none" stroke="url(#lineGrad)" strokeWidth="2.5" strokeLinecap="round" />
      <circle cx="300" cy="10" r="4" fill="#00B386" />
    </svg>
  );
}

// ── Static stat item (no counting animation) ──────────────────────────────────
function StatItem({ value, suffix, label }: { value: number; suffix: string; label: string }) {
  return (
    <div style={{ textAlign: 'center' }}>
      <p style={{ fontSize: 26, fontWeight: 800, color: '#1A1F36', lineHeight: 1 }}>
        {value.toLocaleString('en-IN')}{suffix}
      </p>
      <p style={{ fontSize: 12, color: '#5C6B82', marginTop: 4 }}>{label}</p>
    </div>
  );
}

// ── Main component ────────────────────────────────────────────────────────────
export default function LandingPage() {
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    function onScroll() { setScrolled(window.scrollY > 20); }
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  function scrollTo(id: string) {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
  }

  return (
    <div style={{ fontFamily: 'Inter, system-ui, sans-serif', background: '#FFFFFF', color: '#1A1F36' }}>
      {/* ── Base styles (no animations) ── */}
      <style>{`
        .nav-link-light {
          font-size: 13px;
          font-weight: 500;
          color: #5C6B82;
          cursor: pointer;
          background: none;
          border: none;
        }
        .nav-link-light:hover { color: #387ED1; }
      `}</style>

      {/* ══════════════════════════════════ NAVBAR ══════════════════════════════
          Matches the app: white background, primary-blue accents, Inter font    */}
      <header style={{
        position: 'fixed', top: 0, left: 0, right: 0, zIndex: 100,
        background: '#FFFFFF',
        borderBottom: scrolled ? '1px solid #E5E7EB' : '1px solid transparent',
        boxShadow: scrolled ? '0 1px 12px rgba(26,31,54,0.06)' : 'none',
      }}>
        <div style={{ maxWidth: 1200, margin: '0 auto', padding: '0 24px', height: 60, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          {/* Logo — same W mark used throughout the app */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div style={{
              width: 34, height: 34, borderRadius: 8,
              background: 'linear-gradient(135deg, #387ED1, #00B386)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: 16, fontWeight: 700, color: 'white',
            }}>W</div>
            <span style={{ fontSize: 17, fontWeight: 700, color: '#1A1F36', letterSpacing: '-0.3px' }}>WealthPro</span>
          </div>

          {/* Nav links */}
          <nav style={{ display: 'flex', alignItems: 'center', gap: 32 }}>
            {[['features', 'Features'], ['how-it-works', 'How It Works'], ['testimonials', 'Testimonials']].map(([id, label]) => (
              <button key={id} className="nav-link-light" onClick={() => scrollTo(id)}>{label}</button>
            ))}
          </nav>

          {/* Auth buttons — matches app's btn styles */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <Link to="/login" style={{
              padding: '7px 16px', borderRadius: 8, fontSize: 13, fontWeight: 500,
              color: '#5C6B82', border: '1px solid #E5E7EB', textDecoration: 'none',
              background: 'transparent',
            }}
              onMouseEnter={e => (e.currentTarget.style.background = '#FAFBFC')}
              onMouseLeave={e => (e.currentTarget.style.background = 'transparent')}
            >
              Log in
            </Link>
            <Link to="/login" style={{
              padding: '7px 16px', borderRadius: 8, fontSize: 13, fontWeight: 600,
              background: '#387ED1', color: 'white', textDecoration: 'none',
            }}
              onMouseEnter={e => (e.currentTarget.style.background = '#2C68B0')}
              onMouseLeave={e => (e.currentTarget.style.background = '#387ED1')}
            >
              Get started →
            </Link>
          </div>
        </div>
      </header>

      {/* ══════════════════════════════ LIVE TICKER ═════════════════════════════
          Dark strip provides contrast + signals live market data                */}
      <div style={{
        background: '#1A1F36', height: 36, marginTop: 60,
        display: 'flex', alignItems: 'center',
        borderBottom: '1px solid rgba(255,255,255,0.06)',
        padding: '0 16px',
        boxSizing: 'border-box',
      }}>
        {/* LIVE indicator pinned to the left */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginRight: 16, flexShrink: 0 }}>
          <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#00B386', display: 'inline-block' }} />
          <span style={{ fontSize: 10, color: 'rgba(255,255,255,0.4)', fontWeight: 700, letterSpacing: 1 }}>LIVE</span>
        </div>
        {/* Tickers spread evenly across remaining width */}
        <div style={{
          flex: 1,
          display: 'flex', alignItems: 'center',
          justifyContent: 'space-between',
          height: '100%',
          overflow: 'hidden',
        }}>
          {TICKERS.map((t, i) => (
            <div key={t.symbol} style={{
              display: 'flex', alignItems: 'center', gap: 6,
              padding: '0 10px', whiteSpace: 'nowrap', height: '100%',
              borderLeft: i > 0 ? '1px solid rgba(255,255,255,0.07)' : 'none',
            }}>
              <span style={{ fontSize: 11, fontWeight: 600, color: 'rgba(255,255,255,0.8)', fontFamily: '"IBM Plex Mono", monospace' }}>
                {t.symbol}
              </span>
              <span style={{ fontSize: 11, color: 'rgba(255,255,255,0.45)', fontFamily: '"IBM Plex Mono", monospace' }}>
                {t.price}
              </span>
              <span style={{ fontSize: 11, fontWeight: 600, color: t.up ? '#00B386' : '#EB5B3C', fontFamily: '"IBM Plex Mono", monospace' }}>
                {t.up ? '▲' : '▼'} {t.change}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* ═══════════════════════════════════ HERO ═══════════════════════════════
          Light background matching the app's white/surface palette             */}
      <section style={{
        background: 'linear-gradient(150deg, #FFFFFF 0%, #EAF2FB 45%, #F0FAF6 100%)',
        minHeight: '86vh', display: 'flex', alignItems: 'center',
        padding: '72px 24px', position: 'relative', overflow: 'hidden',
      }}>
        {/* Subtle grid pattern overlay */}
        <div style={{
          position: 'absolute', inset: 0,
          backgroundImage: 'radial-gradient(circle, #E5E7EB 1px, transparent 1px)',
          backgroundSize: '32px 32px', opacity: 0.4, pointerEvents: 'none',
        }} />

        <div style={{ maxWidth: 1200, margin: '0 auto', width: '100%', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 72, alignItems: 'center', position: 'relative' }}>
          {/* Left: copy */}
          <div >
            {/* Badge */}
            <div style={{
              display: 'inline-flex', alignItems: 'center', gap: 8,
              background: '#EAF2FB', border: '1px solid #BDD6F5',
              borderRadius: 20, padding: '5px 14px', marginBottom: 28,
            }}>
              <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#00B386', display: 'inline-block' }} />
              <span style={{ fontSize: 12, color: '#387ED1', fontWeight: 600 }}>SEBI Registered Wealth Platform</span>
            </div>

            <h1 style={{
              fontSize: 50, fontWeight: 800, lineHeight: 1.1,
              color: '#1A1F36', letterSpacing: '-1px', marginBottom: 20,
            }}>
              Grow Your<br />
              <span style={{
                background: 'linear-gradient(90deg, #387ED1, #00B386)',
                WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
              }}>
                Wealth
              </span>{' '}Smarter
            </h1>

            <p style={{ fontSize: 15, color: '#5C6B82', lineHeight: 1.75, marginBottom: 36, maxWidth: 440 }}>
              WealthPro unifies portfolio tracking, goal planning, expert RM advisory, and seamless order execution — all in one regulated platform.
            </p>

            {/* CTAs — match app btn-primary + btn-ghost style */}
            <div style={{ display: 'flex', gap: 12, marginBottom: 52 }}>
              <Link to="/login" style={{
                padding: '12px 26px', borderRadius: 8, fontSize: 14, fontWeight: 600,
                background: '#387ED1', color: 'white', textDecoration: 'none',
                boxShadow: '0 4px 16px rgba(56,126,209,0.3)',
                }}
                onMouseEnter={e => (e.currentTarget.style.background = '#2C68B0')}
                onMouseLeave={e => (e.currentTarget.style.background = '#387ED1')}
              >
                Start investing →
              </Link>
              <button
                onClick={() => scrollTo('how-it-works')}
                style={{
                  padding: '12px 22px', borderRadius: 8, fontSize: 14, fontWeight: 500,
                  background: 'transparent', color: '#5C6B82',
                  border: '1px solid #E5E7EB', cursor: 'pointer',
                    }}
                onMouseEnter={e => (e.currentTarget.style.background = '#FAFBFC')}
                onMouseLeave={e => (e.currentTarget.style.background = 'transparent')}
              >
                How it works
              </button>
            </div>

            {/* Stats — dark text on light bg */}
            <div style={{ display: 'flex', gap: 40, paddingTop: 24, borderTop: '1px solid #E5E7EB' }}>
              <StatItem value={2400} suffix="Cr+" label="AUM Managed" />
              <div style={{ width: 1, background: '#E5E7EB' }} />
              <StatItem value={12000} suffix="+" label="Active Clients" />
              <div style={{ width: 1, background: '#E5E7EB' }} />
              <StatItem value={99} suffix=".9%" label="Uptime SLA" />
            </div>
          </div>

          {/* Right: portfolio card — matches app's panel style exactly */}
          <div  style={{ display: 'flex', justifyContent: 'center' }}>
            <div style={{
              background: '#FFFFFF',
              border: '1px solid #E5E7EB',
              borderRadius: 16,
              padding: 24,
              width: '100%', maxWidth: 380,
              boxShadow: '0 8px 32px rgba(56,126,209,0.1)',
            }}>
              {/* Card header */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 20 }}>
                <div>
                  <p style={{ fontSize: 11, color: '#98A2B3', fontWeight: 600, letterSpacing: 0.5, marginBottom: 4 }}>
                    TOTAL PORTFOLIO VALUE
                  </p>
                  <p style={{ fontSize: 28, fontWeight: 800, color: '#1A1F36', fontFamily: '"IBM Plex Mono", monospace' }}>
                    ₹24,63,450
                  </p>
                  <p style={{ fontSize: 12, color: '#00B386', fontWeight: 600, marginTop: 2 }}>
                    ▲ +18.4% all time
                  </p>
                </div>
                <div style={{
                  background: '#E0F7EF', borderRadius: 8, padding: '4px 10px',
                }}>
                  <span style={{ fontSize: 11, color: '#00B386', fontWeight: 600 }}>LIVE</span>
                </div>
              </div>

              {/* SVG Chart */}
              <div style={{ height: 110, marginBottom: 16 }}>
                <PortfolioChart />
              </div>

              {/* Mini holdings list */}
              <div style={{ borderTop: '1px solid #EEF1F4', paddingTop: 14, display: 'flex', flexDirection: 'column', gap: 12 }}>
                {[
                  { sym: 'HDFCBANK',  qty: '100 shares', val: '₹1,64,875', pct: '+12.4%', up: true  },
                  { sym: 'RELIANCE',  qty: '50 shares',  val: '₹1,46,170', pct: '+7.8%',  up: true  },
                  { sym: 'NIFTYBEES', qty: '500 units',  val: '₹1,18,900', pct: '-2.1%',  up: false },
                ].map((h) => (
                  <div key={h.sym} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                      <div style={{
                        width: 32, height: 32, borderRadius: 8,
                        background: '#EAF2FB',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        fontSize: 10, fontWeight: 700, color: '#387ED1', fontFamily: '"IBM Plex Mono", monospace',
                      }}>
                        {h.sym.slice(0, 2)}
                      </div>
                      <div>
                        <p style={{ fontSize: 12, fontWeight: 600, color: '#1A1F36', fontFamily: '"IBM Plex Mono", monospace' }}>{h.sym}</p>
                        <p style={{ fontSize: 11, color: '#98A2B3' }}>{h.qty}</p>
                      </div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <p style={{ fontSize: 12, fontWeight: 600, color: '#1A1F36', fontFamily: '"IBM Plex Mono", monospace' }}>{h.val}</p>
                      <p style={{ fontSize: 11, fontWeight: 600, color: h.up ? '#00B386' : '#EB5B3C' }}>{h.pct}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ══════════════════════════════ FEATURES ════════════════════════════════ */}
      <section id="features" style={{ padding: '96px 24px', background: '#FAFBFC' }}>
        <div style={{ maxWidth: 1200, margin: '0 auto' }}>
          <div style={{ textAlign: 'center', marginBottom: 56 }}>
            <span style={{ display: 'inline-block', fontSize: 12, fontWeight: 700, color: '#387ED1', letterSpacing: 1.5, marginBottom: 12, textTransform: 'uppercase' }}>
              Platform Features
            </span>
            <h2 style={{ fontSize: 36, fontWeight: 800, color: '#1A1F36', letterSpacing: '-0.5px', marginBottom: 14 }}>
              Everything you need to invest smarter
            </h2>
            <p style={{ fontSize: 14, color: '#5C6B82', maxWidth: 520, margin: '0 auto', lineHeight: 1.75 }}>
              A full-stack wealth management platform built for every participant in the investment journey.
            </p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 20 }}>
            {FEATURES.map((f, i) => (
              <div key={i}  style={{
                background: '#FFFFFF', border: '1px solid #E5E7EB',
                borderRadius: 12, padding: 26,
              }}>
                <div style={{
                  width: 48, height: 48, borderRadius: 12,
                  background: '#EAF2FB', display: 'flex', alignItems: 'center',
                  justifyContent: 'center', fontSize: 22, marginBottom: 16,
                }}>
                  {f.icon}
                </div>
                <h3 style={{ fontSize: 14, fontWeight: 700, color: '#1A1F36', marginBottom: 8 }}>{f.title}</h3>
                <p style={{ fontSize: 13, color: '#5C6B82', lineHeight: 1.65 }}>{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ════════════════════════════ HOW IT WORKS ══════════════════════════════ */}
      <section id="how-it-works" style={{ padding: '96px 24px', background: '#FFFFFF' }}>
        <div style={{ maxWidth: 1200, margin: '0 auto' }}>
          <div style={{ textAlign: 'center', marginBottom: 64 }}>
            <span style={{ display: 'inline-block', fontSize: 12, fontWeight: 700, color: '#387ED1', letterSpacing: 1.5, marginBottom: 12, textTransform: 'uppercase' }}>
              Process
            </span>
            <h2 style={{ fontSize: 36, fontWeight: 800, color: '#1A1F36', letterSpacing: '-0.5px', marginBottom: 14 }}>
              From onboarding to growth in 4 steps
            </h2>
            <p style={{ fontSize: 14, color: '#5C6B82', maxWidth: 460, margin: '0 auto', lineHeight: 1.75 }}>
              A structured, regulated journey designed to protect and grow your wealth.
            </p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', position: 'relative' }}>
            {/* Connecting line */}
            <div style={{
              position: 'absolute', top: 26, left: '12.5%', right: '12.5%',
              height: 2, background: 'linear-gradient(90deg, #387ED1, #00B386)', opacity: 0.2,
            }} />

            {STEPS.map((s, i) => (
              <div key={i}  style={{ textAlign: 'center', padding: '0 20px', cursor: 'default' }}>
                <div  style={{
                  width: 52, height: 52, borderRadius: '50%',
                  background: '#EAF2FB', color: '#387ED1',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  margin: '0 auto 20px', fontSize: 13, fontWeight: 800,
                  border: '2px solid #BDD6F5', position: 'relative', zIndex: 1,
                }}>
                  {s.n}
                </div>
                <h3 style={{ fontSize: 14, fontWeight: 700, color: '#1A1F36', marginBottom: 10 }}>{s.title}</h3>
                <p style={{ fontSize: 13, color: '#5C6B82', lineHeight: 1.65 }}>{s.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ══════════════════════════ TESTIMONIALS ════════════════════════════════ */}
      <section id="testimonials" style={{ padding: '96px 24px', background: '#FAFBFC' }}>
        <div style={{ maxWidth: 1200, margin: '0 auto' }}>
          <div style={{ textAlign: 'center', marginBottom: 56 }}>
            <span style={{ display: 'inline-block', fontSize: 12, fontWeight: 700, color: '#387ED1', letterSpacing: 1.5, marginBottom: 12, textTransform: 'uppercase' }}>
              Testimonials
            </span>
            <h2 style={{ fontSize: 36, fontWeight: 800, color: '#1A1F36', letterSpacing: '-0.5px', marginBottom: 14 }}>
              Trusted by clients and advisors
            </h2>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 20 }}>
            {TESTIMONIALS.map((t, i) => (
              <div key={i}  style={{
                background: '#FFFFFF', border: '1px solid #E5E7EB',
                borderRadius: 12, padding: 26,
              }}>
                {/* Stars */}
                <div style={{ display: 'flex', gap: 3, marginBottom: 14 }}>
                  {Array.from({ length: t.stars }).map((_, j) => (
                    <span key={j} style={{ color: '#F4A41E', fontSize: 14 }}>★</span>
                  ))}
                </div>
                <p style={{ fontSize: 13, color: '#5C6B82', lineHeight: 1.7, marginBottom: 20, fontStyle: 'italic' }}>
                  {t.quote}
                </p>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12, paddingTop: 16, borderTop: '1px solid #EEF1F4' }}>
                  <div style={{
                    width: 40, height: 40, borderRadius: '50%',
                    background: 'linear-gradient(135deg, #387ED1, #00B386)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 13, fontWeight: 700, color: 'white', flexShrink: 0,
                  }}>
                    {t.initials}
                  </div>
                  <div>
                    <p style={{ fontSize: 13, fontWeight: 700, color: '#1A1F36' }}>{t.name}</p>
                    <p style={{ fontSize: 11, color: '#98A2B3' }}>{t.role}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ══════════════════════════════════ CTA ═════════════════════════════════
          Uses app's primary blue — consistent with btn-primary colour           */}
      <section style={{
        padding: '96px 24px',
        background: 'linear-gradient(135deg, #2C68B0 0%, #387ED1 50%, #3A8FD4 100%)',
        position: 'relative', overflow: 'hidden',
      }}>
        {/* Subtle dot pattern */}
        <div style={{
          position: 'absolute', inset: 0,
          backgroundImage: 'radial-gradient(circle, rgba(255,255,255,0.08) 1px, transparent 1px)',
          backgroundSize: '24px 24px', pointerEvents: 'none',
        }} />
        <div style={{ maxWidth: 680, margin: '0 auto', textAlign: 'center', position: 'relative' }}>
          <p style={{ fontSize: 12, fontWeight: 700, color: 'rgba(255,255,255,0.7)', letterSpacing: 1.5, textTransform: 'uppercase', marginBottom: 16 }}>
            Start Today
          </p>
          <h2 style={{ fontSize: 40, fontWeight: 800, color: '#FFFFFF', letterSpacing: '-0.5px', lineHeight: 1.15, marginBottom: 18 }}>
            Your wealth journey<br />starts with one click
          </h2>
          <p style={{ fontSize: 14, color: 'rgba(255,255,255,0.75)', lineHeight: 1.75, marginBottom: 36, maxWidth: 440, margin: '0 auto 36px' }}>
            Join thousands of investors already using WealthPro to track, grow, and protect their financial future.
          </p>
          <div style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
            <Link to="/login" style={{
              padding: '13px 28px', borderRadius: 8, fontSize: 14, fontWeight: 700,
              background: '#FFFFFF', color: '#387ED1', textDecoration: 'none',
              boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
            }}
              onMouseEnter={e => (e.currentTarget.style.boxShadow = '0 6px 28px rgba(0,0,0,0.22)')}
              onMouseLeave={e => (e.currentTarget.style.boxShadow = '0 4px 20px rgba(0,0,0,0.15)')}
            >
              Open your account →
            </Link>
            <button
              onClick={() => scrollTo('features')}
              style={{
                padding: '13px 22px', borderRadius: 8, fontSize: 14, fontWeight: 500,
                background: 'rgba(255,255,255,0.12)', color: 'rgba(255,255,255,0.9)',
                border: '1px solid rgba(255,255,255,0.25)', cursor: 'pointer',
                }}
              onMouseEnter={e => (e.currentTarget.style.background = 'rgba(255,255,255,0.2)')}
              onMouseLeave={e => (e.currentTarget.style.background = 'rgba(255,255,255,0.12)')}
            >
              Explore features
            </button>
          </div>

          {/* Trust badges */}
          <div style={{ display: 'flex', gap: 28, justifyContent: 'center', marginTop: 44, flexWrap: 'wrap' }}>
            {['🔒 SEBI Registered', '🛡 KYC Compliant', '⚡ 99.9% Uptime', '📋 Full Audit Trail'].map((b) => (
              <span key={b} style={{ fontSize: 12, color: 'rgba(255,255,255,0.65)', fontWeight: 500 }}>{b}</span>
            ))}
          </div>
        </div>
      </section>

      {/* ══════════════════════════════ FOOTER ══════════════════════════════════
          Dark footer using the app's text-dark (#1A1F36) as base              */}
      <footer style={{ background: '#1A1F36', padding: '64px 24px 28px' }}>
        <div style={{ maxWidth: 1200, margin: '0 auto' }}>
          <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr 1fr', gap: 48, marginBottom: 48 }}>
            {/* Brand */}
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 14 }}>
                <div style={{
                  width: 34, height: 34, borderRadius: 8,
                  background: 'linear-gradient(135deg, #387ED1, #00B386)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 16, fontWeight: 700, color: 'white',
                }}>W</div>
                <span style={{ fontSize: 17, fontWeight: 700, color: 'white' }}>WealthPro</span>
              </div>
              <p style={{ fontSize: 13, color: 'rgba(255,255,255,0.45)', lineHeight: 1.75, maxWidth: 250, marginBottom: 18 }}>
                A SEBI-compliant wealth management platform connecting clients, advisors, and dealers in one regulated ecosystem.
              </p>
              <div style={{ display: 'flex', gap: 8 }}>
                {['SEBI Reg.', '256-bit SSL'].map((b) => (
                  <span key={b} style={{
                    fontSize: 10, padding: '3px 8px', borderRadius: 4,
                    border: '1px solid rgba(255,255,255,0.12)',
                    color: 'rgba(255,255,255,0.35)', fontWeight: 600,
                  }}>🔒 {b}</span>
                ))}
              </div>
            </div>

            {/* Product */}
            <div>
              <p style={{ fontSize: 11, fontWeight: 700, color: 'rgba(255,255,255,0.6)', letterSpacing: 1.2, textTransform: 'uppercase', marginBottom: 16 }}>Product</p>
              {['Portfolio Tracking', 'Goal Planning', 'Order Management', 'Advisory Tools', 'Compliance'].map((l) => (
                <p key={l} style={{ fontSize: 13, color: 'rgba(255,255,255,0.4)', marginBottom: 10, cursor: 'default' }}>{l}</p>
              ))}
            </div>

            {/* Roles */}
            <div>
              <p style={{ fontSize: 11, fontWeight: 700, color: 'rgba(255,255,255,0.6)', letterSpacing: 1.2, textTransform: 'uppercase', marginBottom: 16 }}>Roles</p>
              {['Client', 'Relationship Manager', 'Advisor', 'Dealer', 'Compliance', 'Admin'].map((l) => (
                <p key={l} style={{ fontSize: 13, color: 'rgba(255,255,255,0.4)', marginBottom: 10, cursor: 'default' }}>{l}</p>
              ))}
            </div>

            {/* Support */}
            <div>
              <p style={{ fontSize: 11, fontWeight: 700, color: 'rgba(255,255,255,0.6)', letterSpacing: 1.2, textTransform: 'uppercase', marginBottom: 16 }}>Support</p>
              {['Documentation', 'KYC Guide', 'Order Help', 'Contact RM', 'Raise a Ticket'].map((l) => (
                <p key={l} style={{ fontSize: 13, color: 'rgba(255,255,255,0.4)', marginBottom: 10, cursor: 'default' }}>{l}</p>
              ))}
            </div>
          </div>

          {/* Bottom bar */}
          <div style={{
            borderTop: '1px solid rgba(255,255,255,0.08)', paddingTop: 22,
            display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12,
          }}>
            <p style={{ fontSize: 12, color: 'rgba(255,255,255,0.3)' }}>
              © 2026 WealthPro. All rights reserved. SEBI Registration No. INP000XXXXX
            </p>
            <div style={{ display: 'flex', gap: 24 }}>
              {['Privacy Policy', 'Terms of Service', 'Disclosures'].map((l) => (
                <span key={l} style={{ fontSize: 12, color: 'rgba(255,255,255,0.3)', cursor: 'default' }}>{l}</span>
              ))}
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}
