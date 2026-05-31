import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { renderWithAuth } from '../../../test/renderWithAuth';
import { answerQuestion, previewCreateNote, summarizeHistory } from '../api/assistantApi';
import { AssistantPage } from './AssistantPage';

vi.mock('../api/assistantApi', () => ({
  answerQuestion: vi.fn(),
  previewCreateNote: vi.fn(),
  summarizeHistory: vi.fn(),
}));

const mockedAnswerQuestion = vi.mocked(answerQuestion);
const mockedPreviewCreateNote = vi.mocked(previewCreateNote);
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
    mockedPreviewCreateNote.mockReset();
    mockedSummarizeHistory.mockReset();
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

    await user.type(screen.getByLabelText(/question/i), 'What did we decide about the API problem?');
    await user.click(screen.getByRole('button', { name: /ask assistant/i }));

    await waitFor(() => expect(mockedAnswerQuestion).toHaveBeenCalledWith(
      'What did we decide about the API problem?',
    ));
    expect(await screen.findByLabelText(/source-aware answer/i)).toHaveTextContent(/available notebook sources/i);
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

    const input = screen.getByLabelText(/question/i);
    await user.type(input, 'What happened with quarterly budget?');
    await user.click(screen.getByRole('button', { name: /ask assistant/i }));

    expect(await screen.findByLabelText(/source-aware answer/i)).toHaveTextContent(/not enough source context/i);
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

    await user.type(screen.getByLabelText(/question/i), 'What did we decide?');
    await user.click(screen.getByRole('button', { name: /ask assistant/i }));

    expect(await screen.findByText(/could not be loaded/i)).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /retry last request/i }));
    expect(await screen.findByText(/retry worked/i)).toBeInTheDocument();
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

    await waitFor(() => expect(mockedSummarizeHistory).toHaveBeenCalledWith('API planning'));
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

    expect(await screen.findByLabelText(/history summary/i)).toHaveTextContent(/not enough source context/i);
    expect(screen.getByText('quarterly budget')).toBeInTheDocument();
    expect(input).toHaveValue('');
  });

  it('shows a recoverable error state when summarizing fails', async () => {
    const user = userEvent.setup();
    mockedSummarizeHistory.mockRejectedValue(new Error('down'));

    renderWithAuth(<AssistantPage />, authSession);

    await user.click(screen.getByRole('button', { name: /summary/i }));
    await user.type(screen.getByLabelText(/summary topic/i), 'API planning');
    await user.click(screen.getByRole('button', { name: /summarize history/i }));

    expect(await screen.findByText(/history summary could not be loaded/i)).toBeInTheDocument();
  });

  it('asks for clarification without calling retrieval for broad prompts', async () => {
    const user = userEvent.setup();

    renderWithAuth(<AssistantPage />, authSession);

    await user.type(screen.getByLabelText(/question/i), 'API');
    await user.click(screen.getByRole('button', { name: /ask assistant/i }));

    expect(screen.getByText('API')).toBeInTheDocument();
    expect(screen.getByText(/tell me a little more/i)).toBeInTheDocument();
    expect(mockedAnswerQuestion).not.toHaveBeenCalled();
  });

  it('renders a create-note preview without applying it', async () => {
    const user = userEvent.setup();
    mockedPreviewCreateNote.mockResolvedValue({
      action: 'create_note',
      changeType: 'create',
      entityType: 'note',
      preview: {
        body: 'We need to document the preview contract.',
        tags: 'document,preview,contract',
        title: 'Preview contract',
      },
      summary: 'Create a new note draft in the active workspace.',
    });

    renderWithAuth(<AssistantPage />, authSession);

    await user.click(screen.getByRole('button', { name: /preview/i }));
    await user.type(screen.getByLabelText(/note preview input/i), 'Preview contract');
    await user.click(screen.getByRole('button', { name: /preview note/i }));

    await waitFor(() => expect(mockedPreviewCreateNote).toHaveBeenCalledWith('Preview contract'));
    const preview = await screen.findByLabelText(/ai change preview/i);
    expect(preview).toHaveTextContent(/preview only/i);
    expect(within(preview).getByText('Preview contract')).toBeInTheDocument();
    expect(within(preview).getByText(/document,preview,contract/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /apply comes next/i })).toBeDisabled();
  });

  it('cancels a visible create-note preview', async () => {
    const user = userEvent.setup();
    mockedPreviewCreateNote.mockResolvedValue({
      action: 'create_note',
      changeType: 'create',
      entityType: 'note',
      preview: {
        body: 'Body',
        tags: '',
        title: 'Temporary preview',
      },
      summary: 'Create a new note draft in the active workspace.',
    });

    renderWithAuth(<AssistantPage />, authSession);

    await user.click(screen.getByRole('button', { name: /preview/i }));
    await user.type(screen.getByLabelText(/note preview input/i), 'Temporary preview');
    await user.click(screen.getByRole('button', { name: /preview note/i }));
    expect(await screen.findByLabelText(/ai change preview/i)).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /cancel/i }));
    expect(screen.queryByLabelText(/ai change preview/i)).not.toBeInTheDocument();
  });

  it('shows a recoverable error state when preview generation fails', async () => {
    const user = userEvent.setup();
    mockedPreviewCreateNote.mockRejectedValue(new Error('down'));

    renderWithAuth(<AssistantPage />, authSession);

    await user.click(screen.getByRole('button', { name: /preview/i }));
    await user.type(screen.getByLabelText(/note preview input/i), 'Preview contract');
    await user.click(screen.getByRole('button', { name: /preview note/i }));

    expect(await screen.findByText(/ai change preview could not be loaded/i)).toBeInTheDocument();
  });
});
