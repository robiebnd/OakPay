export const API_CONFIG = {
  gatewayBaseUrl: process.env.EXPO_PUBLIC_GATEWAY_URL ?? 'http://localhost:8082',
  authBaseUrl: process.env.EXPO_PUBLIC_AUTH_URL ?? 'http://localhost:8083',
  walletBaseUrl: process.env.EXPO_PUBLIC_WALLET_URL ?? 'http://localhost:8084',
  tradingBaseUrl: process.env.EXPO_PUBLIC_TRADING_URL ?? 'http://localhost:8085'
} as const;
