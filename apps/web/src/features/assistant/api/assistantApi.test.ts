import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  answerQuestion,
  applyCreateNotePreview,
  fetchAiActionHistory,
  previewCreateNote,
  summarizeHistory,
} from './assistantApi';

describe('assistantApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('executes answer questions through the assistant action endpoint', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({
      action: 'answer_question',
      result: {
        answer: 'Answer from sources.',
        enoughSourceContext: true,
        sources: [],
      },
    }));

    await expect(answerQuestion('What did we decide?')).resolves.toMatchObject({
      answer: 'Answer from sources.',
      enoughSourceContext: true,
    });
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/v1/ai/actions', expect.objectContaining({
      body: JSON.stringify({ action: 'answer_question', input: { question: 'What did we decide?' } }),
      method: 'POST',
    }));
  });

  it('executes summaries through the assistant action endpoint', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({
      action: 'summarize_history',
      result: {
        enoughSourceContext: true,
        sections: {
          decisions: [],
          keyEvents: ['Event'],
          nextActions: [],
          unresolvedItems: [],
        },
        sources: [],
      },
    }));

    await expect(summarizeHistory('API planning')).resolves.toMatchObject({
      sections: { keyEvents: ['Event'] },
    });
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/v1/ai/actions', expect.objectContaining({
      body: JSON.stringify({ action: 'summarize_history', input: { topic: 'API planning' } }),
      method: 'POST',
    }));
  });

  it('requests create-note previews through the assistant preview endpoint', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({
      action: 'create_note',
      changeType: 'create',
      entityType: 'note',
      preview: {
        body: 'Clean body',
        tags: 'clean,body',
        title: 'Clean title',
      },
      summary: 'Create a new note draft in the active workspace.',
    }));

    await expect(previewCreateNote('messy input')).resolves.toMatchObject({
      preview: { title: 'Clean title' },
    });
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/v1/ai/action-previews', expect.objectContaining({
      body: JSON.stringify({ action: 'create_note', input: { text: 'messy input' } }),
      method: 'POST',
    }));
  });

  it('applies create-note previews through the assistant application endpoint', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({
      action: 'create_note',
      changeType: 'create',
      entity: {
        body: 'Clean body',
        id: 'note-1',
        noteDate: '2026-06-01',
        tags: 'clean,body',
        title: 'Clean title',
      },
      entityType: 'note',
      summary: 'Created note "Clean title".',
    }));

    await expect(applyCreateNotePreview({
      body: 'Clean body',
      noteDate: '2026-06-01',
      tags: 'clean,body',
      title: 'Clean title',
    })).resolves.toMatchObject({
      entity: { title: 'Clean title' },
    });
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/v1/ai/action-applications', expect.objectContaining({
      body: JSON.stringify({
        action: 'create_note',
        input: {
          body: 'Clean body',
          noteDate: '2026-06-01',
          tags: 'clean,body',
          title: 'Clean title',
        },
      }),
      method: 'POST',
    }));
  });

  it('loads AI action history from the history endpoint', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse([{
      action: 'create_note',
      changeType: 'create',
      createdAt: '2026-06-01T15:00:00Z',
      currentState: '{"title":"Clean title"}',
      entityId: 'note-1',
      entityType: 'note',
      id: 'history-1',
      ownerAccountId: 'account-1',
      previousState: null,
      summary: 'Created note "Clean title".',
      workspaceContextId: 'workspace-1',
    }]));

    await expect(fetchAiActionHistory()).resolves.toMatchObject([
      { summary: 'Created note "Clean title".' },
    ]);
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/v1/ai/action-history', expect.objectContaining({
      credentials: 'include',
    }));
  });
});

function jsonResponse(body: unknown) {
  return {
    json: () => Promise.resolve(body),
    ok: true,
  } as Response;
}
