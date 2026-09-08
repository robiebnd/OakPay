import * as SecureStore from 'expo-secure-store';
import { router } from 'expo-router';
import { PropsWithChildren, createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { authApi, LoginRequest, RegisterRequest, TokenResponse, UserResponse } from '../lib/api';

const ACCESS_TOKEN_KEY = 'oakpay.accessToken';
const REFRESH_TOKEN_KEY = 'oakpay.refreshToken';
const USER_KEY = 'oakpay.user';

type AuthContextValue = {
  accessToken: string | null;
  user: UserResponse | null;
  isLoading: boolean;
  signIn: (request: LoginRequest) => Promise<void>;
  signUp: (request: RegisterRequest) => Promise<void>;
  signOut: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

async function saveSession(tokens: TokenResponse, user?: UserResponse) {
  await SecureStore.setItemAsync(ACCESS_TOKEN_KEY, tokens.accessToken);
  await SecureStore.setItemAsync(REFRESH_TOKEN_KEY, tokens.refreshToken);
  if (user) await SecureStore.setItemAsync(USER_KEY, JSON.stringify(user));
}

export function AuthProvider({ children }: PropsWithChildren) {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [user, setUser] = useState<UserResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function restore() {
      try {
        const [storedAccessToken, storedRefreshToken, storedUser] = await Promise.all([
          SecureStore.getItemAsync(ACCESS_TOKEN_KEY),
          SecureStore.getItemAsync(REFRESH_TOKEN_KEY),
          SecureStore.getItemAsync(USER_KEY),
        ]);

        let cachedUser: UserResponse | undefined;
        if (storedUser) {
          try {
            cachedUser = JSON.parse(storedUser) as UserResponse;
            setUser(cachedUser);
          } catch {
            await SecureStore.deleteItemAsync(USER_KEY);
          }
        }

        if (storedAccessToken) {
          setAccessToken(storedAccessToken);
          try {
            const currentUser = await authApi.me(storedAccessToken);
            setUser(currentUser);
            await SecureStore.setItemAsync(USER_KEY, JSON.stringify(currentUser));
          } catch {
            // Keep the cached profile if the API is temporarily unavailable.
          }
          return;
        }

        if (storedRefreshToken) {
          const tokens = await authApi.refresh(storedRefreshToken);
          let currentUser = cachedUser;
          try {
            currentUser = await authApi.me(tokens.accessToken);
          } catch {
            // Keep cached profile if profile refresh is temporarily unavailable.
          }
          await saveSession(tokens, currentUser);
          setAccessToken(tokens.accessToken);
          if (currentUser) setUser(currentUser);
        }
      } catch {
        await clearSession();
      } finally {
        setIsLoading(false);
      }
    }
    restore();
  }, []);

  const signIn = useCallback(async (request: LoginRequest) => {
    const tokens = await authApi.login(request);
    const currentUser = await authApi.me(tokens.accessToken);
    await saveSession(tokens, currentUser);
    setAccessToken(tokens.accessToken);
    setUser(currentUser);
    router.replace('/(tabs)');
  }, []);

  const signUp = useCallback(async (request: RegisterRequest) => {
    const registeredUser = await authApi.register(request);
    setUser(registeredUser);
    await SecureStore.setItemAsync(USER_KEY, JSON.stringify(registeredUser));
    router.replace({ pathname: '/(auth)/login', params: { registered: '1' } });
  }, []);

  const signOut = useCallback(async () => {
    try {
      const refreshToken = await SecureStore.getItemAsync(REFRESH_TOKEN_KEY);
      if (refreshToken) await authApi.logout(refreshToken);
    } catch {
      // Local logout still succeeds if the server is unavailable.
    } finally {
      await clearSession();
      setAccessToken(null);
      setUser(null);
      router.replace('/(auth)/login');
    }
  }, []);

  const value = useMemo(
    () => ({ accessToken, user, isLoading, signIn, signUp, signOut }),
    [accessToken, user, isLoading, signIn, signUp, signOut],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

async function clearSession() {
  await Promise.all([
    SecureStore.deleteItemAsync(ACCESS_TOKEN_KEY),
    SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY),
    SecureStore.deleteItemAsync(USER_KEY),
  ]);
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}
