import { screen, within } from '@testing-library/react';
import { App } from './App';
import { renderRouteWithAuth } from '../test/renderWithAuth';

const account = {
  avatarUrl: null,
  displayName: 'Dario Notebook',
  email: 'dario@example.com',
  id: 'account-1',
};

const personalWorkspace = {
  id: 'workspace-1',
  name: 'Personal',
  type: 'PERSONAL' as const,
};

describe('App shell', () => {
  it('renders the primary product navigation areas', () => {
    renderRouteWithAuth(<App />, <div>Route content</div>);

    expect(screen.getByLabelText('Primary navigation')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /open notes/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /open assistant/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /open search/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /open reminders/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /open plans/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /open projects/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /open integrations/i })).toBeInTheDocument();
  });

  it('shows the active Personal Context for a signed-in user without a switcher', () => {
    renderRouteWithAuth(<App />, <div>Route content</div>, {
      account,
      activeWorkspace: personalWorkspace,
      authenticated: true,
    });

    expect(screen.getByRole('heading', { name: /personal workspace/i })).toBeInTheDocument();
    expect(screen.getByText('Dario Notebook')).toBeInTheDocument();
    expect(within(screen.getByLabelText('Current account')).getByText('Personal')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /switch/i })).not.toBeInTheDocument();
  });

  it('shows the workspace switch control only when multiple contexts are available', () => {
    renderRouteWithAuth(<App />, <div>Route content</div>, {
      account,
      activeWorkspace: personalWorkspace,
      authenticated: true,
      workspaceSwitcherAvailable: true,
    });

    expect(screen.getByRole('button', { name: /switch/i })).toBeInTheDocument();
  });
});
