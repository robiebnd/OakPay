import { apiPost, apiPut, UserResponse } from './api';

export const accountApi = {
  updateProfile: (token: string, body: { firstName: string; lastName: string; phoneNumber?: string; country?: string; dateOfBirth?: string | null }) =>
    apiPut<UserResponse>('/api/v1/auth/me', token, body),
  changePassword: (token: string, currentPassword: string, newPassword: string) =>
    apiPost<void>('/api/v1/auth/change-password', token, { currentPassword, newPassword }),
};
