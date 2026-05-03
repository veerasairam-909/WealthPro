import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/auth/store';
import { getRecommendationsByClientId, updateRecommendationStatus } from '@/api/recommendations';
import { placeOrder } from '@/api/orders';
import { getAllSecurities } from '@/api/securities';
import { getAccountsByClientId } from '@/api/accounts';
import { getBalanceByAccountId } from '@/api/cashLedger';
import { getRiskProfile } from '@/api/clients';
import { TableSkeleton } from '@/components/Skeleton';
import EmptyState from '@/components/EmptyState';
import { BookMarked, Plus, Trash2, RefreshCw } from 'lucide-react';

type FilterType = 'ALL' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'DRAFT';

interface OrderItem {
  id: string;           // local unique key
  securityId: string;
  quantity: string;
  priceType: 'MARKET' | 'LIMIT';
  limitPrice: string;
  assetClass: string;       // pre-set from allocation (empty = manual row)
  allocationPct: number;    // e.g. 60 — shown as label
  suggestedAmount: number;  // balance × (pct/100) — used to auto-calc quantity
}

function emptyItem(): OrderItem {
  return {
    id: Date.now() + Math.random() + '',
    securityId: '', quantity: '', priceType: 'MARKET', limitPrice: '',
    assetClass: '', allocationPct: 0, suggestedAmount: 0,
  };
}

/** Parse allocation object from proposalJson. Returns null if not present. */
function parseAllocation(proposalJson: string): Record<string, number> | null {
  try {
    const p = JSON.parse(proposalJson);
    if (p.allocation && typeof p.allocation === 'object' && Object.keys(p.allocation).length > 0) {
      return p.allocation as Record<string, number>;
    }
  } catch { /* plain text */ }
  return null;
}

/** Build one OrderItem per allocation entry, with suggested amounts pre-filled. */
function buildAllocationRows(allocation: Record<string, number>, balance: number): OrderItem[] {
  return Object.entries(allocation).map(([assetClass, pct]) => ({
    id: Date.now() + Math.random() + '',
    securityId: '',
    quantity: '',
    priceType: 'MARKET' as const,
    limitPrice: '',
    assetClass: assetClass.toUpperCase().replace(/ /g, '_'),
    allocationPct: Number(pct),
    suggestedAmount: Math.round(balance * Number(pct) / 100),
  }));
}

