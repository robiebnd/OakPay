import { apiGet, apiPost } from './api';

export type AppNotification = {
  id: string;
  type: string;
  title: string;
  message: string;
  data?: string | null;
  read: boolean;
  createdAt: string;
};

export type RegisterPushDeviceRequest = {
  expoPushToken: string;
  platform: string;
  deviceId?: string | null;
};

export const notificationsApi = {
  list: (token: string, limit = 50) =>
    apiGet<AppNotification[]>(`/api/v1/notifications?limit=${Math.min(Math.max(limit, 1), 100)}`, token),
  unreadCount: (token: string) =>
    apiGet<number>('/api/v1/notifications/unread-count', token),
  markRead: (token: string, notificationId: string) =>
    apiPost<void>(`/api/v1/notifications/${notificationId}/read`, token),
  markAllRead: (token: string) =>
    apiPost<void>('/api/v1/notifications/read-all', token),
  registerDevice: (token: string, body: RegisterPushDeviceRequest) =>
    apiPost<void>('/api/v1/notifications/devices', token, body),
  createTest: (token: string) =>
    apiPost<AppNotification>('/api/v1/notifications/test', token),
};
