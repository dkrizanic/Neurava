import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { renderWithAuth } from '../../../test/renderWithAuth';
import { searchMemory } from '../api/searchApi';
import { SearchPage } from './SearchPage';

vi.mock('../api/searchApi', () => ({
  searchMemory: vi.fn(),
}));

const mockedSearchMemory = vi.mocked(searchMemory);

const authSession = {
  account: {
    avatarUrl: null,
    displayName: 'Dario Notebook',
    email: 'dario@example.com',
    id: 'account-1',
  },
  activeWorkspace: {
    id: 'workspace-1',
    name: 'Personal',
    type: 'PERSONAL' as const,
  },
  authenticated: true,
};

describe('SearchPage', () => {
  beforeEach(() => {
    mockedSearchMemory.mockReset();
  });

  it('prompts anonymous users to sign in', () => {
    renderWithAuth(<SearchPage />);

    expect(screen.getByRole('heading', { name: /search/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /sign in to unlock your notebook/i })).toBeInTheDocument();
  });

  it('renders weak-fragment search results', async () => {
    const user = userEvent.setup();
    mockedSearchMemory.mockResolvedValue([{
      score: 0.91,
      snippet: 'We talked about the API problem and the migration risk.',
      sourceId: 'note-1',
      sourceType: 'note',
      sourceUpdatedAt: '2026-05-31T12:00:00Z',
      title: 'Planning memory',
    }]);

    renderWithAuth(<SearchPage />, authSession);

    await user.type(screen.getByLabelText(/weak-fragment search/i), 'api problem');
    await user.click(screen.getByRole('button', { name: /search memory/i }));

    await waitFor(() => expect(mockedSearchMemory).toHaveBeenCalledWith('api problem', expect.any(AbortSignal)));
    expect(await screen.findByRole('heading', { name: /planning memory/i })).toBeInTheDocument();
    expect(screen.getByText(/API problem/)).toBeInTheDocument();
    expect(screen.getByText(/91% match/i)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /open details and edit/i })).toHaveAttribute('href', '/notes/note-1');
  });

  it('shows a refinement empty state without clearing the query', async () => {
    const user = userEvent.setup();
    mockedSearchMemory.mockResolvedValue([]);

    renderWithAuth(<SearchPage />, authSession);

    const input = screen.getByLabelText(/weak-fragment search/i);
    await user.type(input, 'quarterly budget');
    await user.click(screen.getByRole('button', { name: /search memory/i }));

    expect(await screen.findByRole('heading', { name: /no memory matches yet/i })).toBeInTheDocument();
    expect(input).toHaveValue('quarterly budget');
  });

  it('shows a recoverable error state when search fails', async () => {
    const user = userEvent.setup();
    mockedSearchMemory.mockRejectedValue(new Error('down'));

    renderWithAuth(<SearchPage />, authSession);

    await user.type(screen.getByLabelText(/weak-fragment search/i), 'api problem');
    await user.click(screen.getByRole('button', { name: /search memory/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/could not be loaded/i);
  });
});
