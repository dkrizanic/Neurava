import { render } from '@testing-library/react';
import type { ReactElement } from 'react';
import { MemoryRouter, Route, Routes } from 'react-router';
import { AuthContext } from '../app/providers/AuthProvider';
import type { AuthAccount, WorkspaceSummary } from '../features/auth/types';

type AuthRenderOptions = {
  account?: AuthAccount | null;
  activeWorkspace?: WorkspaceSummary | null;
  authenticated?: boolean;
  error?: string | null;
  initialPath?: string;
  isLoading?: boolean;
  refresh?: () => Promise<void>;
  signIn?: () => void;
  signOut?: () => Promise<void>;
  workspaceSwitcherAvailable?: boolean;
};

export function renderWithAuth(ui: ReactElement, options: AuthRenderOptions = {}) {
  return render(
    <AuthContext
      value={{
        account: options.account ?? null,
        activeWorkspace: options.activeWorkspace ?? null,
        authenticated: options.authenticated ?? false,
        error: options.error ?? null,
        isLoading: options.isLoading ?? false,
        refresh: options.refresh ?? (async () => undefined),
        signIn: options.signIn ?? (() => undefined),
        signOut: options.signOut ?? (async () => undefined),
        workspaceSwitcherAvailable: options.workspaceSwitcherAvailable ?? false,
      }}
    >
      <MemoryRouter initialEntries={[options.initialPath ?? '/']}>{ui}</MemoryRouter>
    </AuthContext>,
  );
}

export function renderRouteWithAuth(ui: ReactElement, outlet: ReactElement, options: AuthRenderOptions = {}) {
  return renderWithAuth(
    <Routes>
      <Route element={ui} path="/">
        <Route element={outlet} index />
      </Route>
    </Routes>,
    options,
  );
}
