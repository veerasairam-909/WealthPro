import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllSecurities } from '@/api/securities';

const ASSET_CLASSES = ['ALL', 'EQUITY', 'MUTUAL_FUND', 'ETF', 'BOND', 'STRUCTURED'];

export default function Products() {
  const [securities, setSecurities] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [assetFilter, setAssetFilter] = useState('ALL');
  const [serviceDown, setServiceDown] = useState(false);

  useEffect(() => {
    loadSecurities();
  }, []);

  async function loadSecurities() {
    setLoading(true);
    setServiceDown(false);
    try {
      const data = await getAllSecurities();
      if (!Array.isArray(data)) {
        setSecurities([]);
        setServiceDown(true);
      } else {
        setSecurities(data);
      }
    } catch (e) {
      setServiceDown(true);
    }
    setLoading(false);
  }

  const filtered = securities.filter((s) => {
    if (s.status !== 'ACTIVE') return false;
    if (assetFilter !== 'ALL' && s.assetClass !== assetFilter) return false;
    if (search) {
      const q = search.toLowerCase();
      if (
        !s.symbol.toLowerCase().includes(q) &&
        !(s.name || '').toLowerCase().includes(q)
      ) return false;
    }
    return true;
  });

  function getAssetClassPill(ac: string): string {
    if (ac === 'EQUITY')     return 'pill-info';
    if (ac === 'MUTUAL_FUND') return 'pill-success';
    if (ac === 'ETF')        return 'pill-success';
    if (ac === 'BOND')       return 'pill-warn';
    if (ac === 'STRUCTURED') return 'pill-danger';
    return 'pill-info';
  }

  return (
    <div>
      {/* Page header */}
      <div className="mb-5">
        <h1 className="text-2xl font-semibold mb-1">Product Catalog</h1>
        <p className="text-sm text-text-2">
          Browse available securities. To invest, your Relationship Manager will create a
          personalised recommendation for you based on your risk profile and goals.
        </p>
      </div>

      {/* Advisory flow notice */}
      <div className="bg-primary-soft border border-primary/20 rounded-lg p-4 mb-5 flex items-start gap-3">
        <div className="text-xl shrink-0 mt-0.5">💡</div>
        <div className="flex-1">
          <p className="font-semibold text-primary text-sm mb-1">
            How to invest in these products
          </p>
          <p className="text-sm text-text-2">
            This page is for browsing only. Your Relationship Manager studies your risk profile
            and goals, then sends you a personalised recommendation with the right securities
            for you. You can then review and accept the proposal to place orders.
          </p>
        </div>
        <Link
          to="/me/recommendations"
          className="btn btn-primary btn-sm shrink-0"
        >
          My Recommendations →
        </Link>
      </div>

      {/* Filter bar */}
      <div className="panel mb-4">
        <div className="panel-b py-3">
          <div className="flex gap-3 items-center flex-wrap">
            <input
              className="input max-w-xs"
              type="text"
              placeholder="Search symbol or name..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            <div className="flex items-center gap-2">
              <span className="text-xs text-text-2 font-medium">Asset class:</span>
              <select
                className="input py-1.5"
                value={assetFilter}
                onChange={(e) => setAssetFilter(e.target.value)}
              >
                {ASSET_CLASSES.map((a) => (
                  <option key={a} value={a}>{a.replace('_', ' ')}</option>
                ))}
              </select>
            </div>
            <span className="ml-auto text-xs text-text-2">
              Showing {filtered.length} of {securities.length} securities
            </span>
          </div>
        </div>
      </div>

      {/* Service down notice */}
      {serviceDown && (
        <div className="panel mb-4">
          <div className="panel-b text-center py-8">
            <p className="text-3xl mb-2">⚠️</p>
            <p className="font-semibold">Product catalog is unavailable</p>
            <p className="text-sm text-text-2 mt-1">
              The product catalog service is not running. Please contact your administrator.
            </p>
          </div>
        </div>
      )}

      {/* Securities table — read only, no Buy button */}
      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading products...</div>
        ) : filtered.length === 0 ? (
          <div className="panel-b text-center text-text-2 py-10">
            {serviceDown ? 'Service offline' : 'No products match your filter'}
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Symbol</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Name</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Asset Class</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Price (₹)</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Currency</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Country</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((s: any) => (
                <tr
                  key={s.securityId}
                  className="border-t border-border-hairline hover:bg-surface transition-colors"
                >
                  <td className="px-5 py-3 font-semibold mono">{s.symbol}</td>
                  <td className="px-5 py-3 text-text-2 max-w-[200px] truncate">
                    {s.name || '—'}
                  </td>
                  <td className="px-5 py-3">
                    <span className={'pill ' + getAssetClassPill(s.assetClass)}>
                      {s.assetClass?.replace('_', ' ')}
                    </span>
                  </td>
                  <td className="px-5 py-3 mono text-right font-medium">
                    {s.currentPrice
                      ? '₹' + Number(s.currentPrice).toLocaleString('en-IN', {
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 2,
                        })
                      : <span className="text-text-3 text-xs">NAV</span>
                    }
                  </td>
                  <td className="px-5 py-3 text-text-2">{s.currency || '—'}</td>
                  <td className="px-5 py-3 text-text-2">{s.country || '—'}</td>
                  <td className="px-5 py-3">
                    <span className="pill pill-success">ACTIVE</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {/* Footer hint */}
        {!loading && filtered.length > 0 && (
          <div className="panel-b border-t border-border-hairline text-center py-3">
            <p className="text-xs text-text-3">
              Interested in any of these products?{' '}
              <Link to="/me/recommendations" className="text-primary hover:underline font-medium">
                Check your RM's recommendations →
              </Link>
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
