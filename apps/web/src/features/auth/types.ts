export type AuthAccount = {
  avatarUrl: string | null;
  displayName: string | null;
  email: string | null;
  id: string | null;
};

export type WorkspaceSummary = {
  id: string;
  name: string;
  type: 'PERSONAL' | 'BUSINESS';
};

export type AuthSession =
  | {
      account: AuthAccount;
      activeWorkspace: WorkspaceSummary;
      authenticated: true;
      workspaceSwitcherAvailable: boolean;
    }
  | {
      account: null;
      activeWorkspace: null;
      authenticated: false;
      workspaceSwitcherAvailable: false;
    };
