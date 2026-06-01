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

  it('keeps answer turns in session history with source references', async () => {
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

    await user.type(screen.getByLabelText(/message/i), 'What did we decide about the API problem?');
    await user.click(screen.getByRole('button', { name: /send/i }));

    await waitFor(() => expect(mockedAnswerQuestion).toHaveBeenCalledWith(
      'What did we decide about the API problem?',
    ));
    expect(await screen.findByLabelText(/assistant answer/i)).toHaveTextContent(/available notebook sources/i);
    expect(screen.getByText('What did we decide about the API problem?')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /source references/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /api decision/i })).toBeInTheDocument();
    expect(screen.getByText(/93% match/i)).toBeInTheDocument();
  });

  it('shows insufficient-context answer message in history', async () => {
    const user = userEvent.setup();
    mockedAnswerQuestion.mockResolvedValue({
      answer: 'I do not have enough source context in this workspace to answer that yet.',
      enoughSourceContext: false,
      sources: [],
    });

    renderWithAuth(<AssistantPage />, authSession);

    const input = screen.getByLabelText(/message/i);
    await user.type(input, 'What happened with quarterly budget?');
    await user.click(screen.getByRole('button', { name: /send/i }));

    expect(await screen.findByLabelText(/assistant answer/i)).toHaveTextContent(/not enough source context/i);
    expect(screen.getByText('What happened with quarterly budget?')).toBeInTheDocument();
    expect(input).toHaveValue('');
  });

  it('shows a recoverable error message and retry for failed answers', async () => {
    const user = userEvent.setup();
    mockedAnswerQuestion
      .mockRejectedValueOnce(new Error('down'))
      .mockResolvedValueOnce({
        answer: 'Based on the available notebook sources, retry worked.',
        enoughSourceContext: true,
        sources: [],
      });

    renderWithAuth(<AssistantPage />, authSession);

    await user.type(screen.getByLabelText(/message/i), 'What did we decide?');
    await user.click(screen.getByRole('button', { name: /send/i }));

    expect(await screen.findByText(/could not be loaded/i)).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /retry last request/i }));
    expect(await screen.findByText(/retry worked/i)).toBeInTheDocument();
  });

  it('asks for clarification without calling retrieval for broad prompts', async () => {
    const user = userEvent.setup();

    renderWithAuth(<AssistantPage />, authSession);

    await user.type(screen.getByLabelText(/message/i), 'API');
    await user.click(screen.getByRole('button', { name: /send/i }));

    expect(screen.getByText('API')).toBeInTheDocument();
    expect(screen.getByText(/tell me a little more/i)).toBeInTheDocument();
    expect(mockedAnswerQuestion).not.toHaveBeenCalled();
  });

  it('routes summary-style requests through the single assistant prompt', async () => {
    const user = userEvent.setup();
    mockedAnswerQuestion.mockResolvedValue({
      answer: 'Here is a short summary of the planning notes.',
      enoughSourceContext: true,
      sources: [],
    });

    renderWithAuth(<AssistantPage />, authSession);

    await user.type(screen.getByLabelText(/message/i), 'Summarize API planning');
    await user.click(screen.getByRole('button', { name: /send/i }));

    await waitFor(() => expect(mockedAnswerQuestion).toHaveBeenCalledWith('Summarize API planning'));
    expect(await screen.findByLabelText(/assistant answer/i)).toHaveTextContent(/short summary/i);
  });
});