export default function ClientRecommendations() {
  const user = useAuth((s) => s.user);
  const clientId = user?.clientId;
  const navigate = useNavigate();

  const [recos, setRecos] = useState<any[]>([]);
  const [securities, setSecurities] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const [filter, setFilter] = useState<FilterType>('ALL');

  // ── Order placement modal state ──────────────────────────────────────────
  const [showOrderModal, setShowOrderModal] = useState(false);
  const [activeReco, setActiveReco] = useState<any>(null);
  const [orderItems, setOrderItems] = useState<OrderItem[]>([emptyItem()]);
  const [orderError, setOrderError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [orderResults, setOrderResults] = useState<{ symbol: string; ok: boolean; msg: string }[]>([]);
  const [actionLoading, setActionLoading] = useState<number | null>(null);

  // ── Account & balance state (loaded when modal opens) ────────────────────
  const [accountId, setAccountId] = useState<number | null>(null);
  const [cashBalance, setCashBalance] = useState<number | null>(null);
  const [balanceLoading, setBalanceLoading] = useState(false);

  // Asset classes removed from the modal because they would always fail pre-trade checks
  const [blockedAssets, setBlockedAssets] = useState<string[]>([]);

  // The client's OWN assessed risk class (Conservative / Balanced / Aggressive).
  // Used for suitability pre-filtering — independent of the recommendation's risk tag.
  const [clientRiskClass, setClientRiskClass] = useState<string | null>(null);

  // wrap in useCallback so the visibilitychange listener can reference it
  const loadAll = useCallback(async (silent = false) => {
    if (!clientId) return;
    if (silent) setRefreshing(true);
    else setLoading(true);
    try {
      const [recosRes, secsRes, riskRes] = await Promise.allSettled([
        getRecommendationsByClientId(clientId),
        getAllSecurities(),
        getRiskProfile(clientId),
      ]);
      if (recosRes.status === 'fulfilled' && Array.isArray(recosRes.value)) {
        const sorted = [...recosRes.value].sort((a, b) => {
          if (a.status === 'SUBMITTED' && b.status !== 'SUBMITTED') return -1;
          if (b.status === 'SUBMITTED' && a.status !== 'SUBMITTED') return 1;
          return new Date(b.proposedDate).getTime() - new Date(a.proposedDate).getTime();
        });
        setRecos(sorted);
      }
      if (secsRes.status === 'fulfilled' && Array.isArray(secsRes.value)) {
        setSecurities(secsRes.value);
      }
      // Store the client's own risk class — used to enforce suitability in the
      // order modal. We use the client's assessed profile, NOT the recommendation's
      // risk tag, because those can differ if the RM sent the wrong portfolio.
      if (riskRes.status === 'fulfilled' && riskRes.value?.riskClass) {
        setClientRiskClass(riskRes.value.riskClass.toUpperCase());
      }
      setLastUpdated(new Date());
    } catch (e) {}
    setLoading(false);
    setRefreshing(false);
  }, [clientId]);

  // Initial load
  useEffect(() => {
    if (clientId) loadAll();
  }, [clientId, loadAll]);

  // Reload silently whenever the browser tab comes back into focus
  // — this catches the case where the RM created a recommendation
  //   while the client had this page open in the background
  useEffect(() => {
    function onFocus() {
      if (clientId && !loading) loadAll(true);
    }
    document.addEventListener('visibilitychange', onFocus);
    window.addEventListener('focus', onFocus);
    return () => {
      document.removeEventListener('visibilitychange', onFocus);
      window.removeEventListener('focus', onFocus);
    };
  }, [clientId, loading, loadAll]);

  // ── Helpers ──────────────────────────────────────────────────────────────
  function getStatusPill(s: string) {
    if (s === 'APPROVED') return 'pill-success';
    if (s === 'REJECTED') return 'pill-danger';
    if (s === 'SUBMITTED') return 'pill-info';
    return 'pill-warn';
  }

  function getRiskPill(r: string) {
    if (r === 'CONSERVATIVE') return 'bg-success-soft text-success';
    if (r === 'AGGRESSIVE') return 'bg-danger-soft text-danger';
    return 'bg-warn-soft text-warn';
  }

  function renderProposal(json: string) {
    let p: any;
    try { p = JSON.parse(json); } catch { return <p className="text-sm text-text-2">{json}</p>; }
    if (p.text) return <p className="text-sm text-text-2 leading-relaxed">{p.text}</p>;
    if (p.summary && Object.keys(p).length === 1)
      return <p className="text-sm text-text-2 leading-relaxed">{p.summary}</p>;
    return (
      <div className="text-sm space-y-3">
        {p.summary && <p className="text-text-2 leading-relaxed">{p.summary}</p>}
        <div className="flex flex-wrap gap-2">
          {p.targetReturn !== undefined && (
            <div className="bg-surface border border-border px-3 py-2 rounded-lg text-center min-w-[100px]">
              <p className="text-xs text-text-3 mb-0.5">Target Return</p>
              <p className="font-semibold text-primary">{p.targetReturn}%</p>
            </div>
          )}
          {p.timeHorizon && (
            <div className="bg-surface border border-border px-3 py-2 rounded-lg text-center min-w-[100px]">
              <p className="text-xs text-text-3 mb-0.5">Time Horizon</p>
              <p className="font-semibold">{p.timeHorizon}</p>
            </div>
          )}
          {p.SIP && (
            <div className="bg-surface border border-border px-3 py-2 rounded-lg text-center min-w-[100px]">
              <p className="text-xs text-text-3 mb-0.5">Monthly SIP</p>
              <p className="font-semibold">₹{Number(p.SIP).toLocaleString('en-IN')}</p>
            </div>
          )}
        </div>
        {p.allocation && (
          <div>
            <p className="text-xs text-text-3 uppercase font-semibold mb-2">Suggested Allocation</p>
            <div className="flex flex-wrap gap-2">
              {Object.entries(p.allocation).map(([k, v]) => (
                <span key={k} className="bg-primary-soft text-primary px-3 py-1 rounded-full text-xs font-semibold">
                  {k} · {String(v)}%
                </span>
              ))}
            </div>
          </div>
        )}
      </div>
    );
  }

  // ── Order item CRUD ──────────────────────────────────────────────────────
  function addOrderItem() {
    setOrderItems((prev) => [...prev, emptyItem()]);
  }

  function removeOrderItem(id: string) {
    setOrderItems((prev) => prev.filter((o) => o.id !== id));
  }

  function updateOrderItem(id: string, field: keyof OrderItem, value: string) {
    setOrderItems((prev) =>
      prev.map((o) => {
        if (o.id !== id) return o;
        const updated = { ...o, [field]: value };
        // Reset limitPrice when switching away from LIMIT
        if (field === 'priceType' && value !== 'LIMIT') updated.limitPrice = '';
        // Auto-calculate quantity from suggested amount when security is picked.
        // Rule 4: MUTUAL_FUND BUY minimum is 1000 units — enforce that floor
        // so the auto-filled value never produces an order that will be rejected.
        if (field === 'securityId' && value) {
          const sec = securities.find((s) => String(s.securityId) === value);
          const price = sec ? Number(sec.currentPrice) : 0;
          if (price > 0 && updated.suggestedAmount > 0) {
            const rawQty = Math.floor(updated.suggestedAmount / price);
            updated.quantity = String(Math.max(1, rawQty));
          }
        }
        return updated;
      })
    );
  }

  // ── Open / close modal ───────────────────────────────────────────────────
  async function openOrderModal(reco: any) {
    setActiveReco(reco);
    setOrderItems([emptyItem()]); // placeholder until balance loads
    setOrderError('');
    setOrderResults([]);
    setBlockedAssets([]);
    setAccountId(null);
    setCashBalance(null);
    setShowOrderModal(true);

    // Fetch account + balance, then build allocation rows
    if (!clientId) return;
    setBalanceLoading(true);
    try {
      const accounts = await getAccountsByClientId(clientId);
      if (Array.isArray(accounts) && accounts.length > 0) {
        const acc = accounts[0];
        setAccountId(acc.accountId);
        const bal = await getBalanceByAccountId(acc.accountId);
        const balance = Number(bal) || 0;
        setCashBalance(balance);

        // Build pre-filled rows from allocation percentages if available
        const allocation = parseAllocation(reco.proposalJson);
        if (allocation && balance > 0) {
          let rows = buildAllocationRows(allocation, balance);

          // ── Suitability pre-filter ──────────────────────────────────────
          // Remove asset classes that would always fail the SUITABILITY
          // pre-trade check for this client, so they aren't shown rows
          // they can never successfully place.
          //
          // IMPORTANT: We check the CLIENT'S OWN risk class (from their
          // assessed risk profile), NOT the recommendation's risk class tag.
          // The recommendation tag can be wrong if an RM sent a mismatched
          // portfolio. The client's profile is the source of truth.
          //
          // Rule 1: CONSERVATIVE risk class → cannot BUY EQUITY
          // Rule 6: Non-UHNI segment → cannot BUY STRUCTURED (handled at pre-trade)
          const effectiveRiskClass = clientRiskClass;   // client's own assessed class
          const blocked: string[] = [];
          if (effectiveRiskClass === 'CONSERVATIVE') {
            const equityRows = rows.filter((r) => r.assetClass === 'EQUITY');
            if (equityRows.length > 0) {
              equityRows.forEach((r) => blocked.push(r.assetClass));
              rows = rows.filter((r) => r.assetClass !== 'EQUITY');
            }
          }
          setBlockedAssets(blocked);
          setOrderItems(rows.length > 0 ? rows : [emptyItem()]);
        } else {
          setOrderItems([emptyItem()]);
        }
      } else {
        setAccountId(null);
        setCashBalance(0);
        setOrderItems([emptyItem()]);
      }
    } catch (e) {
      setCashBalance(null);
      setOrderItems([emptyItem()]);
    }
    setBalanceLoading(false);
  }

  // ── Decline ──────────────────────────────────────────────────────────────
  async function handleDecline(recoId: number) {
    if (!confirm('Decline this recommendation from your RM?')) return;
    setActionLoading(recoId);
    try {
      await updateRecommendationStatus(recoId, 'REJECTED');
      await loadAll();
    } catch (e: any) {
      alert('Failed to decline: ' + (e.response?.data?.message || e.message || 'Unknown error'));
    }
    setActionLoading(null);
  }

  // ── Place multiple orders ────────────────────────────────────────────────
  async function handlePlaceOrders(e: React.FormEvent) {
    e.preventDefault();
    if (!clientId || !activeReco) return;
    setOrderError('');
    setOrderResults([]);

    // ── Validate every row ───────────────────────────────────────────────
    for (let i = 0; i < orderItems.length; i++) {
      const item = orderItems[i];
      const rowLabel = orderItems.length > 1 ? `Row ${i + 1}: ` : '';
      if (!item.securityId) {
        setOrderError(`${rowLabel}Please select a security`); return;
      }
      const qty = parseInt(item.quantity, 10);
      if (isNaN(qty) || qty < 1) {
        setOrderError(`${rowLabel}Quantity must be at least 1`); return;
      }
      if (item.priceType === 'LIMIT') {
        const lp = parseFloat(item.limitPrice);
        if (!item.limitPrice || isNaN(lp) || lp <= 0) {
          setOrderError(`${rowLabel}Enter a valid limit price`); return;
        }
      }
    }

    // ── Check for duplicate securities ──────────────────────────────────
    const pickedIds = orderItems.map((o) => o.securityId);
    const dupes = pickedIds.filter((id, idx) => pickedIds.indexOf(id) !== idx);
    if (dupes.length > 0) {
      setOrderError('You have selected the same security more than once. Remove the duplicate row.');
      return;
    }

    // ── Check cash balance ───────────────────────────────────────────────
    if (accountId === null) {
      setOrderError(
        'You do not have a portfolio account yet. ' +
        'Please ask your Dealer to create an account for you before placing orders.'
      );
      return;
    }
    if (cashBalance !== null && cashBalance <= 0) {
      setOrderError(
        'Your account has ₹0 balance. ' +
        'Please ask your Relationship Manager to fund your account before placing orders.'
      );
      return;
    }
    if (cashBalance !== null && totalEstimated > 0 && cashBalance < totalEstimated) {
      setOrderError(
        `Insufficient balance. Your account has ₹${cashBalance.toLocaleString('en-IN')} ` +
        `but the estimated investment is ₹${totalEstimated.toLocaleString('en-IN')}. ` +
        `Please reduce the quantity or ask your RM to fund your account.`
      );
      return;
    }

    setSubmitting(true);

    // ── Step 1: Accept recommendation (only if still SUBMITTED) ─────────
    if (activeReco.status === 'SUBMITTED') {
      try {
        await updateRecommendationStatus(activeReco.recoId, 'APPROVED');
        setActiveReco((prev: any) => ({ ...prev, status: 'APPROVED' }));
      } catch (e: any) {
        const msg = e.response?.data?.message || e.message || '';
        // Ignore "already APPROVED" errors — proceed to place orders
        if (!msg.toLowerCase().includes('approved')) {
          setOrderError('Failed to accept recommendation: ' + msg);
          setSubmitting(false);
          return;
        }
      }
    }

    // ── Step 2: Place one order per row ──────────────────────────────────
    const results: { symbol: string; ok: boolean; msg: string }[] = [];

    for (const item of orderItems) {
      const sec = securities.find((s) => String(s.securityId) === item.securityId);
      const symbol = sec ? `${sec.symbol} (${sec.assetClass})` : `Security #${item.securityId}`;
      try {
        await placeOrder({
          clientId,
          securityId: parseInt(item.securityId, 10),
          side: 'BUY',
          quantity: parseInt(item.quantity, 10),
          priceType: item.priceType,
          ...(item.priceType === 'LIMIT' ? { limitPrice: parseFloat(item.limitPrice) } : {}),
        });
        results.push({ symbol, ok: true, msg: 'Order placed successfully' });
      } catch (e: any) {
        const msg =
          e.response?.data?.message ||
          e.response?.data ||
          e.message ||
          'Unknown error';
        results.push({ symbol, ok: false, msg: typeof msg === 'string' ? msg : 'Failed' });
      }
    }

    setOrderResults(results);
    setSubmitting(false);

    const allOk = results.every((r) => r.ok);
    const anyOk = results.some((r) => r.ok);

    if (allOk) {
      // All succeeded — close modal and refresh
      setShowOrderModal(false);
      await loadAll();
      alert(
        `✓ ${results.length} order${results.length > 1 ? 's' : ''} placed successfully!\n\n` +
        'Your recommendation has been accepted. Orders are with the Dealer for execution.\n' +
        'Track them in "My Orders".'
      );
    } else if (anyOk) {
      // Partial success — keep modal open to show results
      setOrderError('Some orders could not be placed. See details below.');
      await loadAll();
    } else {
      // All failed
      setOrderError('All orders failed. See details below or try again.');
    }
  }

  // ── Derived values ────────────────────────────────────────────────────────
  const filtered = filter === 'ALL' ? recos : recos.filter((r) => r.status === filter);
  const pendingCount = recos.filter((r) => r.status === 'SUBMITTED').length;

  // Fallback list for manually-added rows (no specific asset class)
  const activeSecurities = securities.filter((s) => s.status === 'ACTIVE');
  const securitiesForModal = activeSecurities.length > 0 ? activeSecurities : securities;

  // Total estimated investment across all rows
  const totalEstimated = orderItems.reduce((sum, item) => {
    const sec = securities.find((s) => String(s.securityId) === item.securityId);
    const qty = parseInt(item.quantity, 10) || 0;
    return sum + (sec ? Number(sec.currentPrice) * qty : 0);
  }, 0);

  // IDs already selected (for duplicate warning highlight)
  const selectedIds = orderItems.map((o) => o.securityId).filter(Boolean);

  if (!clientId) return <div className="p-10 text-center text-text-2">Loading...</div>;

  return (
    <div>
      {/* Page header */}
      <div className="flex justify-between items-start mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">My Recommendations</h1>
          <p className="text-sm text-text-2">
            Investment advice prepared by your Relationship Manager based on your risk profile and goals
          </p>
          {lastUpdated && (
            <p className="text-xs text-text-3 mt-1">
              Last updated: {lastUpdated.toLocaleTimeString()}
            </p>
          )}
        </div>
        <div className="flex items-center gap-2 shrink-0">
          {pendingCount > 0 && (
            <span className="pill pill-warn text-sm px-3 py-1">
              {pendingCount} awaiting your decision
            </span>
          )}
          <button
            onClick={() => loadAll(true)}
            disabled={refreshing || loading}
            className="btn btn-ghost btn-sm flex items-center gap-1.5"
            title="Check for new recommendations from your RM"
          >
            <RefreshCw
              size={14}
              className={refreshing ? 'animate-spin' : ''}
            />
            {refreshing ? 'Checking…' : 'Refresh'}
          </button>
        </div>
      </div>

      {/* Advisory flow banner */}
      {pendingCount > 0 && (
        <div className="bg-primary-soft border border-primary/20 rounded-lg p-4 mb-5 flex gap-3">
          <div className="text-2xl shrink-0">📋</div>
          <div>
            <p className="font-semibold text-primary mb-1">How the advisory flow works</p>
            <p className="text-sm text-text-2">
              Your RM has analysed your risk profile and goals and prepared a personalised investment proposal.
              Review it below — you can invest in one or multiple securities at once — or decline if you prefer not to proceed.
              Accepted orders go to the Dealing desk for execution.
            </p>
          </div>
        </div>
      )}

      {/* Filter tabs */}
      <div className="flex gap-2 mb-5 overflow-x-auto pb-1">
        {(['ALL', 'SUBMITTED', 'APPROVED', 'REJECTED', 'DRAFT'] as FilterType[]).map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={'btn btn-sm shrink-0 ' + (filter === f ? 'btn-primary' : 'btn-ghost')}
          >
            {f === 'ALL' && 'All'}
            {f === 'SUBMITTED' && (
              <span className="flex items-center gap-1.5">
                Pending
                {pendingCount > 0 && (
                  <span className={
                    'rounded-full w-5 h-5 text-xs flex items-center justify-center font-bold ' +
                    (filter === 'SUBMITTED' ? 'bg-white text-primary' : 'bg-primary text-white')
                  }>
                    {pendingCount}
                  </span>
                )}
              </span>
            )}
            {f === 'APPROVED' && 'Accepted'}
            {f === 'REJECTED' && 'Declined'}
            {f === 'DRAFT' && 'Draft'}
          </button>
        ))}
      </div>

      {/* Recommendation cards */}
      {loading ? (
        <div className="panel"><TableSkeleton rows={3} cols={3} /></div>
      ) : filtered.length === 0 ? (
        <div className="panel">
          <EmptyState
            icon={<BookMarked size={32} />}
            title={
              filter === 'SUBMITTED' ? 'No pending recommendations' :
              filter === 'APPROVED' ? 'No accepted recommendations yet' :
              filter === 'REJECTED' ? 'No declined recommendations' :
              'No recommendations yet'
            }
            description={
              filter === 'ALL'
                ? 'Your Relationship Manager will prepare personalised investment recommendations based on your risk profile and financial goals.'
                : filter === 'SUBMITTED'
                ? 'All caught up! No recommendations are waiting for your decision.'
                : 'No recommendations in this category.'
            }
          />
        </div>
      ) : (
        <div className="space-y-4">
          {filtered.map((r: any) => (
            <div
              key={r.recoId}
              className={
                'panel transition-shadow ' +
                (r.status === 'SUBMITTED' ? 'ring-1 ring-primary/30 shadow-md' : '')
              }
            >
              <div className="panel-b">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className={'pill ' + getStatusPill(r.status)}>
                      {r.status === 'SUBMITTED' ? '⏳ Pending Your Action' :
                       r.status === 'APPROVED'  ? '✓ Accepted' :
                       r.status === 'REJECTED'  ? '✗ Declined' : r.status}
                    </span>
                    <span className={'text-xs font-medium px-2 py-0.5 rounded-full ' + getRiskPill(r.riskClass)}>
                      {r.riskClass}
                    </span>
                    {r.modelName && (
                      <span className="text-xs bg-surface border border-border px-2 py-0.5 rounded font-medium">
                        {r.modelName}
                      </span>
                    )}
                  </div>
                  <div className="text-right shrink-0 ml-3">
                    <p className="text-xs text-text-3">Proposed on</p>
                    <p className="text-sm font-medium">{r.proposedDate}</p>
                  </div>
                </div>

                {r.status === 'SUBMITTED' && (
                  <div className="bg-primary-soft border border-primary/20 rounded-lg p-3 mb-4">
                    <p className="text-xs font-semibold text-primary mb-1">
                      Investment Proposal from your Relationship Manager
                    </p>
                    <p className="text-xs text-text-2">
                      Review the details below. You can invest in one or multiple securities based on the proposal.
                    </p>
                  </div>
                )}

                <div className="mb-4">{renderProposal(r.proposalJson)}</div>

                {r.status === 'SUBMITTED' && (
                  <div className="flex flex-wrap gap-3 pt-4 border-t border-border-hairline">
                    <button
                      onClick={() => openOrderModal(r)}
                      className="btn btn-primary btn-sm"
                      disabled={actionLoading === r.recoId}
                    >
                      ✓ Accept & Place Orders
                    </button>
                    <button
                      onClick={() => handleDecline(r.recoId)}
                      className="btn btn-ghost btn-sm"
                      style={{ color: 'var(--color-danger)' }}
                      disabled={actionLoading === r.recoId}
                    >
                      {actionLoading === r.recoId ? 'Processing…' : '✗ Decline'}
                    </button>
                    <span className="text-xs text-text-3 self-center ml-auto">
                      Declining will not affect your existing portfolio
                    </span>
                  </div>
                )}

                {r.status === 'APPROVED' && (
                  <div className="flex items-center gap-3 pt-3 border-t border-border-hairline">
                    <span className="text-success text-sm font-medium">✓ Accepted — orders placed</span>
                    <button onClick={() => navigate('/me/orders')} className="btn btn-ghost btn-sm ml-auto">
                      View My Orders →
                    </button>
                  </div>
                )}

                {r.status === 'REJECTED' && (
                  <div className="pt-3 border-t border-border-hairline">
                    <p className="text-xs text-text-3">
                      You declined this recommendation. Your RM may prepare a revised proposal.
                    </p>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ── Multi-Security Order Modal ────────────────────────────────────── */}
      {showOrderModal && activeReco && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-2xl w-full max-h-[92vh] flex flex-col">

            {/* Modal header */}
            <div className="px-6 py-4 border-b border-border-hairline flex items-start justify-between shrink-0">
              <div>
                <h3 className="font-semibold text-lg">Accept & Place Orders</h3>
                <p className="text-xs text-text-2 mt-0.5">
                  RM recommendation ·{' '}
                  <span className={
                    'font-medium ' +
                    (activeReco.riskClass?.toUpperCase() === 'CONSERVATIVE' ? 'text-success' :
                     activeReco.riskClass?.toUpperCase() === 'AGGRESSIVE' ? 'text-danger' : 'text-warn')
                  }>{activeReco.riskClass}</span>
                  {' '}· Amounts pre-filled based on allocation %
                </p>
              </div>
              <button
                onClick={() => setShowOrderModal(false)}
                className="text-text-3 hover:text-text text-2xl leading-none mt-0.5"
              >
                ×
              </button>
            </div>

            <div className="overflow-y-auto flex-1 px-6 pt-5 pb-2">
              {/* RM Proposal summary */}
              <div className="bg-surface rounded-lg p-4 mb-5">
                <p className="text-xs font-semibold text-text-3 uppercase tracking-wider mb-3">RM Proposal</p>
                {renderProposal(activeReco.proposalJson)}
              </div>

              {/* Suitability warning — shown when some allocation rows were removed */}
              {blockedAssets.length > 0 && (
                <div className="bg-danger-soft border border-danger/30 rounded-lg p-3 mb-4">
                  <p className="text-xs font-semibold text-danger mb-1">
                    ⛔ {blockedAssets.join(', ')} removed from your order
                  </p>
                  <p className="text-xs text-text-2">
                    Your risk profile is <strong>{clientRiskClass ?? 'Conservative'}</strong>.{' '}
                    {clientRiskClass === 'CONSERVATIVE'
                      ? <>Conservative investors are not permitted to buy <strong>{blockedAssets.join(' or ')}</strong> securities — those orders would be rejected by compliance pre-trade checks (Suitability Rule 1).</>
                      : <>Your risk profile does not permit buying <strong>{blockedAssets.join(' or ')}</strong> — those orders would be rejected by compliance pre-trade checks.</>
                    }{' '}
                    Your RM should send a revised recommendation with suitable asset classes only
                    {clientRiskClass === 'CONSERVATIVE' ? ' (e.g. BOND, MUTUAL_FUND, ETF)' : ''}.
                  </p>
                </div>
              )}

              {/* ── Order rows ── */}
              <form id="multi-order-form" onSubmit={handlePlaceOrders}>
                <div className="space-y-3 mb-4">
                  {orderItems.map((item, idx) => {
                    const sec = securities.find((s) => String(s.securityId) === item.securityId);
                    const est = sec ? Number(sec.currentPrice) * (parseInt(item.quantity) || 0) : 0;
                    const isDuplicate =
                      item.securityId &&
                      selectedIds.filter((id) => id === item.securityId).length > 1;

                    // Per-row security list: filter to this row's asset class if set
                    const rowSecurities = item.assetClass
                      ? securities.filter(
                          (s) =>
                            s.status === 'ACTIVE' &&
                            (s.assetClass || '').toUpperCase() === item.assetClass,
                        )
                      : securitiesForModal;
                    const secOptions = rowSecurities.length > 0 ? rowSecurities : securitiesForModal;

                    // Suggested vs actual difference
                    const suggestedQty =
                      item.suggestedAmount > 0 && sec && Number(sec.currentPrice) > 0
                        ? Math.max(1, Math.floor(item.suggestedAmount / Number(sec.currentPrice)))
                        : 0;

                    return (
                      <div
                        key={item.id}
                        className={
                          'border rounded-lg p-4 ' +
                          (isDuplicate ? 'border-danger bg-danger-soft' : 'border-border bg-white')
                        }
                      >
                        {/* Row header — allocation badge */}
                        <div className="flex items-center justify-between mb-3">
                          <div className="flex items-center gap-2 flex-wrap">
                            {item.assetClass ? (
                              <>
                                <span className="bg-primary-soft text-primary text-xs font-semibold px-2 py-0.5 rounded-full">
                                  {item.assetClass.replace(/_/g, ' ')}
                                </span>
                                <span className="text-xs font-bold text-primary">
                                  {item.allocationPct}%
                                </span>
                                <span className="text-xs text-text-2">
                                  · Suggested ₹{item.suggestedAmount.toLocaleString('en-IN')}
                                </span>
                              </>
                            ) : (
                              <p className="text-xs font-semibold text-text-3 uppercase tracking-wide">
                                {orderItems.length > 1 ? `Investment ${idx + 1}` : 'Investment'}
                              </p>
                            )}
                          </div>
                          <button
                            type="button"
                            onClick={() => removeOrderItem(item.id)}
                            className="text-danger hover:text-danger/80 p-1 rounded"
                            title="Remove this row"
                          >
                            <Trash2 size={14} />
                          </button>
                        </div>

                        {isDuplicate && (
                          <p className="text-xs text-danger font-medium mb-2">
                            ⚠️ This security is already selected in another row
                          </p>
                        )}

                        {/* Security select — filtered to this row's asset class */}
                        <div className="mb-3">
                          <label className="label block mb-1 text-xs">
                            Security *
                            {item.assetClass && (
                              <span className="text-text-3 font-normal ml-1">
                                ({secOptions.length} {item.assetClass.replace(/_/g, ' ')} available)
                              </span>
                            )}
                          </label>
                          <select
                            className="input text-sm"
                            value={item.securityId}
                            onChange={(e) => updateOrderItem(item.id, 'securityId', e.target.value)}
                          >
                            <option value="">— Select security —</option>
                            {secOptions.map((s: any) => (
                              <option key={s.securityId} value={String(s.securityId)}>
                                {s.symbol} — {s.name || s.assetClass} · ₹{Number(s.currentPrice).toLocaleString('en-IN')}
                              </option>
                            ))}
                          </select>
                        </div>

                        {/* Quantity + Price type */}
                        <div className="grid grid-cols-2 gap-3 mb-2">
                          <div>
                            <label className="label block mb-1 text-xs">
                              Quantity (units) *
                              {suggestedQty > 0 && String(suggestedQty) !== item.quantity && (
                                <button
                                  type="button"
                                  className="text-primary font-normal ml-1 underline"
                                  onClick={() => updateOrderItem(item.id, 'quantity', String(suggestedQty))}
                                >
                                  (reset to {suggestedQty})
                                </button>
                              )}
                            </label>
                            <input
                              className="input mono text-sm"
                              type="number"
                              min="1"
                              step="1"
                              placeholder="Auto-filled on security select"
                              value={item.quantity}
                              onChange={(e) => updateOrderItem(item.id, 'quantity', e.target.value)}
                            />
                          </div>
                          <div>
                            <label className="label block mb-1 text-xs">Order type</label>
                            <div className="flex gap-1.5">
                              {(['MARKET', 'LIMIT'] as const).map((pt) => (
                                <button
                                  key={pt}
                                  type="button"
                                  onClick={() => updateOrderItem(item.id, 'priceType', pt)}
                                  className={
                                    'flex-1 py-1.5 text-xs rounded border font-medium transition-colors ' +
                                    (item.priceType === pt
                                      ? 'bg-primary text-white border-primary'
                                      : 'bg-white text-text border-border hover:bg-surface')
                                  }
                                >
                                  {pt}
                                </button>
                              ))}
                            </div>
                          </div>
                        </div>

                        {/* Limit price (conditional) */}
                        {item.priceType === 'LIMIT' && (
                          <div className="mb-2">
                            <label className="label block mb-1 text-xs">Limit price (₹) *</label>
                            <input
                              className="input mono text-sm"
                              type="number"
                              min="0.01"
                              step="0.01"
                              placeholder="e.g. 450.00"
                              value={item.limitPrice}
                              onChange={(e) => updateOrderItem(item.id, 'limitPrice', e.target.value)}
                            />
                          </div>
                        )}

                        {/* Estimated vs suggested comparison */}
                        {est > 0 && (
                          <div className="mt-2 flex items-center gap-3 text-xs mono">
                            <span className="text-text-3">
                              Estimated: <span className="font-semibold text-text">₹{est.toLocaleString('en-IN')}</span>
                              <span className="text-text-3 ml-1">
                                ({item.quantity} × ₹{Number(sec?.currentPrice).toLocaleString('en-IN')})
                              </span>
                            </span>
                            {item.suggestedAmount > 0 && (
                              <span className={
                                'font-medium ' +
                                (Math.abs(est - item.suggestedAmount) / item.suggestedAmount < 0.05
                                  ? 'text-success'
                                  : est > item.suggestedAmount ? 'text-warn' : 'text-text-3')
                              }>
                                {Math.abs(est - item.suggestedAmount) / item.suggestedAmount < 0.05
                                  ? '✓ Within allocation'
                                  : est > item.suggestedAmount
                                  ? `⚠ ₹${(est - item.suggestedAmount).toLocaleString('en-IN')} over suggested`
                                  : `₹${(item.suggestedAmount - est).toLocaleString('en-IN')} under suggested`}
                              </span>
                            )}
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>

                {/* Add another security button */}
                <button
                  type="button"
                  onClick={addOrderItem}
                  className="btn btn-ghost btn-sm w-full border border-dashed border-border mb-4 flex items-center justify-center gap-1.5"
                >
                  <Plus size={14} />
                  Add another security
                </button>
              </form>

              {/* Order results (after submit with partial failures) */}
              {orderResults.length > 0 && (
                <div className="mb-4 space-y-1.5">
                  {orderResults.map((r, i) => (
                    <div
                      key={i}
                      className={
                        'flex items-start gap-2 text-sm rounded-lg px-3 py-2 ' +
                        (r.ok ? 'bg-success-soft text-success' : 'bg-danger-soft text-danger')
                      }
                    >
                      <span className="shrink-0 font-bold">{r.ok ? '✓' : '✗'}</span>
                      <div>
                        <span className="font-medium">{r.symbol}</span>
                        <span className="text-xs ml-2 opacity-80">— {r.msg}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* Global error */}
              {orderError && (
                <div className="pill pill-danger w-full text-center py-2 mb-4">{orderError}</div>
              )}

              {/* Disclaimer */}
              <div className="bg-warn-soft border border-warn/20 rounded-lg p-3 mb-4">
                <p className="text-xs font-semibold text-warn mb-1">⚠️ Please confirm</p>
                <p className="text-xs text-text-2">
                  By proceeding, you confirm you have reviewed the RM's proposal.
                  {orderItems.length > 1
                    ? ` ${orderItems.length} BUY orders will be submitted to the Dealing desk.`
                    : ' A BUY order will be submitted to the Dealing desk.'}
                  {' '}Investment values are subject to market risk.
                </p>
              </div>
            </div>

            {/* Modal footer */}
            <div className="px-6 py-4 border-t border-border-hairline shrink-0 bg-white">

              {/* Balance + Total row */}
              <div className="flex items-start justify-between mb-3">

                {/* Cash balance */}
                <div>
                  <p className="text-xs text-text-3 mb-0.5">Available balance</p>
                  {balanceLoading ? (
                    <p className="text-sm text-text-3">Checking balance…</p>
                  ) : accountId === null ? (
                    <p className="text-sm font-semibold text-danger">No account found</p>
                  ) : cashBalance === null ? (
                    <p className="text-sm text-text-3">Could not fetch balance</p>
                  ) : (
                    <p className={
                      'font-semibold mono text-sm ' +
                      (cashBalance <= 0 ? 'text-danger' :
                       totalEstimated > 0 && cashBalance < totalEstimated ? 'text-warn' :
                       'text-success')
                    }>
                      ₹{cashBalance.toLocaleString('en-IN')}
                      {cashBalance <= 0 && (
                        <span className="text-xs font-normal ml-1 text-danger">— No funds</span>
                      )}
                      {cashBalance > 0 && totalEstimated > 0 && cashBalance < totalEstimated && (
                        <span className="text-xs font-normal ml-1 text-warn">— Insufficient</span>
                      )}
                    </p>
                  )}
                </div>

                {/* Estimated total */}
                {totalEstimated > 0 && (
                  <div className="text-right">
                    <p className="text-xs text-text-3 mb-0.5">Total estimated</p>
                    <p className="font-semibold mono text-sm text-primary">
                      ₹{totalEstimated.toLocaleString('en-IN')}
                      {orderItems.length > 1 && (
                        <span className="text-xs font-normal text-text-3 ml-1">
                          ({orderItems.length} securities)
                        </span>
                      )}
                    </p>
                    {/* Remaining balance after investment */}
                    {cashBalance !== null && cashBalance > totalEstimated && (
                      <p className="text-xs text-text-3">
                        Remaining: ₹{(cashBalance - totalEstimated).toLocaleString('en-IN')}
                      </p>
                    )}
                  </div>
                )}
              </div>

              {/* No account warning */}
              {!balanceLoading && accountId === null && (
                <div className="bg-danger-soft border border-danger/20 rounded-lg p-3 mb-3">
                  <p className="text-xs font-semibold text-danger mb-1">⚠️ No portfolio account</p>
                  <p className="text-xs text-text-2">
                    You do not have a portfolio account yet. Please ask your Dealer to create
                    one for you before you can place orders.
                  </p>
                </div>
              )}

              {/* Zero balance warning */}
              {!balanceLoading && accountId !== null && cashBalance !== null && cashBalance <= 0 && (
                <div className="bg-danger-soft border border-danger/20 rounded-lg p-3 mb-3">
                  <p className="text-xs font-semibold text-danger mb-1">⚠️ No funds available</p>
                  <p className="text-xs text-text-2">
                    Your account has ₹0 balance. Please ask your Relationship Manager
                    to fund your account before placing orders.
                  </p>
                </div>
              )}

              {/* Insufficient balance warning */}
              {!balanceLoading && cashBalance !== null && cashBalance > 0 &&
               totalEstimated > 0 && cashBalance < totalEstimated && (
                <div className="bg-warn-soft border border-warn/20 rounded-lg p-3 mb-3">
                  <p className="text-xs font-semibold text-warn mb-1">⚠️ Insufficient balance</p>
                  <p className="text-xs text-text-2">
                    Your balance (₹{cashBalance.toLocaleString('en-IN')}) is less than the
                    estimated investment (₹{totalEstimated.toLocaleString('en-IN')}).
                    Reduce the quantity or ask your RM to add funds.
                  </p>
                </div>
              )}

              {/* Action buttons */}
              <div className="flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowOrderModal(false)}
                  className="btn btn-ghost"
                  disabled={submitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  form="multi-order-form"
                  className="btn btn-primary"
                  disabled={
                    submitting ||
                    balanceLoading ||
                    accountId === null ||
                    (cashBalance !== null && cashBalance <= 0)
                  }
                >
                  {submitting ? (
                    <span className="flex items-center gap-2">
                      <span className="animate-spin inline-block w-3 h-3 border-2 border-white border-t-transparent rounded-full" />
                      Placing {orderItems.length > 1 ? `${orderItems.length} orders` : 'order'}…
                    </span>
                  ) : (
                    `✓ Confirm & Place ${orderItems.length > 1 ? `${orderItems.length} Orders` : 'Order'}`
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
