import { useState, useEffect } from 'react';
import { getAllSecurities } from '@/api/securities';

export default function Securities() {
  const [securities, setSecurities] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [assetFilter, setAssetFilter] = useState('ALL');

  useEffect(() => { loadSecurities(); }, []);

  async function loadSecurities() {
    setLoading(true);
    try {
      const data = await getAllSecurities();
      if (Array.isArray(data)) setSecurities(data);
    } catch (e) {
    }
    setLoading(false);
  }

  // Derive asset class list from data
  const assetClasses = ['ALL', ...Array.from(new Set(securities.map((s) => s.assetClass).filter(Boolean)))];

  const filtered = securities.filter((s) => {
    const q = search.toLowerCase();
    const matchSearch =
      !search ||
      (s.symbol || '').toLowerCase().includes(q) ||
      (s.name || '').toLowerCase().includes(q) ||
      (s.isin || '').toLowerCase().includes(q);
    const matchAsset = assetFilter === 'ALL' || s.assetClass === assetFilter;
    return matchSearch && matchAsset;
  });

  function getAssetPill(ac: string) {
    if (ac === 'EQUITY') return 'pill-info';
    if (ac === 'DEBT') return 'pill-warn';
    if (ac === 'ETF') return 'pill-success';
    if (ac === 'MUTUAL_FUND') return 'pill-info';
    return 'pill-info';
  }

  function fmtPrice(price: any) {
    if (price == null) return <span className="text-text-3">NAV</span>;
    return (
      <span className="mono font-medium">
        ₹{Number(price).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
      </span>
    );
  }

  const equityCount = securities.filter((s) => s.assetClass === 'EQUITY').length;
  const debtCount = securities.filter((s) => s.assetClass === 'DEBT').length;
  const mfCount = securities.filter((s) => s.assetClass === 'MUTUAL_FUND').length;
  const etfCount = securities.filter((s) => s.assetClass === 'ETF').length;

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Securities Reference</h1>
          <p className="text-sm text-text-2">
            Product catalogue with current prices — use as reference when routing and filling orders.
          </p>
        </div>
        <button onClick={loadSecurities} disabled={loading} className="btn btn-ghost btn-sm">
          {loading ? 'Loading...' : '↻ Refresh'}
        </button>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-4 gap-4 mb-4">
        <div className="panel">
          <div className="panel-b py-3">
            <p className="label text-xs">Equity</p>
            <p className="text-xl font-semibold mt-0.5">{equityCount}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b py-3">
            <p className="label text-xs">Debt</p>
            <p className="text-xl font-semibold mt-0.5">{debtCount}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b py-3">
            <p className="label text-xs">Mutual Funds</p>
            <p className="text-xl font-semibold mt-0.5">{mfCount}</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b py-3">
            <p className="label text-xs">ETF</p>
            <p className="text-xl font-semibold mt-0.5">{etfCount}</p>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="panel mb-4">
        <div className="panel-b py-3 flex gap-2 items-center flex-wrap">
          <input
            className="input max-w-xs"
            type="text"
            placeholder="Search symbol, name, or ISIN..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <div className="flex gap-1 flex-wrap">
            {assetClasses.map((ac) => (
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
          <span className="ml-auto text-xs text-text-2">{filtered.length} securities</span>
        </div>
      </div>

      {/* Table */}
      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading securities...</div>
        ) : filtered.length === 0 ? (
          <div className="panel-b text-center py-8">
            <p className="text-3xl mb-2">🔍</p>
            <p className="font-semibold">No securities found</p>
            <p className="text-sm text-text-2 mt-1">Try adjusting your search or filter.</p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Symbol</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Name</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Asset class</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Exchange</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">ISIN</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Price / NAV</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((s: any) => (
                <tr key={s.securityId} className="border-t border-border-hairline hover:bg-surface/50">
                  <td className="px-5 py-3">
                    <span className="font-semibold mono">{s.symbol}</span>
                  </td>
                  <td className="px-5 py-3 text-text-2 max-w-xs truncate" title={s.name}>
                    {s.name}
                  </td>
                  <td className="px-5 py-3">
                    <span className={'pill ' + getAssetPill(s.assetClass)}>{s.assetClass}</span>
                  </td>
                  <td className="px-5 py-3 text-text-2 text-xs">{s.exchange || '—'}</td>
                  <td className="px-5 py-3 mono text-xs text-text-2">{s.isin || '—'}</td>
                  <td className="px-5 py-3 text-right">{fmtPrice(s.currentPrice)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
