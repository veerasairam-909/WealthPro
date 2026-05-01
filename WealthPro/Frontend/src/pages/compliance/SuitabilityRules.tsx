import { useState, useEffect } from 'react';

// ─── Types ────────────────────────────────────────────────────────────────────

type Severity = 'HIGH' | 'MEDIUM' | 'LOW';

interface Rule {
  id: number;
  name: string;
  description: string;
  severity: Severity;
  category: string;
  triggerCondition: string;
}

// ─── Static rules data ────────────────────────────────────────────────────────

const RULES: Rule[] = [
  {
    id: 1,
    name: 'KYC Verification Required',
    description: 'All active clients must have at least one verified KYC document on record.',
    severity: 'HIGH',
    category: 'KYC',
    triggerCondition: 'Client is Active AND no Verified KYC document exists',
  },
  {
    id: 2,
    name: 'KYC Pending Review',
    description: 'KYC documents uploaded but not yet verified should be resolved within 7 days.',
    severity: 'HIGH',
    category: 'KYC',
    triggerCondition: 'Client has KYC documents but none with Verified status',
  },
  {
    id: 3,
    name: 'Risk Profile Mandatory',
    description: 'All active clients must have a completed risk profile assessment.',
    severity: 'MEDIUM',
    category: 'Risk Profiling',
    triggerCondition: 'Client is Active AND no risk profile record exists',
  },
  {
    id: 4,
    name: 'Inactive Account Monitoring',
    description: 'Inactive accounts must be periodically reviewed for data retention compliance.',
    severity: 'LOW',
    category: 'Account Maintenance',
    triggerCondition: 'Client status is Inactive',
  },
  {
    id: 5,
    name: 'PENDING_KYC Status Block',
    description: 'Clients in PENDING_KYC status must complete KYC before any orders can be placed.',
    severity: 'HIGH',
    category: 'KYC',
    triggerCondition: 'Client status is PENDING_KYC',
  },
  {
    id: 6,
    name: 'Suitability Assessment',
    description: "Investment recommendations must match the client's risk class and suitability profile.",
    severity: 'HIGH',
    category: 'Suitability',
    triggerCondition: 'Recommendation risk class does not match client risk profile',
  },
  {
    id: 7,
    name: 'Concentration Risk',
    description: "No single security should exceed 25% of a client's total portfolio value.",
    severity: 'MEDIUM',
    category: 'Risk',
    triggerCondition: 'Single security weight > 25% of portfolio',
  },
  {
    id: 8,
    name: 'Pre-Trade Compliance',
    description: 'All orders must pass pre-trade checks before being routed to market.',
    severity: 'HIGH',
    category: 'Trading',
    triggerCondition: 'Order placed without passing pre-trade checks',
  },
];

const STORAGE_KEY = 'wp_suitability_rules_disabled';

// ─── Helpers ─────────────────────────────────────────────────────────────────

function severityPill(severity: Severity): string {
  if (severity === 'HIGH') return 'pill-danger';
  if (severity === 'MEDIUM') return 'pill-warn';
  return 'pill-info';
}

function loadDisabled(): Set<number> {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return new Set();
    const parsed = JSON.parse(raw);
    if (Array.isArray(parsed)) return new Set<number>(parsed);
  } catch {
    // ignore corrupt storage
  }
  return new Set();
}

function saveDisabled(ids: Set<number>) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify([...ids]));
}

// Derive unique categories from rules
const ALL_CATEGORIES = ['ALL', ...Array.from(new Set(RULES.map((r) => r.category)))];

// ─── Component ────────────────────────────────────────────────────────────────

