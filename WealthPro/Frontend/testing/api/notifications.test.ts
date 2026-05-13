import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import {
  createNotification,
  getNotificationsByUserId,
  getUnreadNotifications,
  markAllAsRead,
  markNotificationRead,
} from '@/api/notifications';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('notifications API', () => {
  beforeEach(() => vi.clearAllMocks());

  // ─── createNotification ──────────────────────────────────────────────────────

  it('createNotification posts to /api/notifications', async () => {
    const payload = { userId: 1, message: 'Hello', category: 'INFO' };
    const created = { notificationId: 5, ...payload };
    vi.mocked(api.post).mockResolvedValue({ data: created });

    const result = await createNotification(payload);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/notifications', payload);
    expect(result.notificationId).toBe(5);
  });

  it('createNotification returns created notification', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { notificationId: 10, message: 'Test' } });

    const result = await createNotification({ userId: 2, message: 'Test', category: 'ALERT' });

    expect(result.message).toBe('Test');
  });

  // ─── getNotificationsByUserId ────────────────────────────────────────────────

  it('getNotificationsByUserId calls correct endpoint', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getNotificationsByUserId(5);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/notifications/user/5');
  });

  it('getNotificationsByUserId returns list of notifications', async () => {
    const notifications = [{ notificationId: 1 }, { notificationId: 2 }];
    vi.mocked(api.get).mockResolvedValue({ data: notifications });

    const result = await getNotificationsByUserId(3);

    expect(result).toHaveLength(2);
  });

  // ─── getUnreadNotifications ──────────────────────────────────────────────────

  it('getUnreadNotifications calls unread endpoint', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getUnreadNotifications(7);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/notifications/user/7/unread');
  });

  it('getUnreadNotifications returns unread items', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ notificationId: 3, status: 'UNREAD' }] });

    const result = await getUnreadNotifications(7);

    expect(result[0].status).toBe('UNREAD');
  });

  // ─── markAllAsRead ───────────────────────────────────────────────────────────

  it('markAllAsRead puts to read-all endpoint', async () => {
    vi.mocked(api.put).mockResolvedValue({ data: null });

    await markAllAsRead(4);

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/notifications/user/4/read-all');
  });

  it('markAllAsRead returns response data', async () => {
    vi.mocked(api.put).mockResolvedValue({ data: { count: 3 } });

    const result = await markAllAsRead(4);

    expect(result).toEqual({ count: 3 });
  });

  // ─── markNotificationRead ────────────────────────────────────────────────────

  it('markNotificationRead puts READ status to notification endpoint', async () => {
    vi.mocked(api.put).mockResolvedValue({ data: { notificationId: 9, status: 'READ' } });

    const result = await markNotificationRead(9);

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/notifications/9/status', {
      status: 'READ',
    });
    expect(result.status).toBe('READ');
  });
});
