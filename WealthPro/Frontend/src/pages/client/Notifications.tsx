import { useState, useEffect } from 'react';
import { useAuth } from '@/auth/store';
import {
  getNotificationsByUserId,
  markAllAsRead,
  markNotificationRead,
} from '@/api/notifications';
import { TableSkeleton } from '@/components/Skeleton';
import EmptyState from '@/components/EmptyState';
import { Bell, BellOff } from 'lucide-react';

// localStorage key per client so different clients don't share read state
function storageKey(clientId: number) {
  return `wp_read_notifs_${clientId}`;
}

function getLocalReadIds(clientId: number): Set<number> {
  try {
    const raw = localStorage.getItem(storageKey(clientId));
    if (!raw) return new Set();
    return new Set(JSON.parse(raw) as number[]);
  } catch {
    return new Set();
  }
}

function saveLocalReadIds(clientId: number, ids: Set<number>) {
  try {
    localStorage.setItem(storageKey(clientId), JSON.stringify([...ids]));
  } catch {
    // storage full or unavailable — silent fail
  }
}

export default function Notifications() {
  const user = useAuth((s) => s.user);
  const clientId = user?.clientId;

  const [notifications, setNotifications] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [markingAll, setMarkingAll] = useState(false);
  const [filter, setFilter] = useState<'ALL' | 'UNREAD'>('ALL');

  useEffect(() => {
    if (clientId) loadNotifications();
  }, [clientId]);

  async function loadNotifications() {
    if (!clientId) return;
    setLoading(true);
    try {
      const data = await getNotificationsByUserId(clientId);
      if (Array.isArray(data)) {
        // merge server status with locally stored read IDs
        const localRead = getLocalReadIds(clientId);
        const merged = data.map((n: any) => ({
          ...n,
          status: localRead.has(n.notificationId) ? 'READ' : n.status,
        }));
        const sorted = [...merged].sort((a, b) =>
          (b.createdDate || '').localeCompare(a.createdDate || '')
        );
        setNotifications(sorted);
      }
    } catch (e) {
      setNotifications([]);
    }
    setLoading(false);
  }

  async function handleMarkAllRead() {
    if (!clientId) return;
    setMarkingAll(true);

    // persist all IDs to localStorage so reload remembers
    const allIds = new Set(notifications.map((n) => n.notificationId as number));
    saveLocalReadIds(clientId, allIds);

    // optimistically update UI immediately
    setNotifications((prev) => prev.map((n) => ({ ...n, status: 'READ' })));

    try {
      await markAllAsRead(clientId);
    } catch (e) {
    }
    setMarkingAll(false);
  }

  async function handleMarkOne(notificationId: number) {
    if (!clientId) return;

    // persist to localStorage
    const localRead = getLocalReadIds(clientId);
    localRead.add(notificationId);
    saveLocalReadIds(clientId, localRead);

    // optimistically update UI
    setNotifications((prev) =>
      prev.map((n) =>
        n.notificationId === notificationId ? { ...n, status: 'READ' } : n
      )
    );

    try {
      await markNotificationRead(notificationId);
    } catch (e) {
    }
  }

  function getCategoryPill(cat: string) {
    if (cat === 'Order') return 'pill-info';
    if (cat === 'KYC') return 'pill-warn';
    if (cat === 'System') return 'pill-danger';
    return 'pill-info';
  }

  // Replace raw #N ids with production-style reference e.g. REF-0044
  function formatMessage(msg: string) {
    return msg.replace(/#(\d+)/g, (_, n) => 'REF-' + String(n).padStart(4, '0'));
  }

  const unreadCount = notifications.filter((n) => n.status !== 'READ').length;

  const allFiltered = filter === 'UNREAD'
    ? notifications.filter((n) => n.status !== 'READ')
    : notifications;

  // Always show unread first, then read — within each group keep date order
  const unreadList = allFiltered.filter((n) => n.status !== 'READ');
  const readList   = allFiltered.filter((n) => n.status === 'READ');

  if (!clientId) return <div className="p-10 text-center text-text-2">Loading...</div>;

  return (
    <div>
      <div className="flex justify-between items-end mb-5">
        <div>
          <h1 className="text-2xl font-semibold mb-1">
            Notifications
            {unreadCount > 0 && (
              <span className="pill pill-danger ml-3">{unreadCount} unread</span>
            )}
          </h1>
          <p className="text-sm text-text-2">
            Order updates, KYC alerts, and system messages.
          </p>
        </div>
        {unreadCount > 0 && (
          <button
            onClick={handleMarkAllRead}
            disabled={markingAll}
            className="btn btn-ghost btn-sm"
          >
            {markingAll ? 'Marking...' : '✓ Mark all as read'}
          </button>
        )}
      </div>

      {/* filter toggle */}
      <div className="panel mb-4">
        <div className="panel-b py-3 flex gap-2">
          {(['ALL', 'UNREAD'] as const).map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={
                'px-4 py-1.5 text-sm rounded border font-medium ' +
                (filter === f
                  ? 'bg-primary text-white border-primary'
                  : 'bg-white text-text-2 border-border')
              }
            >
              {f === 'ALL' ? 'All (' + notifications.length + ')' : 'Unread (' + unreadCount + ')'}
            </button>
          ))}
        </div>
      </div>

      <div className="panel">
        {loading ? (
          <TableSkeleton rows={5} cols={3} />
        ) : allFiltered.length === 0 ? (
          <EmptyState
            icon={filter === 'UNREAD' ? <BellOff size={26} /> : <Bell size={26} />}
            title={filter === 'UNREAD' ? 'No unread notifications' : 'No notifications yet'}
            description="You'll get notified when your orders are routed, filled, or rejected."
          />
        ) : (
          <div>
            {/* ── Unread section ── */}
            {unreadList.map((n: any) => (
              <div
                key={n.notificationId}
                className="flex items-start gap-4 px-5 py-4 border-b border-border-hairline bg-blue-50/40"
              >
                <div className="mt-1.5 shrink-0">
                  <div className="w-2.5 h-2.5 rounded-full bg-primary" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium">{formatMessage(n.message)}</p>
                  <p className="text-xs text-text-3 mt-1">
                    {(n.createdDate || '').replace('T', ' ').slice(0, 19)}
                  </p>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <span className={'pill text-xs ' + getCategoryPill(n.category)}>{n.category}</span>
                  <button
                    onClick={() => handleMarkOne(n.notificationId)}
                    className="text-xs text-text-3 hover:text-primary"
                    title="Mark as read"
                  >
                    ✓
                  </button>
                </div>
              </div>
            ))}

            {/* ── Read section ── */}
            {readList.length > 0 && filter !== 'UNREAD' && (
              <>
                {unreadList.length > 0 && (
                  <div className="px-5 py-2 bg-surface border-b border-border-hairline">
                    <span className="text-xs font-semibold text-text-3 uppercase tracking-wide">
                      Read
                    </span>
                  </div>
                )}
                {readList.map((n: any) => (
                  <div
                    key={n.notificationId}
                    className="flex items-start gap-4 px-5 py-4 border-b border-border-hairline last:border-0"
                  >
                    <div className="mt-1.5 shrink-0">
                      <div className="w-2.5 h-2.5 rounded-full bg-border" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm text-text-2">{formatMessage(n.message)}</p>
                      <p className="text-xs text-text-3 mt-1">
                        {(n.createdDate || '').replace('T', ' ').slice(0, 19)}
                      </p>
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      <span className={'pill text-xs ' + getCategoryPill(n.category)}>{n.category}</span>
                    </div>
                  </div>
                ))}
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