export default function SuitabilityRules() {
  const [disabledIds, setDisabledIds] = useState<Set<number>>(() => loadDisabled());
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');

  // Persist whenever disabledIds changes
  useEffect(() => {
    saveDisabled(disabledIds);
  }, [disabledIds]);

  function toggleRule(id: number) {
    setDisabledIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  }

  // ── Derived counts ──────────────────────────────────────────────────────────

  const totalCount = RULES.length;
  const disabledCount = disabledIds.size;
  const activeCount = totalCount - disabledCount;

  // Count by category (active rules only)
  const categoryCounts = RULES.reduce<Record<string, number>>((acc, r) => {
    if (!disabledIds.has(r.id)) {
      acc[r.category] = (acc[r.category] ?? 0) + 1;
    }
    return acc;
  }, {});

  const displayed = RULES.filter((r) =>
    categoryFilter === 'ALL' ? true : r.category === categoryFilter,
  );

  // ── Render ──────────────────────────────────────────────────────────────────

  return (
    <div>
      {/* Header */}
      <div className="mb-5">
        <h1 className="text-2xl font-semibold mb-1">Suitability & Compliance Rules</h1>
        <p className="text-sm text-text-2">
          Compliance rules that drive breach detection. Toggle rules on or off — your preferences
          are stored locally in this browser.
        </p>
      </div>

      {/* KPI cards */}
      <div className="grid grid-cols-4 gap-4 mb-5">
        <div className="panel">
          <div className="panel-b">
            <p className="label">Total Rules</p>
            <p className="text-3xl font-bold mt-1">{totalCount}</p>
            <p className="text-xs text-text-3 mt-1">in the rule engine</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Active</p>
            <p className="text-3xl font-bold mt-1 text-success">{activeCount}</p>
            <p className="text-xs text-text-3 mt-1">rules enforced</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">Disabled</p>
            <p className={'text-3xl font-bold mt-1 ' + (disabledCount > 0 ? 'text-warn' : 'text-text-2')}>
              {disabledCount}
            </p>
            <p className="text-xs text-text-3 mt-1">bypassed rules</p>
          </div>
        </div>
        <div className="panel">
          <div className="panel-b">
            <p className="label">By Category</p>
            <div className="mt-2 space-y-0.5">
              {Object.entries(categoryCounts).slice(0, 4).map(([cat, count]) => (
                <div key={cat} className="flex justify-between text-xs">
                  <span className="text-text-2 truncate mr-2">{cat}</span>
                  <span className="font-medium mono">{count}</span>
                </div>
              ))}
              {Object.keys(categoryCounts).length === 0 && (
                <p className="text-xs text-text-3 italic">All rules disabled</p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Category filter */}
      <div className="panel mb-4">
        <div className="panel-b py-3 flex gap-2 items-center flex-wrap">
          {ALL_CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => setCategoryFilter(cat)}
              className={
                'px-3 py-1.5 text-xs font-medium rounded border ' +
                (categoryFilter === cat
                  ? 'bg-primary text-white border-primary'
                  : 'bg-white text-text-2 border-border')
              }
            >
              {cat}
              {cat !== 'ALL' && (
                <span className="ml-1 opacity-70">
                  ({RULES.filter((r) => r.category === cat).length})
                </span>
              )}
              {cat === 'ALL' && <span className="ml-1 opacity-70">({totalCount})</span>}
            </button>
          ))}
          <span className="ml-auto text-xs text-text-2">
            {displayed.length} rule{displayed.length !== 1 ? 's' : ''} shown
          </span>
        </div>
      </div>

      {/* Rules table */}
      <div className="panel">
        {disabledCount > 0 && (
          <div className="panel-b py-2 px-5 border-b border-border-hairline bg-warn/5">
            <p className="text-xs text-warn font-medium">
              Warning: {disabledCount} rule{disabledCount !== 1 ? 's are' : ' is'} currently disabled. Breach detection may be incomplete.
            </p>
          </div>
        )}

        <table className="w-full text-sm">
          <thead className="bg-surface">
            <tr>
              <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2 w-6">
                #
              </th>
              <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">
                Rule Name
              </th>
              <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">
                Category
              </th>
              <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">
                Severity
              </th>
              <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">
                Trigger Condition
              </th>
              <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">
                Status
              </th>
              <th className="text-right px-5 py-3 text-xs uppercase font-medium text-text-2">
                Toggle
              </th>
            </tr>
          </thead>
          <tbody>
            {displayed.map((rule) => {
              const isDisabled = disabledIds.has(rule.id);
              return (
                <tr
                  key={rule.id}
                  className={
                    'border-t border-border-hairline ' +
                    (isDisabled ? 'opacity-50' : '')
                  }
                >
                  <td className="px-5 py-4 mono text-xs text-text-3">{rule.id}</td>
                  <td className="px-5 py-4 max-w-[220px]">
                    <p className="font-medium">{rule.name}</p>
                    <p className="text-xs text-text-2 mt-0.5 leading-relaxed">{rule.description}</p>
                  </td>
                  <td className="px-5 py-4 text-text-2 text-xs">{rule.category}</td>
                  <td className="px-5 py-4">
                    <span className={'pill ' + severityPill(rule.severity)}>{rule.severity}</span>
                  </td>
                  <td className="px-5 py-4 text-xs text-text-2 max-w-[260px]">
                    <span className="mono bg-surface px-2 py-1 rounded border border-border text-text-2 block leading-relaxed">
                      {rule.triggerCondition}
                    </span>
                  </td>
                  <td className="px-5 py-4">
                    {isDisabled ? (
                      <span className="pill pill-danger">Inactive</span>
                    ) : (
                      <span className="pill pill-success">Active</span>
                    )}
                  </td>
                  <td className="px-5 py-4 text-right">
                    {isDisabled ? (
                      <button
                        onClick={() => toggleRule(rule.id)}
                        className="btn btn-success btn-sm"
                      >
                        Enable
                      </button>
                    ) : (
                      <button
                        onClick={() => toggleRule(rule.id)}
                        className="btn btn-ghost btn-sm"
                      >
                        Disable
                      </button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>

        {displayed.length === 0 && (
          <div className="panel-b text-center text-text-2 py-10">
            No rules in this category.
          </div>
        )}
      </div>

      {/* Footer note */}
      <p className="text-xs text-text-3 mt-3 text-center">
        Rule enable/disable state is stored in your browser (localStorage) and does not affect
        the server-side rule engine.
      </p>
    </div>
  );
}
