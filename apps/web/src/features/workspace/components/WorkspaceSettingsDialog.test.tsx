import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { renderWithAuth } from '../../../test/renderWithAuth';
import { registerCompany } from '../api/companyApi';
import { WorkspaceSettingsDialog } from './WorkspaceSettingsDialog';

vi.mock('../api/companyApi', () => ({
  registerCompany: vi.fn(),
}));

const mockedRegisterCompany = vi.mocked(registerCompany);

describe('WorkspaceSettingsDialog', () => {
  beforeEach(() => {
    mockedRegisterCompany.mockReset();
  });

  it('shows company registration for a signed-in Personal Context user', () => {
    renderWithAuth(<WorkspaceSettingsDialog onClose={() => undefined} open />, {
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

    expect(screen.getByRole('dialog', { name: /workspace settings/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/company name/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /register company/i })).toBeInTheDocument();
  });

  it('registers a company and refreshes session state', async () => {
    const refresh = vi.fn(async () => undefined);
    const user = userEvent.setup();
    mockedRegisterCompany.mockResolvedValue({
      businessWorkspace: {
        id: 'workspace-2',
        name: 'Acme Labs',
        type: 'BUSINESS',
      },
      company: {
        id: 'company-1',
        name: 'Acme Labs',
      },
    });

    renderWithAuth(<WorkspaceSettingsDialog onClose={() => undefined} open />, {
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
      refresh,
    });

    await user.type(screen.getByLabelText(/company name/i), 'Acme Labs');
    await user.click(screen.getByRole('button', { name: /register company/i }));

    await waitFor(() => expect(mockedRegisterCompany).toHaveBeenCalledWith('Acme Labs'));
    expect(refresh).toHaveBeenCalledTimes(1);
    expect(screen.getByText(/acme labs is available as a business context/i)).toBeInTheDocument();
  });
});
