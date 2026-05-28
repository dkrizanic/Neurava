import { screen } from '@testing-library/react';
import { OverviewPage } from './OverviewPage';
import { renderWithAuth } from '../../test/renderWithAuth';

describe('OverviewPage', () => {
  it('renders all top-level section entry points', () => {
    renderWithAuth(<OverviewPage />);

    expect(screen.getByRole('heading', { name: /a calm command center/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /open notes/i })).toHaveAttribute('href', '/notes');
    expect(screen.getByRole('link', { name: /open assistant/i })).toHaveAttribute('href', '/assistant');
    expect(screen.getByRole('link', { name: /open search/i })).toHaveAttribute('href', '/search');
    expect(screen.getByRole('link', { name: /open reminders/i })).toHaveAttribute('href', '/reminders');
    expect(screen.getByRole('link', { name: /open plans/i })).toHaveAttribute('href', '/plans');
    expect(screen.getByRole('link', { name: /open projects/i })).toHaveAttribute('href', '/projects');
    expect(screen.getByRole('link', { name: /open integrations/i })).toHaveAttribute('href', '/integrations');
  });

  it('shows a signed-out prompt when the session is anonymous', () => {
    renderWithAuth(<OverviewPage />);

    expect(screen.getByRole('heading', { name: /sign in to unlock your notebook/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in with google/i })).toBeInTheDocument();
  });

  it('does not show the signed-out prompt for an authenticated Personal Context session', () => {
    renderWithAuth(<OverviewPage />, {
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
    });

    expect(screen.queryByRole('heading', { name: /sign in to unlock your notebook/i })).not.toBeInTheDocument();
  });
});
