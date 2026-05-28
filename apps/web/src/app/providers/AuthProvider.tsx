import { createContext, useCallback, useEffect, useMemo, useState, type PropsWithChildren } from 'react';
import { fetchCurrentSession, logout, startGoogleSignIn } from '../../features/auth/api/authApi';
import type { AuthAccount, AuthSession, WorkspaceSummary } from '../../features/auth/types';

type AuthContextValue = {
  account: AuthAccount | null;
  activeWorkspace: WorkspaceSummary | null;
  authenticated: boolean;
  error: string | null;
  isLoading: boolean;
  refresh: () => Promise<void>;
  signIn: () => void;
  signOut: () => Promise<void>;
  workspaceSwitcherAvailable: boolean;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

const anonymousSession: AuthSession = {
  account: null,
  activeWorkspace: null,
  authenticated: false,
  workspaceSwitcherAvailable: false,
};

export function AuthProvider({ children }: PropsWithChildren) {
  const [session, setSession] = useState<AuthSession>(anonymousSession);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    const controller = new AbortController();
    setIsLoading(true);
    setError(null);

    try {
      setSession(await fetchCurrentSession(controller.signal));
    } catch {
      setSession(anonymousSession);
      setError('Session could not be loaded.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const signOut = useCallback(async () => {
    await logout();
    setSession(anonymousSession);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      account: session.account,
      activeWorkspace: session.activeWorkspace,
      authenticated: session.authenticated,
      error,
      isLoading,
      refresh,
      signIn: startGoogleSignIn,
      signOut,
      workspaceSwitcherAvailable: session.workspaceSwitcherAvailable,
    }),
    [
      error,
      isLoading,
      refresh,
      session.account,
      session.activeWorkspace,
      session.authenticated,
      session.workspaceSwitcherAvailable,
      signOut,
    ],
  );

  return <AuthContext value={value}>{children}</AuthContext>;
}
