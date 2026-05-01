import { useState, useEffect } from 'react';
import { getAllClients } from '@/api/clients';
import { getAccountsByClientId } from '@/api/accounts';
import { getHoldingsByAccountId } from '@/api/holdings';
import { getAllSecurities } from '@/api/securities';
import { cachedFetch, parallelLimit } from '@/lib/fetchUtils';

type ConcentrationRisk = 'HIGH' | 'MEDIUM' | 'LOW';

interface TopHolding {
  symbol: string;
  value: number;
  pct: number;
}

interface TopAssetClass {
  name: string;
  pct: number;
}

interface ClientRisk {
  clientId: number;
  name: string;
  segment: string;
  totalValue: number;
  holdingsCount: number;
  topHolding: TopHolding | null;
  topAssetClass: TopAssetClass | null;
  concentrationRisk: ConcentrationRisk;
  diversification: number;
}

function getRiskPill(risk: ConcentrationRisk): string {
  if (risk === 'HIGH') return 'pill-danger';
  if (risk === 'MEDIUM') return 'pill-warn';
  return 'pill-success';
}

function getDiversificationLabel(count: number): { label: string; cls: string } {
  if (count >= 10) return { label: 'Strong', cls: 'text-success' };
  if (count >= 5) return { label: 'Moderate', cls: 'text-text-2' };
  return { label: 'Weak', cls: 'text-danger' };
}

function computeConcentration(pct: number): ConcentrationRisk {
  if (pct > 25) return 'HIGH';
  if (pct > 15) return 'MEDIUM';
  return 'LOW';
}

