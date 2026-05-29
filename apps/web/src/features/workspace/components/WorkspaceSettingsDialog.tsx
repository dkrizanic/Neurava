import { Building2 } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, Dialog, Field } from '../../../shared/ui';
import { registerCompany } from '../api/companyApi';

type WorkspaceSettingsDialogProps = {
  onClose: () => void;
  open: boolean;
};

export function WorkspaceSettingsDialog({ onClose, open }: WorkspaceSettingsDialogProps) {
  const { activeWorkspace, authenticated, refresh } = useAuth();
  const [companyName, setCompanyName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [registeredCompanyName, setRegisteredCompanyName] = useState<string | null>(null);

  async function submitRegistration(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmedName = companyName.trim();

    if (!trimmedName) {
      setError('Company name is required.');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const registration = await registerCompany(trimmedName);
      setRegisteredCompanyName(registration.company.name);
      setCompanyName('');
      await refresh();
    } catch {
      setError('Company could not be registered.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Dialog
      description={activeWorkspace ? `${activeWorkspace.name} context` : undefined}
      onClose={onClose}
      open={open}
      title="Workspace settings"
    >
      <div className="workspace-settings">
        {authenticated ? (
          <form className="workspace-settings__form" onSubmit={submitRegistration}>
            <Field
              autoComplete="organization"
              error={error ?? undefined}
              label="Company name"
              maxLength={160}
              name="companyName"
              onChange={(event) => setCompanyName(event.target.value)}
              placeholder="Acme Labs"
              value={companyName}
            />
            <Button
              disabled={isSubmitting}
              icon={<Building2 aria-hidden="true" size={18} />}
              type="submit"
              variant="primary"
            >
              {isSubmitting ? 'Registering' : 'Register company'}
            </Button>
          </form>
        ) : (
          <p className="workspace-settings__notice">Sign in to register a company.</p>
        )}

        {registeredCompanyName ? (
          <p className="workspace-settings__success">{registeredCompanyName} is available as a Business Context.</p>
        ) : null}

      </div>
    </Dialog>
  );
}
