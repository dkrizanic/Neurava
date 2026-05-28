import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { renderWithAuth } from '../../../test/renderWithAuth';
import { AuthStatus } from './AuthStatus';

describe('AuthStatus', () => {
  it('starts Google sign-in from signed-out state', async () => {
    const signIn = vi.fn();
    const user = userEvent.setup();

    renderWithAuth(<AuthStatus />, { signIn });

    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(signIn).toHaveBeenCalledTimes(1);
  });

  it('shows signed-in account and signs out', async () => {
    const signOut = vi.fn(async () => undefined);
    const user = userEvent.setup();

    renderWithAuth(<AuthStatus />, {
      account: {
        avatarUrl: null,
        displayName: 'Dario Notebook',
        email: 'dario@example.com',
        id: 'account-1',
      },
      activeWorkspace: {
        id: 'workspace-1',
        name: 'Personal',
        type: 'PERSONAL',
      },
      authenticated: true,
      signOut,
    });

    expect(screen.getByLabelText('Current account')).toBeInTheDocument();
    expect(screen.getByText('Dario Notebook')).toBeInTheDocument();
    expect(screen.getByText('Personal')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /sign out/i }));

    expect(signOut).toHaveBeenCalledTimes(1);
  });
});
