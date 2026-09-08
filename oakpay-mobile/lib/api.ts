const API_BASE_URL = (process.env.EXPO_PUBLIC_API_URL ?? '').replace(/\/$/, '');
const REQUEST_TIMEOUT_MS = 15000;
type ApiErrorBody = { message?: string; error?: string; fieldErrors?: Record<string, string> };
export type ApiError = { status: number; message: string; fieldErrors?: Record<string, string> };

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
    const response = await Promise.race([fetch(`${API_BASE_URL}${path}`, { ...options, signal: controller.signal, headers: { Accept: 'application/json', 'Content-Type': 'application/json', ...(options.headers ?? {}) } }), timeout]);
    const raw = await response.text();
    let body: unknown = null;
    try { body = raw ? JSON.parse(raw) : null; } catch { body = raw; }
    if (!response.ok) {
      const data = (body ?? {}) as ApiErrorBody;
      const message = data.message ?? data.error ?? (typeof body === 'string' ? body : `Request failed with status ${response.status}`);
      throw { status: response.status, message, fieldErrors: data.fieldErrors } satisfies ApiError;
    }
    return body as T;
  } catch (error) {
    if (error instanceof Error && error.message.startsWith('Request timed out')) throw error;
    if (error && typeof error === 'object' && 'status' in error && 'message' in error) {
      const apiError = error as ApiError;
      throw new Error(`${apiError.status} — ${apiError.message}`);
    }
    if (error instanceof TypeError) throw new Error(`Unable to reach OakPay Gateway at ${API_BASE_URL}. Make sure your phone and PC are on the same network and the Gateway is running.`);
    if (error instanceof Error) throw error;
    throw new Error('OakPay request failed for an unknown reason.');
  } finally { if (timeoutId) clearTimeout(timeoutId); }
}

export type LoginRequest = { email: string; password: string };
export type RegisterRequest = { email: string; password: string; firstName: string; lastName: string };
export type TokenResponse = { tokenType: string; accessToken: string; refreshToken: string; expiresIn: number };
export type UserResponse = { id: string; email: string; firstName: string; lastName: string; emailVerified: boolean };
export type Wallet = { id: string; userId: string; currency: string; availableBalance: number; lockedBalance: number; totalBalance: number; createdAt: string; updatedAt: string };
export type LedgerTransaction = { id: string; walletId: string; userId: string; transactionType: string; status: string; direction: string; balanceType: string; currency: string; amount: number; balanceBefore: number; balanceAfter: number; reference: string; metadata?: string; createdAt: string };
export type DepositAddress = { id: string; currency: string; network: string; address: string; memoTag?: string; status: string; createdAt: string };
export type P2PAd = { id: string; ownerId: string; side: 'BUY' | 'SELL'; asset: string; fiatCurrency: string; price: number; totalQuantity: number; availableQuantity: number; minQuantity: number; maxQuantity: number; paymentMethods: string; terms: string; status: string; createdAt: string; updatedAt: string };
export type P2PTrade = { id: string; advertisementId?: string; sellerId?: string; buyerId?: string; asset?: string; fiatCurrency?: string; quantity?: number; unitPrice?: number; fiatAmount?: number; paymentMethod?: string; status?: string; paymentReference?: string; paymentNote?: string; expiresAt?: string; createdAt?: string; updatedAt?: string };

export const authApi = {
  login: (request: LoginRequest) => requestJson<TokenResponse>('/api/v1/auth/login', request),
  register: (request: RegisterRequest) => requestJson<UserResponse>('/api/v1/auth/register', request),
  refresh: (refreshToken: string) => requestJson<TokenResponse>('/api/v1/auth/refresh', { refreshToken }),
  logout: (refreshToken: string) => requestJson<void>('/api/v1/auth/logout', { refreshToken })
};

export const walletApi = {
  wallets: (token: string) => apiGet<Wallet[]>('/api/v1/wallets', token),
  transactions: (token: string, currency?: string) => apiGet<LedgerTransaction[]>(`/api/v1/wallets/transactions?limit=30${currency ? `&currency=${encodeURIComponent(currency)}` : ''}`, token),
  deposit: (token: string, currency: string, amount: number) => apiPost<LedgerTransaction>(`/api/v1/wallets/${currency}/deposit`, token, { amount, reference: `MOBILE-DEPOSIT-${Date.now()}` }),
  withdraw: (token: string, currency: string, amount: number) => apiPost<LedgerTransaction>(`/api/v1/wallets/${currency}/withdraw`, token, { amount, reference: `MOBILE-WITHDRAW-${Date.now()}` }),
  depositAddresses: (token: string) => apiGet<DepositAddress[]>('/api/v1/wallets/deposit-addresses', token),
  depositAddress: (token: string, currency: string, network: string) => apiGet<DepositAddress>(`/api/v1/wallets/deposit-addresses/${encodeURIComponent(currency)}/${encodeURIComponent(network)}`, token)
};

export const p2pApi = {
  ads: (token: string, side: 'BUY' | 'SELL', asset = 'USDT', fiat = 'ZWG', paymentMethod?: string) => apiGet<P2PAd[]>(`/api/v1/p2p/ads?side=${side}&asset=${encodeURIComponent(asset)}&fiatCurrency=${encodeURIComponent(fiat)}&limit=50${paymentMethod ? `&paymentMethod=${encodeURIComponent(paymentMethod)}` : ''}`, token),
  take: (token: string, adId: string, quantity: number, paymentMethod: string) => apiPost<P2PTrade>(`/api/v1/p2p/ads/${adId}/take`, token, { quantity, paymentMethod, expiryMinutes: 30 }),
  trades: (token: string, status?: string, asset?: string) => apiGet<P2PTrade[]>(`/api/v1/p2p/trades?limit=50${status ? `&status=${encodeURIComponent(status)}` : ''}${asset ? `&asset=${encodeURIComponent(asset)}` : ''}`, token),
  trade: (token: string, tradeId: string) => apiGet<P2PTrade>(`/api/v1/p2p/trades/${tradeId}`, token),
  markPaid: (token: string, tradeId: string, paymentReference: string, paymentNote?: string) => apiPost<P2PTrade>(`/api/v1/p2p/trades/${tradeId}/paid`, token, { paymentReference, paymentNote }),
  confirm: (token: string, tradeId: string) => apiPost<P2PTrade>(`/api/v1/p2p/trades/${tradeId}/confirm`, token),
  cancel: (token: string, tradeId: string) => apiPost<P2PTrade>(`/api/v1/p2p/trades/${tradeId}/cancel`, token)
};

async function requestJson<T>(path: string, body: unknown, method = 'POST') { return request<T>(path, { method, body: JSON.stringify(body) }); }
export async function apiGet<T>(path: string, accessToken: string): Promise<T> { return request<T>(path, { method: 'GET', headers: { Authorization: `Bearer ${accessToken}` } }); }
export async function apiPost<T>(path: string, accessToken: string, body?: unknown): Promise<T> { return request<T>(path, { method: 'POST', headers: { Authorization: `Bearer ${accessToken}` }, body: body === undefined ? undefined : JSON.stringify(body) }); }
