export type AuthAccount = {
  avatarUrl: string | null;
  displayName: string | null;
  email: string | null;
  id: string | null;
};

export type AuthSession =
  | {
      account: AuthAccount;
      authenticated: true;
    }
  | {
      account: null;
      authenticated: false;
    };
