const API_BASE_URL = (process.env.EXPO_PUBLIC_API_URL ?? '').replace(/\/$/, '');

export type ApiError = {
  status: number;
  message: string;
  fieldErrors?: Record<string, string>;
};

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  if (!API_BASE_URL) {
    throw new Error('EXPO_PUBLIC_API_URL is not configured.');
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      ...(options.headers ?? {}),
    },
  });

  const raw = await response.text();
  let body: unknown = null;

  try {
    body = raw ? JSON.parse(raw) : null;
  } catch {
    body = raw;
  }

  if (!response.ok) {
    const data = body as { message?: string; error?: string; fieldErrors?: Record<string, string> } | null;
    const error: ApiError = {
      status: response.status,
      message: data?.message ?? data?.error ?? (typeof body === 'string' ? body : 'Request failed'),
      fieldErrors: data?.fieldErrors,
    };
    throw error;
  }

  return body as T;
}

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
};

export type TokenResponse = {
  tokenType: string;
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
};

export type UserResponse = {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  emailVerified: boolean;
};

export const authApi = {
  login: (request: LoginRequest) =>
    requestJson<TokenResponse>('/api/v1/auth/login', request),
  register: (request: RegisterRequest) =>
    requestJson<UserResponse>('/api/v1/auth/register', request, 'POST'),
  refresh: (refreshToken: string) =>
    requestJson<TokenResponse>('/api/v1/auth/refresh', { refreshToken }),
  logout: (refreshToken: string) =>
    requestJson<void>('/api/v1/auth/logout', { refreshToken }, 'POST'),
};

async function requestJson<T>(path: string, body: unknown, method = 'POST') {
  return request<T>(path, {
    method,
    body: JSON.stringify(body),
  });
}

export async function apiGet<T>(path: string, accessToken: string): Promise<T> {
  return request<T>(path, {
    method: 'GET',
    headers: { Authorization: `Bearer ${accessToken}` },
  });
}

export async function apiPost<T>(path: string, accessToken: string, body?: unknown): Promise<T> {
  return request<T>(path, {
    method: 'POST',
    headers: { Authorization: `Bearer ${accessToken}` },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
}