export default function RiskMonitor() {
  const [clientRisks, setClientRisks] = useState<ClientRisk[]>([]);
  const [loading, setLoading] = useState(true);
  const [riskFilter, setRiskFilter] = useState<'ALL' | ConcentrationRisk>('ALL');

  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    setLoading(true);
    setClientRisks([]);

    // Step 1: load clients + securities — both cached for 60 s
    const [clientsRes, secsRes] = await Promise.allSettled([
      cachedFetch('clients', getAllClients),
      cachedFetch('securities', getAllSecurities),
    ]);

    const clients: any[] =
      clientsRes.status === 'fulfilled' && Array.isArray(clientsRes.value)
        ? clientsRes.value
        : [];

    const secMap: { [id: number]: any } = {};
    if (secsRes.status === 'fulfilled' && Array.isArray(secsRes.value)) {
      for (const s of secsRes.value) {
        secMap[s.securityId] = s;
      }
    }

    if (clients.length === 0) {
      setLoading(false);
      return;
    }

    // Step 2: for each client load their accounts — capped at 6 concurrent
    const accountResults = await parallelLimit(
      clients.map((c: any) => () => getAccountsByClientId(c.clientId))
    );

    // Step 3: gather all (clientIdx, accountId) pairs that succeeded
    const holdingFetches: { clientIdx: number; accountId: number }[] = [];
    for (let i = 0; i < clients.length; i++) {
      const res = accountResults[i];
      if (res.status === 'fulfilled' && Array.isArray(res.value)) {
        for (const acc of res.value) {
          holdingFetches.push({ clientIdx: i, accountId: acc.accountId });
        }
      }
      // skip clients whose account load failed
    }

    // Step 4: load all holdings — capped at 6 concurrent
    const holdingResults = await parallelLimit(
      holdingFetches.map((f) => () => getHoldingsByAccountId(f.accountId))
    );

    // Step 5: aggregate holdings per client
    // clientHoldings[clientIdx] = array of holdings
    const clientHoldings: { [clientIdx: number]: any[] } = {};
    for (let i = 0; i < holdingFetches.length; i++) {
      const { clientIdx } = holdingFetches[i];
      const res = holdingResults[i];
      if (res.status === 'fulfilled' && Array.isArray(res.value)) {
        if (!clientHoldings[clientIdx]) clientHoldings[clientIdx] = [];
        clientHoldings[clientIdx].push(...res.value);
      }
    }

    // Step 6: compute risk metrics for each client
    const risks: ClientRisk[] = [];

    for (let i = 0; i < clients.length; i++) {
      const c = clients[i];
      const holdings = clientHoldings[i] || [];

      if (holdings.length === 0) continue; // filter out clients with no holdings

      // Compute total value and per-holding values
      let totalValue = 0;
      const holdingValues: { securityId: number; value: number }[] = [];

      for (const h of holdings) {
        const sec = secMap[h.securityId];
        const qty = Number(h.quantity) || 0;
        const price = sec?.currentPrice ? Number(sec.currentPrice) : Number(h.avgCost) || 0;
        const value = qty * price;
        totalValue += value;
        holdingValues.push({ securityId: h.securityId, value });
      }

      // Top holding by value
      let topHolding: TopHolding | null = null;
      if (holdingValues.length > 0 && totalValue > 0) {
        const best = holdingValues.reduce((a, b) => (b.value > a.value ? b : a));
        const sec = secMap[best.securityId];
        topHolding = {
          symbol: sec ? sec.symbol : String(best.securityId),
          value: best.value,
          pct: (best.value / totalValue) * 100,
        };
      }

      // Top asset class by aggregated value
      const assetClassMap: { [name: string]: number } = {};
      for (const hv of holdingValues) {
        const sec = secMap[hv.securityId];
        const cls = sec?.assetClass || 'UNKNOWN';
        assetClassMap[cls] = (assetClassMap[cls] || 0) + hv.value;
      }
      let topAssetClass: TopAssetClass | null = null;
      if (totalValue > 0) {
        const entries = Object.entries(assetClassMap);
        if (entries.length > 0) {
          const [name, value] = entries.reduce((a, b) => (b[1] > a[1] ? b : a));
          topAssetClass = { name, pct: (value / totalValue) * 100 };
        }
      }

      const topPct = topHolding ? topHolding.pct : 0;
      const concentrationRisk = computeConcentration(topPct);
      const uniqueSecurities = new Set(holdings.map((h: any) => h.securityId)).size;

      risks.push({
        clientId: c.clientId,
        name: c.name,
        segment: c.segment,
        totalValue,
        holdingsCount: holdings.length,
        topHolding,
        topAssetClass,
        concentrationRisk,
        diversification: uniqueSecurities,
      });
    }

    // Sort: HIGH risk first, then MEDIUM, then LOW
    const riskOrder: { [k: string]: number } = { HIGH: 0, MEDIUM: 1, LOW: 2 };
    risks.sort((a, b) => riskOrder[a.concentrationRisk] - riskOrder[b.concentrationRisk]);

    setClientRisks(risks);
    setLoading(false);
  }

  // KPIs
  const highCount = clientRisks.filter((r) => r.concentrationRisk === 'HIGH').length;
  const medCount = clientRisks.filter((r) => r.concentrationRisk === 'MEDIUM').length;
  const avgDiversification =
    clientRisks.length > 0
      ? clientRisks.reduce((sum, r) => sum + r.diversification, 0) / clientRisks.length
      : 0;

  const RISK_FILTERS: Array<'ALL' | ConcentrationRisk> = ['ALL', 'HIGH', 'MEDIUM', 'LOW'];

  const displayed =
    riskFilter === 'ALL' ? clientRisks : clientRisks.filter((r) => r.concentrationRisk === riskFilter);

  function fmt(n: number) {
    return n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  return (
    <div>
      {/* Header */}
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">Portfolio Risk Monitor</h1>
          <p className="text-sm text-text-2">
            Concentration risk analysis across all client portfolios.
          </p>
        </div>
        <button onClick={loadAll} disabled={loading} className="btn btn-ghost btn-sm">
          {loading ? 'Scanning...' : '↻ Refresh'}
        </button>
      </div>

      {/* KPI cards */}
      <div className="grid grid-cols-4 gap-4 mb-5">
        <div className="panel">
          <div className="panel-b">
            <p className="label">Clients Monitored</p>
            <p className="text-3xl font-bold mono mt-1">
              {loading ? '—' : clientRisks.length}
            </p>
            <p className="text-xs text-text-3 mt-1">with active holdings</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">HIGH Risk</p>
            <p className={'text-3xl font-bold mono mt-1 ' + (highCount > 0 ? 'text-danger' : 'text-success')}>
              {loading ? '—' : highCount}
            </p>
            <p className="text-xs text-text-3 mt-1">top position &gt;25%</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">MEDIUM Risk</p>
            <p className={'text-3xl font-bold mono mt-1 ' + (medCount > 0 ? 'text-text-2' : 'text-success')}>
              {loading ? '—' : medCount}
            </p>
            <p className="text-xs text-text-3 mt-1">top position 15–25%</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Avg Diversification</p>
            <p className="text-3xl font-bold mono mt-1">
              {loading ? '—' : avgDiversification.toFixed(1)}
            </p>
            <p className="text-xs text-text-3 mt-1">unique securities held</p>
          </div>
        </div>
      </div>

      {/* Risk filter */}
      <div className="panel mb-4">
        <div className="panel-b py-3 flex gap-2 items-center">
          {RISK_FILTERS.map((f) => (
            <button
              key={f}
              onClick={() => setRiskFilter(f)}
              className={
                'px-3 py-1.5 text-xs font-medium rounded border ' +
                (riskFilter === f
                  ? 'bg-primary text-white border-primary'
                  : 'bg-white text-text-2 border-border')
              }
            >
              {f}
              {f !== 'ALL' && !loading && (
                <span className="ml-1 opacity-70">
                  ({clientRisks.filter((r) => r.concentrationRisk === f).length})
                </span>
              )}
            </button>
          ))}
          <span className="ml-auto text-xs text-text-2">{displayed.length} clients shown</span>
        </div>
      </div>

      {/* Table */}
      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-12">
            <p className="text-base font-medium mb-1">Analysing portfolios...</p>
            <p className="text-xs text-text-3">
              This may take a moment as we scan all client portfolios.
            </p>
          </div>
        ) : clientRisks.length === 0 ? (
          <div className="panel-b text-center py-10">
            <p className="font-semibold">No portfolio data available yet.</p>
            <p className="text-sm text-text-2 mt-1">
              Client holdings will appear here once accounts are funded and orders are allocated.
            </p>
          </div>
        ) : displayed.length === 0 ? (
          <div className="panel-b text-center py-10 text-text-2">
            No clients match the selected risk level.
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Client</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Segment</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Portfolio Value</th>
                <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">Holdings</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Top Position</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Top Asset Class</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Concentration Risk</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Diversification</th>
              </tr>
            </thead>
            <tbody>
              {displayed.map((r) => {
                const { label: divLabel, cls: divCls } = getDiversificationLabel(r.diversification);
                return (
                  <tr key={r.clientId} className="border-t border-border-hairline hover:bg-surface">
                    <td className="px-5 py-3">
                      <p className="font-medium">{r.name}</p>
                      <p className="text-xs text-text-3 mono">{r.clientId}</p>
                    </td>
                    <td className="px-5 py-3 text-text-2">{r.segment}</td>
                    <td className="px-5 py-3 mono text-right font-medium">
                      {r.totalValue > 0 ? '₹' + fmt(r.totalValue) : <span className="text-text-3">—</span>}
                    </td>
                    <td className="px-5 py-3 mono text-right">{r.holdingsCount}</td>
                    <td className="px-5 py-3">
                      {r.topHolding ? (
                        <div>
                          <p className="font-medium mono">{r.topHolding.symbol}</p>
                          <p className={'text-xs ' + (r.topHolding.pct > 25 ? 'text-danger' : r.topHolding.pct > 15 ? 'text-text-2' : 'text-text-3')}>
                            {r.topHolding.pct.toFixed(1)}%
                          </p>
                        </div>
                      ) : (
                        <span className="text-text-3">—</span>
                      )}
                    </td>
                    <td className="px-5 py-3">
                      {r.topAssetClass ? (
                        <div>
                          <p className="text-xs font-medium">{r.topAssetClass.name}</p>
                          <p className="text-xs text-text-3">{r.topAssetClass.pct.toFixed(1)}%</p>
                        </div>
                      ) : (
                        <span className="text-text-3">—</span>
                      )}
                    </td>
                    <td className="px-5 py-3">
                      <span className={'pill ' + getRiskPill(r.concentrationRisk)}>
                        {r.concentrationRisk}
                      </span>
                    </td>
                    <td className="px-5 py-3">
                      <p className={'font-semibold ' + divCls}>{divLabel}</p>
                      <p className="text-xs text-text-3">{r.diversification} securities</p>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
