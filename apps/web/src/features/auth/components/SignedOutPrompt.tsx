import { ShieldCheck } from 'lucide-react';
import { Button, EmptyState } from '../../../shared/ui';
import { useAuth } from '../hooks/useAuth';

export function SignedOutPrompt() {
  const { signIn } = useAuth();

  return (
    <EmptyState
      action={
        <Button onClick={signIn} variant="primary">
          Sign in with Google
        </Button>
      }
      description="Sign in stays backend-owned through a secure session cookie. Google tokens are never stored in the browser."
      icon={<ShieldCheck aria-hidden="true" size={24} />}
      title="Sign in to unlock your notebook"
    />
  );
}
