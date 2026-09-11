import { authApi, UserResponse } from './api';

export async function updateUserProfile(
  accessToken: string,
  profile: { firstName: string; lastName: string; phoneNumber?: string; country?: string; dateOfBirth?: string | null },
): Promise<UserResponse> {
  const response = await fetch('/api/v1/auth/me', {
    method: 'PUT',
    headers: { Authorization: `Bearer ${accessToken}`, 'Content-Type': 'application/json' },
    body: JSON.stringify(profile),
  });
  if (!response.ok) throw new Error(`Profile update failed (${response.status}).`);
  return response.json();
}

export { authApi };
