import { useState, useEffect } from 'react';
import {
  getAllSecurities,
  createSecurity,
  updateSecurity,
  deleteSecurity,
} from '@/api/securities';
import { TableSkeleton } from '@/components/Skeleton';
import EmptyState from '@/components/EmptyState';
import { ShieldCheck } from 'lucide-react';

const ASSET_CLASSES = ['EQUITY', 'BOND', 'MUTUAL_FUND', 'ETF', 'STRUCTURED'];
const STATUSES      = ['ACTIVE', 'SUSPENDED', 'INACTIVE'];

const ASSET_PILL: Record<string, string> = {
  EQUITY:      'pill-info',
  BOND:        'pill-warn',
  MUTUAL_FUND: 'pill-success',
  ETF:         'pill-info',
  STRUCTURED:  'pill-danger',
};

export default function AdminSecurities() {
  const [securities, setSecurities] = useState<any[]>([]);
  const [loading, setLoading]       = useState(true);
  const [globalError, setGlobalError] = useState('');

  // modal
  const [showModal, setShowModal]   = useState(false);
  const [editingId, setEditingId]   = useState<number | null>(null);
  const [saving, setSaving]         = useState(false);
  const [formError, setFormError]   = useState('');

  // fields
  const [symbol, setSymbol]           = useState('');
  const [assetClass, setAssetClass]   = useState('EQUITY');
  const [currency, setCurrency]       = useState('INR');
  const [country, setCountry]         = useState('India');
  const [status, setStatus]           = useState('ACTIVE');
  const [currentPrice, setCurrentPrice] = useState('');

  // price adjustment
  const [updatingId, setUpdatingId]   = useState<number | null>(null);
  const [priceFlash, setPriceFlash]   = useState<{ id: number; dir: 'up' | 'down' } | null>(null);

  // search
  const [search, setSearch]           = useState('');
  const [assetFilter, setAssetFilter] = useState('ALL');

  useEffect(() => { load(); }, []);

  async function load() {
    setLoading(true);
    try {
      const data = await getAllSecurities();
      if (Array.isArray(data)) setSecurities(data);
    } catch { /* service may be down */ }
    setLoading(false);
  }

  // ── price adjustment ───────────────────────────────────────────────────────
  async function adjustPrice(s: any, pct: number) {
    if (s.currentPrice == null) return;
    const newPrice = Math.round(s.currentPrice * (1 + pct / 100) * 100) / 100;
    if (newPrice <= 0) return;

    setUpdatingId(s.securityId);
    try {
      await updateSecurity(s.securityId, {
        symbol: s.symbol,
        name: s.name ?? null,
        exchange: s.exchange ?? null,
        isin: s.isin ?? null,
        assetClass: s.assetClass,
        currency: s.currency,
        country: s.country,
        status: s.status,
        currentPrice: newPrice,
      });
      // Optimistic update — no full reload needed
      setSecurities((prev) =>
        prev.map((sec) =>
          sec.securityId === s.securityId ? { ...sec, currentPrice: newPrice } : sec
        )
      );
      setPriceFlash({ id: s.securityId, dir: pct > 0 ? 'up' : 'down' });
      setTimeout(() => setPriceFlash(null), 1500);
    } catch {
      setGlobalError('Price update failed. Please try again.');
      setTimeout(() => setGlobalError(''), 4000);
    }
    setUpdatingId(null);
  }

  // ── filtered view ──────────────────────────────────────────────────────────
  const filtered = securities.filter((s) => {
    if (assetFilter !== 'ALL' && s.assetClass !== assetFilter) return false;
    if (search) {
      const q = search.toLowerCase();
      if (!s.symbol.toLowerCase().includes(q) && !String(s.securityId).includes(q)) return false;
    }
    return true;
  });

  // ── modal helpers ──────────────────────────────────────────────────────────
  function openCreate() {
    setEditingId(null);
    setSymbol('');
    setAssetClass('EQUITY');
    setCurrency('INR');
    setCountry('India');
    setStatus('ACTIVE');
    setCurrentPrice('');
    setFormError('');
    setShowModal(true);
  }

  function openEdit(s: any) {
    setEditingId(s.securityId);
    setSymbol(s.symbol || '');
    setAssetClass(s.assetClass || 'EQUITY');
    setCurrency(s.currency || 'INR');
    setCountry(s.country || 'India');
    setStatus(s.status || 'ACTIVE');
    setCurrentPrice(s.currentPrice != null ? String(s.currentPrice) : '');
    setFormError('');
    setShowModal(true);
  }

  function closeModal() { setShowModal(false); setFormError(''); }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const symbolValue = symbol.trim().toUpperCase();
    if (!symbolValue) { setFormError('Symbol is required'); return; }
    if (!/^[A-Z0-9&._-]+$/.test(symbolValue)) {
      setFormError('Symbol can only contain letters, numbers and the characters & . _ -');
      return;
    }
    if (symbolValue.length > 20) {
      setFormError('Symbol must be 20 characters or fewer');
      return;
    }

    let price: number | null = null;
    if (currentPrice.trim() !== '') {
      price = parseFloat(currentPrice);
      if (isNaN(price) || price <= 0) { setFormError('Price must be greater than zero'); return; }
    }

    setSaving(true);
    setFormError('');
    try {
      const payload = {
        symbol: symbol.trim().toUpperCase(),
        assetClass,
        currency: currency.trim() || 'INR',
        country: country.trim() || 'India',
        status,
        currentPrice: price,
      };
      if (editingId !== null) {
        await updateSecurity(editingId, payload);
      } else {
        await createSecurity(payload);
      }
      closeModal();
      load();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.response?.data || 'Save failed';
      setFormError(typeof msg === 'string' ? msg : JSON.stringify(msg));
    }
    setSaving(false);
  }

  async function handleDelete(s: any) {
    if (!confirm(`Delete security "${s.symbol}" (ID ${s.securityId})? This cannot be undone.`)) return;
    setGlobalError('');
    try {
      await deleteSecurity(s.securityId);
      load();
    } catch {
      setGlobalError('Delete failed — security may be referenced by orders or holdings.');
      setTimeout(() => setGlobalError(''), 4000);
    }
  }

  // ── counts ─────────────────────────────────────────────────────────────────
  const activeCount    = securities.filter((s) => s.status === 'ACTIVE').length;
  const suspendedCount = securities.filter((s) => s.status === 'SUSPENDED').length;

  return (
    <div>
      {/* header */}
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Securities Master</h1>
          <p className="text-sm text-text-2">
            Manage the product catalog — equities, bonds, mutual funds, ETFs and structured products.
          </p>
        </div>
        <button onClick={openCreate} className="btn btn-primary btn-sm">+ Add Security</button>
      </div>

      {globalError && (
        <div className="pill pill-danger block text-center mb-4 py-2">{globalError}</div>
      )}

      {/* KPI strip */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-5">
        <div className="panel"><div className="panel-b py-3 text-center">
          <p className="label">Total</p>
          <p className="text-2xl font-bold mono mt-1">{securities.length}</p>
        </div></div>
        <div className="panel"><div className="panel-b py-3 text-center">
          <p className="label">Active</p>
          <p className="text-2xl font-bold mono mt-1 text-success">{activeCount}</p>
        </div></div>
        <div className="panel"><div className="panel-b py-3 text-center">
          <p className="label">Suspended</p>
          <p className="text-2xl font-bold mono mt-1 text-danger">{suspendedCount}</p>
        </div></div>
        <div className="panel"><div className="panel-b py-3 text-center">
          <p className="label">Asset Classes</p>
          <p className="text-2xl font-bold mono mt-1">{new Set(securities.map((s) => s.assetClass)).size}</p>
        </div></div>
      </div>

      {/* filters */}
      <div className="panel mb-4">
        <div className="panel-b py-3 flex gap-3 items-center flex-wrap">
          <input
            className="input max-w-xs"
            type="text"
            placeholder="Search by symbol or ID..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <div className="flex gap-1 flex-wrap ml-auto">
            {['ALL', ...ASSET_CLASSES].map((ac) => (
              <button
                key={ac}
                onClick={() => setAssetFilter(ac)}
                className={
                  'px-3 py-1.5 text-xs font-medium rounded border ' +
                  (assetFilter === ac
                    ? 'bg-primary text-white border-primary'
                    : 'bg-white text-text-2 border-border')
                }
              >
                {ac}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* table */}
      <div className="panel">
        {loading ? (
          <TableSkeleton rows={6} cols={7} />
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<ShieldCheck size={26} />}
            title="No securities found"
            description="Add a security to populate the product catalog."
            action={<button onClick={openCreate} className="btn btn-primary btn-sm">+ Add Security</button>}
          />
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">ID</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Symbol</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Asset Class</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Currency</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Country</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Market Price</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((s: any) => (
                <tr key={s.securityId} className="border-t border-border-hairline hover:bg-surface">
                  <td className="px-5 py-3 mono text-xs text-text-2">{s.securityId}</td>
                  <td className="px-5 py-3 font-semibold mono">{s.symbol}</td>
                  <td className="px-5 py-3">
                    <span className={'pill ' + (ASSET_PILL[s.assetClass] || 'pill-info')}>
                      {s.assetClass}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-text-2">{s.currency}</td>
                  <td className="px-5 py-3 text-text-2">{s.country}</td>
                  <td className="px-5 py-3 text-right">
                    {s.currentPrice != null ? (
                      <div>
                        <span className={
                          'mono font-medium transition-colors duration-500 ' +
                          (priceFlash?.id === s.securityId
                            ? priceFlash.dir === 'up' ? 'text-success' : 'text-danger'
                            : '')
                        }>
                          ₹{Number(s.currentPrice).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                        </span>
                        <div className="flex gap-1 justify-end mt-1.5">
                          {[-5, -1, 1, 5].map((pct) => (
                            <button
                              key={pct}
                              onClick={() => adjustPrice(s, pct)}
                              disabled={updatingId === s.securityId}
                              title={`${pct > 0 ? '+' : ''}${pct}% → ₹${(Math.round(s.currentPrice * (1 + pct / 100) * 100) / 100).toFixed(2)}`}
                              className={
                                'text-xs px-1.5 py-0.5 rounded border font-mono disabled:opacity-40 ' +
                                (pct < 0
                                  ? 'text-danger border-danger/40 hover:bg-danger/10'
                                  : 'text-success border-success/40 hover:bg-success/10')
                              }
                            >
                              {pct > 0 ? '+' : ''}{pct}%
                            </button>
                          ))}
                        </div>
                      </div>
                    ) : (
                      <span className="text-text-3 text-xs">— no price set</span>
                    )}
                  </td>
                  <td className="px-5 py-3">
                    <span className={'pill ' + (s.status === 'ACTIVE' ? 'pill-success' : 'pill-danger')}>
                      {s.status}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-right">
                    <div className="flex gap-2 justify-end">
                      <button onClick={() => openEdit(s)} className="btn btn-ghost btn-sm">Edit</button>
                      <button onClick={() => handleDelete(s)} className="btn btn-danger btn-sm">Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* ── Create / Edit modal ── */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="px-6 py-4 border-b border-border-hairline flex items-center justify-between">
              <h3 className="font-semibold">{editingId !== null ? 'Edit Security' : 'Add Security'}</h3>
              <button onClick={closeModal} className="text-text-3 text-xl leading-none">×</button>
            </div>

            <form onSubmit={handleSubmit} className="p-6">
              {/* Symbol */}
              <div className="mb-3">
                <label className="label block mb-1">Symbol <span className="text-danger">*</span></label>
                <input
                  className="input mono"
                  type="text"
                  placeholder="e.g. HDFCBANK"
                  maxLength={20}
                  value={symbol}
                  onChange={(e) => setSymbol(e.target.value)}
                  autoFocus
                />
              </div>

              {/* Asset Class */}
              <div className="mb-3">
                <label className="label block mb-1">Asset Class</label>
                <select className="input" value={assetClass} onChange={(e) => setAssetClass(e.target.value)}>
                  {ASSET_CLASSES.map((ac) => <option key={ac} value={ac}>{ac}</option>)}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3 mb-3">
                <div>
                  <label className="label block mb-1">Currency</label>
                  <input className="input mono" type="text" placeholder="INR" value={currency} onChange={(e) => setCurrency(e.target.value)} />
                </div>
                <div>
                  <label className="label block mb-1">Country</label>
                  <input className="input" type="text" placeholder="India" value={country} onChange={(e) => setCountry(e.target.value)} />
                </div>
              </div>

              {/* Current Price */}
              <div className="mb-3">
                <label className="label block mb-1">
                  Market Price (₹) <span className="text-text-3 font-normal">— optional</span>
                </label>
                <input
                  className="input mono"
                  type="number"
                  step="0.01"
                  min="0.01"
                  placeholder="Leave blank for suspended securities"
                  value={currentPrice}
                  onChange={(e) => setCurrentPrice(e.target.value)}
                />
              </div>

              {/* Status */}
              <div className="mb-4">
                <label className="label block mb-1">Status</label>
                <div className="flex gap-2">
                  {STATUSES.map((st) => (
                    <button
                      key={st}
                      type="button"
                      onClick={() => setStatus(st)}
                      className={
                        'flex-1 py-2 text-sm rounded border font-medium ' +
                        (status === st
                          ? 'bg-primary text-white border-primary'
                          : 'bg-white text-text border-border')
                      }
                    >
                      {st}
                    </button>
                  ))}
                </div>
              </div>

              {formError && (
                <div className="pill pill-danger block text-center w-full mb-3">{formError}</div>
              )}

              <div className="flex justify-end gap-2">
                <button type="button" onClick={closeModal} className="btn btn-ghost">Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saving...' : editingId !== null ? 'Save Changes' : 'Add Security'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
