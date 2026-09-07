const API_BASE_URL = (process.env.EXPO_PUBLIC_API_URL ?? '').replace(/\/$/, '');
const REQUEST_TIMEOUT_MS = 15000;

type ApiErrorBody = {
  message?: string;
  error?: string;
  fieldErrors?: Record<string, string>;
};

export type ApiError = {
  status: number;
  message: string;
  fieldErrors?: Record<string, string>;
};

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  if (!API_BASE_URL) throw new Error('EXPO_PUBLIC_API_URL is not configured.');

  const controller = new AbortController();
  let timeoutId: ReturnType<typeof setTimeout> | undefined;
  const timeout = new Promise<never>((_, reject) => {
    timeoutId = setTimeout(() => {
      controller.abort();
      reject(new Error(`Request timed out after ${REQUEST_TIMEOUT_MS / 1000}s. Check that the OakPay Gateway is running and reachable at ${API_BASE_URL}.`));
    }, REQUEST_TIMEOUT_MS);
  });

  try {
    const response = await Promise.race([
      fetch(`${API_BASE_URL}${path}`, {
        ...options,
        signal: controller.signal,
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
          ...(options.headers ?? {}),
        },
      }),
      timeout,
    ]);

    const raw = await response.text();
    let body: unknown = null;
    try {
      body = raw ? JSON.parse(raw) : null;
    } catch {
      body = raw;
    }

    if (!response.ok) {
      const data = (body ?? {}) as ApiErrorBody;
      throw {
        status: response.status,
        message: data.message ?? data.error ?? (typeof body === 'string' ? body : `Request failed with status ${response.status}`),
        fieldErrors: data.fieldErrors,
      } satisfies ApiError;
    }

    return body as T;
  } catch (error) {
    if (error instanceof Error && error.message.startsWith('Request timed out')) throw error;
    if (error instanceof TypeError) {
      throw new Error(`Unable to reach OakPay Gateway at ${API_BASE_URL}. Make sure your phone and PC are on the same network and the Gateway is running.`);
    }
    throw error;
  } finally {
    if (timeoutId) clearTimeout(timeoutId);
  }
}

export type LoginRequest = { email: string; password: string };
export type RegisterRequest = { email: string; password: string; firstName: string; lastName: string };
export type TokenResponse = { tokenType: string; accessToken: string; refreshToken: string; expiresIn: number };
export type UserResponse = { id: string; email: string; firstName: string; lastName: string; emailVerified: boolean };

export const authApi = {
  login: (request: LoginRequest) => requestJson<TokenResponse>('/api/v1/auth/login', request),
  register: (request: RegisterRequest) => requestJson<UserResponse>('/api/v1/auth/register', request),
  refresh: (refreshToken: string) => requestJson<TokenResponse>('/api/v1/auth/refresh', { refreshToken }),
  logout: (refreshToken: string) => requestJson<void>('/api/v1/auth/logout', { refreshToken }),
};

async function requestJson<T>(path: string, body: unknown, method = 'POST') {
  return request<T>(path, { method, body: JSON.stringify(body) });
}

export async function apiGet<T>(path: string, accessToken: string): Promise<T> {
  return request<T>(path, { method: 'GET', headers: { Authorization: `Bearer ${accessToken}` } });
}

export async function apiPost<T>(path: string, accessToken: string, body?: unknown): Promise<T> {
  return request<T>(path, {
    method: 'POST',
    headers: { Authorization: `Bearer ${accessToken}` },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
}
