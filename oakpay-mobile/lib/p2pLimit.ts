import { apiGet } from './api';

export type P2PTradeLimit = {
  fiatCurrency: string;
  unlimited: boolean;
  maximumFiatAmount: number | null;
};

export function getP2PTradeLimit(token: string, fiatCurrency: string) {
  return apiGet<P2PTradeLimit>(
    `/api/v1/p2p/trades/trade-limit?fiatCurrency=${encodeURIComponent(fiatCurrency)}`,
    token,
  );
}
