import * as SecureStore from 'expo-secure-store';

const rawApiBaseUrl = (process.env.EXPO_PUBLIC_API_URL ?? '').trim();
const API_BASE_URL = rawApiBaseUrl
  ? (/^https?:\/\//i.test(rawApiBaseUrl) ? rawApiBaseUrl : `http://${rawApiBaseUrl}`).replace(/\/$/, '')
  : '';
const REQUEST_TIMEOUT_MS = 15000;
type ApiErrorBody = { message?: string; error?: string; fieldErrors?: Record<string, string> };

export class OakPayApiError extends Error {
  readonly status: number;
  readonly fieldErrors?: Record<string, string>;

  constructor(status: number, message: string, fieldErrors?: Record<string, string>) {
    super(message);
    this.name = 'OakPayApiError';
    this.fieldErrors = fieldErrors;
  }
}

export function isAuthError(error: unknown): error is OakPayApiError {
  return error instanceof OakPayApiError && (error.status === 401 || error.status === 403);
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  if (!API_BASE_URL) throw new Error('EXPO_PUBLIC_API_URL is not configured.');
  const controller = new AbortController();
  let timeoutId: ReturnType<typeof setTimeout> | undefined;
  const timeout = new Promise<never>((_, reject) => {
    timeoutId = setTimeout(() => { controller.abort(); reject(new Error(`Request timed out after ${REQUEST_TIMEOUT_MS / 1000}s. Check that the OakPay Gateway is running and reachable at ${API_BASE_URL}.`)); }, REQUEST_TIMEOUT_MS);
  });
  try {
    const response = await Promise.race([fetch(`${API_BASE_URL}${path}`, {...options,signal:controller.signal,headers:{Accept:'application/json','Content-Type':'application/json',...(options.headers ?? {})}}),timeout]);
    const raw = await response.text(); let body: unknown = null;
    try { body = raw ? JSON.parse(raw) : null; } catch { body = raw; }
    if (!response.ok) { const data=(body ?? {}) as ApiErrorBody; const message=data.message ?? data.error ?? (typeof body==='string'?body:`Request failed with status ${response.status}`); throw new OakPayApiError(response.status,message,data.fieldErrors); }
    return body as T;
  } catch (error) {
    if (error instanceof Error && error.message.startsWith('Request timed out')) throw error;
    if (error instanceof OakPayApiError) throw error;
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
export type P2PRate = { baseCurrency:string; quoteCurrency:string; rate:number; source:string; effectiveAt:string };
export type P2PTrade = { id:string; advertisementId?:string; sellerId?:string; buyerId?:string; asset?:string; fiatCurrency?:string; quantity?:number; unitPrice?:number; fiatAmount?:number; paymentMethod?:string; status?:string; paymentReference?:string; paymentNote?:string; expiresAt?:string; createdAt?:string; updatedAt?:string };
export type P2PPayment = { id:string; tradeId:string; payerId:string; payeeId:string; amount:number; currency:string; paymentMethod:string; paymentReference:string; note?:string; status:string; submittedAt?:string; verifiedAt?:string };

export const authApi = {
  login: (request: LoginRequest) => requestJson<TokenResponse>('/api/v1/auth/login', request),
  register: (request: RegisterRequest) => requestJson<UserResponse>('/api/v1/auth/register', request),
  refresh: (refreshToken: string) => requestJson<TokenResponse>('/api/v1/auth/refresh', { refreshToken }),
  logout: (refreshToken: string) => requestJson<void>('/api/v1/auth/logout', { refreshToken }),
  me: (token: string) => apiGet<UserResponse>('/api/v1/auth/me', token),
};

export const walletApi = {
  wallets: (token: string) => apiGet<Wallet[]>('/api/v1/wallets', token),
  createWallet: (token: string, currency: string) => apiPost<Wallet>('/api/v1/wallets', token, { currency }),
  transactions: (token: string, currency?: string) => apiGet<LedgerTransaction[]>(`/api/v1/wallets/transactions?limit=30${currency ? `&currency=${encodeURIComponent(currency)}` : ''}`, token),
  deposit: (token: string, currency: string, amount: number) => apiPost<LedgerTransaction>(`/api/v1/wallets/${currency}/deposit`, token, { amount, reference: `MOBILE-DEPOSIT-${Date.now()}` }),
  withdraw: (token: string, currency: string, amount: number) => apiPost<LedgerTransaction>(`/api/v1/wallets/${currency}/withdraw`, token, { amount, reference: `MOBILE-WITHDRAW-${Date.now()}` }),
  depositAddresses: (token: string) => apiGet<DepositAddress[]>('/api/v1/wallets/deposit-addresses', token),
  depositAddressesForAsset: async (token: string, currency: string) => { const addresses = await apiGet<DepositAddress[]>('/api/v1/wallets/deposit-addresses', token); return addresses.filter(address => address.currency.toUpperCase() === currency.toUpperCase()); },
  depositAddress: (token: string, currency: string, network: string) => apiGet<DepositAddress>(`/api/v1/wallets/deposit-addresses/${encodeURIComponent(currency)}/${encodeURIComponent(network)}`, token),
};

export const p2pApi = {
  ads: (token: string, side: 'BUY' | 'SELL', asset = 'USDT', fiat = 'ZWG', paymentMethod?: string) => apiGet<P2PAd[]>(`/api/v1/p2p/ads?side=${side}&asset=${encodeURIComponent(asset)}&fiatCurrency=${encodeURIComponent(fiat)}&limit=50${paymentMethod ? `&paymentMethod=${encodeURIComponent(paymentMethod)}` : ''}`, token),
  mineAds: (token: string) => apiGet<P2PAd[]>('/api/v1/p2p/ads/mine', token),
  createAd: (token: string, body: { side: 'BUY' | 'SELL'; asset: string; fiatCurrency: string; price: number; totalQuantity: number; minQuantity: number; maxQuantity: number; paymentMethods: string; terms?: string }) => apiPost<P2PAd>('/api/v1/p2p/ads', token, body),
  updateAd: (token: string, adId: string, body: { price?: number; minQuantity?: number; maxQuantity?: number; paymentMethods?: string; terms?: string }) => apiPut<P2PAd>(`/api/v1/p2p/ads/${adId}`, token, body),
  pauseAd: (token: string, adId: string) => apiPost<P2PAd>(`/api/v1/p2p/ads/${adId}/pause`, token),
  resumeAd: (token: string, adId: string) => apiPost<P2PAd>(`/api/v1/p2p/ads/${adId}/resume`, token),
  closeAd: (token: string, adId: string) => apiPost<P2PAd>(`/api/v1/p2p/ads/${adId}/close`, token),
  rate: (token: string, baseCurrency = 'USDT', quoteCurrency = 'ZWG') => apiGet<P2PRate>(`/api/v1/p2p/rates?baseCurrency=${encodeURIComponent(baseCurrency)}&quoteCurrency=${encodeURIComponent(quoteCurrency)}`, token),
  take: (token: string, adId: string, quantity: number, paymentMethod: string) => apiPost<P2PTrade>(`/api/v1/p2p/ads/${adId}/take`, token, { quantity, paymentMethod, expiryMinutes: 30 }),
  trades: (token: string, status?: string, asset?: string) => apiGet<P2PTrade[]>(`/api/v1/p2p/trades?limit=50${status ? `&status=${encodeURIComponent(status)}` : ''}${asset ? `&asset=${encodeURIComponent(asset)}` : ''}`, token),
  trade: (token: string, tradeId: string) => apiGet<P2PTrade>(`/api/v1/p2p/trades/${tradeId}`, token),
  payment: (token: string, tradeId: string) => apiGet<P2PPayment>(`/api/v1/p2p/trades/${tradeId}/payment`, token),
  markPaid: (token: string, tradeId: string, paymentReference: string, paymentNote?: string) => apiPost<P2PTrade>(`/api/v1/p2p/trades/${tradeId}/paid`, token, { paymentReference, paymentNote }),
  verifyPayment: (token: string, tradeId: string) => apiPost<P2PPayment>(`/api/v1/p2p/trades/${tradeId}/payment/verify`, token),
  confirm: async (token: string, tradeId: string) => { await apiPost<P2PPayment>(`/api/v1/p2p/trades/${tradeId}/payment/verify`, token); return apiPost<P2PTrade>(`/api/v1/p2p/trades/${tradeId}/confirm`, token); },
  cancel: (token: string, tradeId: string) => apiPost<P2PTrade>(`/api/v1/p2p/trades/${tradeId}/cancel`, token),
  dispute: (token: string, tradeId: string, reason = 'Payment dispute opened from OakPay mobile app', evidence?: string) => apiPost<unknown>(`/api/v1/p2p/trades/${tradeId}/dispute`, token, { reason, evidence: evidence?.trim() || undefined }),
};

async function requestJson<T>(path: string, body: unknown, method = 'POST') { return request<T>(path, { method, body: JSON.stringify(body) }); }

let refreshPromise: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
  if (refreshPromise) return refreshPromise;
  refreshPromise = (async () => {
    try {
      const refreshToken = await SecureStore.getItemAsync('oakpay.refreshToken');
      if (!refreshToken) return null;
      const tokens = await authApi.refresh(refreshToken);
      await SecureStore.setItemAsync('oakpay.accessToken', tokens.accessToken);
      await SecureStore.setItemAsync('oakpay.refreshToken', tokens.refreshToken);
      return tokens.accessToken;
    } catch {
      return null;
    } finally {
      refreshPromise = null;
    }
  })();
  return refreshPromise;
}

async function authenticatedRequest<T>(path: string, accessToken: string, method: 'GET' | 'POST' | 'PUT', body?: unknown): Promise<T> {
  const options: RequestInit = {
    method,
    headers: { Authorization: `Bearer ${accessToken}` },
    ...(body === undefined ? {} : { body: JSON.stringify(body) }),
  };
  try {
    return await request<T>(path, options);
  } catch (error) {
    if (!(error instanceof OakPayApiError) || error.status !== 401) throw error;
    const refreshedToken = await refreshAccessToken();
    if (!refreshedToken) throw error;
    return request<T>(path, {
      ...options,
      headers: { Authorization: `Bearer ${refreshedToken}` },
    });
  }
}

export async function apiGet<T>(path: string, accessToken: string): Promise<T> { return authenticatedRequest<T>(path, accessToken, 'GET'); }
export async function apiPost<T>(path: string, accessToken: string, body?: unknown): Promise<T> { return authenticatedRequest<T>(path, accessToken, 'POST', body); }
export async function apiPut<T>(path: string, accessToken: string, body: unknown): Promise<T> { return authenticatedRequest<T>(path, accessToken, 'PUT', body); }
