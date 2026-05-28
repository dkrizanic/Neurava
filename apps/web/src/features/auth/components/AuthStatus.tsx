import { LogIn, LogOut } from 'lucide-react';
import { Button } from '../../../shared/ui';
import { useAuth } from '../hooks/useAuth';

export function AuthStatus() {
  const { account, authenticated, isLoading, signIn, signOut } = useAuth();

  if (isLoading) {
    return <span className="auth-status auth-status--muted">Checking session</span>;
  }

  if (!authenticated || !account) {
    return (
      <Button icon={<LogIn aria-hidden="true" size={18} />} onClick={signIn} variant="primary">
        Sign in
      </Button>
    );
  }

  return (
    <div className="auth-status" aria-label="Current account">
      <span className="auth-status__avatar" aria-hidden="true">
        {account.avatarUrl ? <img alt="" src={account.avatarUrl} /> : initials(account.displayName ?? account.email)}
      </span>
      <span className="auth-status__text">
        <strong>{account.displayName ?? account.email ?? 'Signed in'}</strong>
        {account.email ? <span>{account.email}</span> : null}
      </span>
      <Button
        aria-label="Sign out"
        icon={<LogOut aria-hidden="true" size={18} />}
        onClick={signOut}
        variant="ghost"
      >
        Sign out
      </Button>
    </div>
  );
}

function initials(value: string | null) {
  if (!value) {
    return 'U';
  }

  return value
    .split(/\s|@/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('');
}
