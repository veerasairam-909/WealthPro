import { api } from './client';

export async function createNotification(data: { userId: number; message: string; category: string }) {
  const res = await api.post('/api/notifications', data);
  return res.data;
}

export async function getNotificationsByUserId(userId: number) {
  const res = await api.get('/api/notifications/user/' + userId);
  return res.data;
}

export async function getUnreadNotifications(userId: number) {
  const res = await api.get('/api/notifications/user/' + userId + '/unread');
  return res.data;
}

export async function markAllAsRead(userId: number) {
  const res = await api.put('/api/notifications/user/' + userId + '/read-all');
  return res.data;
}

export async function markNotificationRead(notificationId: number) {
  const res = await api.put('/api/notifications/' + notificationId + '/status', {
    status: 'READ',
  });
  return res.data;
}
