import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '@/auth/store';
import {
  getNotificationsByUserId,
  markAllAsRead,
  markNotificationRead,
} from '@/api/notifications';

// ─── userId cache written by Admin › Users page ───────────────────────────────
function getCachedUserId(username: string): number | null {
  try {
    const raw = localStorage.getItem('wp_user_id_map');
    if (!raw) return null;
    const map = JSON.parse(raw);
    const entry = map[username];
    if (!entry) return null;
    // Support both old format (plain number) and new format ({ userId, role })
    return typeof entry === 'object' ? Number(entry.userId) || null : Number(entry) || null;
  } catch {
    return null;
  }
}

// ─── Read-state persistence ───────────────────────────────────────────────────
function storageKey(userId: number) { return `wp_read_notifs_staff_${userId}`; }

function getLocalReadIds(userId: number): Set<number> {
  try {
    const raw = localStorage.getItem(storageKey(userId));
    return raw ? new Set(JSON.parse(raw) as number[]) : new Set();
  } catch { return new Set(); }
}

function saveLocalReadIds(userId: number, ids: Set<number>) {
  try { localStorage.setItem(storageKey(userId), JSON.stringify([...ids])); } catch { /* ignore */ }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

/**
 * Parse a clientId out of compliance notification messages:
 *   "…for client John Doe (ID: 5)…"
 *   "…for client 7 was rejected…"
 */
function extractClientId(msg: string): number | null {
  const m1 = msg.match(/\(ID:\s*(\d+)\)/i);
  if (m1) return Number(m1[1]);
  const m2 = msg.match(/for client\s+(\d+)/i);
  if (m2) return Number(m2[1]);
  return null;
}

function getCategoryPill(cat: string): string {
  if (cat === 'Compliance') return 'pill-danger';
  if (cat === 'Order') return 'pill-info';
  if (cat === 'KYC') return 'pill-warn';
  return 'pill-info';
}

// ─── Component ────────────────────────────────────────────────────────────────

export default function RmNotifications() {
  const user = useAuth((s) => s.user);

  // Resolve userId — try JWT claim first, then the admin-written localStorage cache
  const autoUserId: number | null = (() => {
    if (user?.userId) return Number(user.userId);
    if (user?.username) return getCachedUserId(user.username);
    return null;
  })();

  const [resolvedUserId] = useState<number | null>(autoUserId);
  const [notifications, setNotifications] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [markingAll, setMarkingAll] = useState(false);
  const [filter, setFilter] = useState<'ALL' | 'UNREAD'>('ALL');

  useEffect(() => {
    if (resolvedUserId) {
      loadNotifications(resolvedUserId);
    } else {
      setLoading(false);
    }
  }, [resolvedUserId]);

  async function loadNotifications(uid: number) {
    setLoading(true);
    try {
      const data = await getNotificationsByUserId(uid);
      if (Array.isArray(data)) {
        const localRead = getLocalReadIds(uid);
        const merged = data.map((n: any) => ({
          ...n,
          status: localRead.has(n.notificationId) ? 'READ' : n.status,
        }));
        setNotifications(
          [...merged].sort((a, b) => (b.createdDate || '').localeCompare(a.createdDate || ''))
        );
      }
    } catch {
      setNotifications([]);
    }
    setLoading(false);
  }

  async function handleMarkAllRead() {
    if (!resolvedUserId) return;
    setMarkingAll(true);
    const allIds = new Set(notifications.map((n) => n.notificationId as number));
    saveLocalReadIds(resolvedUserId, allIds);
    setNotifications((prev) => prev.map((n) => ({ ...n, status: 'READ' })));
    try { await markAllAsRead(resolvedUserId); } catch { /* localStorage is source of truth */ }
    setMarkingAll(false);
  }

  async function handleMarkOne(notificationId: number) {
    if (!resolvedUserId) return;
    const localRead = getLocalReadIds(resolvedUserId);
    localRead.add(notificationId);
    saveLocalReadIds(resolvedUserId, localRead);
    setNotifications((prev) =>
      prev.map((n) => n.notificationId === notificationId ? { ...n, status: 'READ' } : n)
    );
    try { await markNotificationRead(notificationId); } catch { /* silent */ }
  }

  const unreadCount = notifications.filter((n) => n.status !== 'READ').length;
  const allFiltered = filter === 'UNREAD' ? notifications.filter((n) => n.status !== 'READ') : notifications;
  const unreadList = allFiltered.filter((n) => n.status !== 'READ');
  const readList   = allFiltered.filter((n) => n.status === 'READ');

  function renderCard(n: any, isUnread: boolean) {
    const clientId = extractClientId(n.message || '');
    return (
      <div
        key={n.notificationId}
        className={'flex items-start gap-4 px-5 py-4 border-b border-border-hairline last:border-0 ' + (isUnread ? 'bg-blue-50/40' : '')}
      >
        <div className="mt-1.5 shrink-0">
          <div className={'w-2.5 h-2.5 rounded-full ' + (isUnread ? 'bg-primary' : 'bg-border')} />
        </div>
        <div className="flex-1 min-w-0">
          <p className={'text-sm ' + (isUnread ? 'font-medium' : 'text-text-2')}>{n.message}</p>
          <div className="flex items-center gap-3 mt-1.5">
            <p className="text-xs text-text-3">{(n.createdDate || '').replace('T', ' ').slice(0, 19)}</p>
            {clientId && (
              <Link
                to={`/rm/clients/${clientId}`}
                className="text-xs text-primary hover:underline font-medium"
                onClick={() => { if (isUnread) handleMarkOne(n.notificationId); }}
              >
                View client →
              </Link>
            )}
          </div>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <span className={'pill text-xs ' + getCategoryPill(n.category)}>{n.category}</span>
          {isUnread && (
            <button onClick={() => handleMarkOne(n.notificationId)} className="text-xs text-text-3 hover:text-primary" title="Mark as read">✓</button>
          )}
        </div>
      </div>
    );
  }

  // ── userId still not known (backend doesn't include it anywhere) ─────────────
  if (!loading && !resolvedUserId) {
    return (
      <div>
        <div className="mb-5">
          <h1 className="text-2xl font-semibold mb-1">Notifications</h1>
          <p className="text-sm text-text-2">Compliance alerts and system messages.</p>
        </div>
        <div className="panel">
          <div className="panel-b text-center py-12">
            <p className="text-3xl mb-3">🔔</p>
            <p className="font-semibold">No notifications yet</p>
            <p className="text-sm text-text-2 mt-1 max-w-sm mx-auto">
              Log out and log back in — your user ID will be registered automatically
              and notifications will appear here.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">
            Notifications
            {unreadCount > 0 && <span className="pill pill-danger ml-3">{unreadCount} unread</span>}
          </h1>
          <p className="text-sm text-text-2">
            Compliance alerts and system messages. Click "View client" to investigate.
          </p>
        </div>
        {unreadCount > 0 && (
          <button onClick={handleMarkAllRead} disabled={markingAll} className="btn btn-ghost btn-sm">
            {markingAll ? 'Marking...' : '✓ Mark all as read'}
          </button>
        )}
      </div>

      <div className="panel mb-4">
        <div className="panel-b py-3 flex gap-2">
          {(['ALL', 'UNREAD'] as const).map((f) => (
            <button key={f} onClick={() => setFilter(f)}
              className={'px-4 py-1.5 text-sm rounded border font-medium ' + (filter === f ? 'bg-primary text-white border-primary' : 'bg-white text-text-2 border-border')}>
              {f === 'ALL' ? `All (${notifications.length})` : `Unread (${unreadCount})`}
            </button>
          ))}
        </div>
      </div>

      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading notifications...</div>
        ) : allFiltered.length === 0 ? (
          <div className="panel-b text-center py-10">
            <p className="text-3xl mb-2">🔔</p>
            <p className="font-semibold">{filter === 'UNREAD' ? 'No unread notifications' : 'No notifications yet'}</p>
            <p className="text-sm text-text-2 mt-1">Compliance will notify you here when a client issue needs your attention.</p>
          </div>
        ) : (
          <div>
            {unreadList.map((n) => renderCard(n, true))}
            {readList.length > 0 && filter !== 'UNREAD' && (
              <>
                {unreadList.length > 0 && (
                  <div className="px-5 py-2 bg-surface border-b border-border-hairline">
                    <span className="text-xs font-semibold text-text-3 uppercase tracking-wide">Read</span>
                  </div>
                )}
                {readList.map((n) => renderCard(n, false))}
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
