import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { renderWithAuth } from '../../../test/renderWithAuth';
import { answerQuestion } from '../api/assistantApi';
import { AssistantPage } from './AssistantPage';

vi.mock('../api/assistantApi', () => ({
  answerQuestion: vi.fn(),
}));

const mockedAnswerQuestion = vi.mocked(answerQuestion);

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

describe('AssistantPage', () => {
  beforeEach(() => {
    mockedAnswerQuestion.mockReset();
  });

  it('prompts anonymous users to sign in', () => {
    renderWithAuth(<AssistantPage />);

    expect(screen.getByRole('heading', { name: /assistant/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /sign in to unlock your notebook/i })).toBeInTheDocument();
  });

  it('renders answer text separately from source references', async () => {
    const user = userEvent.setup();
    mockedAnswerQuestion.mockResolvedValue({
      answer: 'Based on the available notebook sources, the API problem used stable problem details.',
      enoughSourceContext: true,
      sources: [{
        id: 'note-1',
        score: 0.93,
        snippet: 'We decided the API problem should use stable problem details.',
        sourceUpdatedAt: '2026-05-31T12:00:00Z',
        title: 'API decision',
        type: 'note',
      }],
    });

    renderWithAuth(<AssistantPage />, authSession);

    await user.type(screen.getByLabelText(/question/i), 'What did we decide about the API problem?');
    await user.click(screen.getByRole('button', { name: /ask assistant/i }));

    await waitFor(() => expect(mockedAnswerQuestion).toHaveBeenCalledWith(
      'What did we decide about the API problem?',
      expect.any(AbortSignal),
    ));
    expect(await screen.findByLabelText(/source-aware answer/i)).toHaveTextContent(/available notebook sources/i);
    expect(screen.getByRole('heading', { name: /source references/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /api decision/i })).toBeInTheDocument();
    expect(screen.getByText(/93% match/i)).toBeInTheDocument();
  });

  it('shows insufficient-context state without clearing the question', async () => {
    const user = userEvent.setup();
    mockedAnswerQuestion.mockResolvedValue({
      answer: 'I do not have enough source context in this workspace to answer that yet.',
      enoughSourceContext: false,
      sources: [],
    });

    renderWithAuth(<AssistantPage />, authSession);

    const input = screen.getByLabelText(/question/i);
    await user.type(input, 'What happened with quarterly budget?');
    await user.click(screen.getByRole('button', { name: /ask assistant/i }));

    expect(await screen.findByRole('heading', { name: /not enough source context/i })).toBeInTheDocument();
    expect(input).toHaveValue('What happened with quarterly budget?');
  });

  it('shows a recoverable error state when answering fails', async () => {
    const user = userEvent.setup();
    mockedAnswerQuestion.mockRejectedValue(new Error('down'));

    renderWithAuth(<AssistantPage />, authSession);

    await user.type(screen.getByLabelText(/question/i), 'What did we decide?');
    await user.click(screen.getByRole('button', { name: /ask assistant/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/could not be loaded/i);
  });
});
