import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { renderWithAuth } from '../../../test/renderWithAuth';
import { answerQuestion, summarizeHistory } from '../api/assistantApi';
import { AssistantPage } from './AssistantPage';

vi.mock('../api/assistantApi', () => ({
  answerQuestion: vi.fn(),
  summarizeHistory: vi.fn(),
}));

const mockedAnswerQuestion = vi.mocked(answerQuestion);
const mockedSummarizeHistory = vi.mocked(summarizeHistory);

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
    mockedSummarizeHistory.mockReset();
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

  it('renders a structured history summary with sources', async () => {
    const user = userEvent.setup();
    mockedSummarizeHistory.mockResolvedValue({
      enoughSourceContext: true,
      sections: {
        decisions: ['Decision signal in "API planning": We decided to keep problem details stable.'],
        keyEvents: ['From "API planning": We decided to keep problem details stable.'],
        nextActions: ['Next-action signal in "API planning": Next action is to document the contract.'],
        unresolvedItems: ['Open item signal in "API planning": The migration risk remains open.'],
      },
      sources: [{
        id: 'note-1',
        score: 0.94,
        snippet: 'We decided to keep problem details stable. The migration risk remains open.',
        sourceUpdatedAt: '2026-05-31T12:00:00Z',
        title: 'API planning',
        type: 'note',
      }],
    });

    renderWithAuth(<AssistantPage />, authSession);

    await user.click(screen.getByRole('button', { name: /summary/i }));
    await user.type(screen.getByLabelText(/summary topic/i), 'API planning');
    await user.click(screen.getByRole('button', { name: /summarize history/i }));

    await waitFor(() => expect(mockedSummarizeHistory).toHaveBeenCalledWith('API planning', expect.any(AbortSignal)));
    expect(await screen.findByLabelText(/history summary/i)).toHaveTextContent(/key events/i);
    expect(screen.getByText(/Open item signal.*migration risk remains open/i)).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /source references/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /api planning/i })).toBeInTheDocument();
  });

  it('shows insufficient-context state for summaries without clearing the topic', async () => {
    const user = userEvent.setup();
    mockedSummarizeHistory.mockResolvedValue({
      enoughSourceContext: false,
      sections: {
        decisions: [],
        keyEvents: ['Not enough source context is available in this workspace to summarize that topic.'],
        nextActions: [],
        unresolvedItems: [],
      },
      sources: [],
    });

    renderWithAuth(<AssistantPage />, authSession);

    await user.click(screen.getByRole('button', { name: /summary/i }));
    const input = screen.getByLabelText(/summary topic/i);
    await user.type(input, 'quarterly budget');
    await user.click(screen.getByRole('button', { name: /summarize history/i }));

    expect(await screen.findByRole('heading', { name: /not enough source context/i })).toBeInTheDocument();
    expect(input).toHaveValue('quarterly budget');
  });

  it('shows a recoverable error state when summarizing fails', async () => {
    const user = userEvent.setup();
    mockedSummarizeHistory.mockRejectedValue(new Error('down'));

    renderWithAuth(<AssistantPage />, authSession);

    await user.click(screen.getByRole('button', { name: /summary/i }));
    await user.type(screen.getByLabelText(/summary topic/i), 'API planning');
    await user.click(screen.getByRole('button', { name: /summarize history/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/history summary could not be loaded/i);
  });
});
