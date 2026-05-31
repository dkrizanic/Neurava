import { afterEach, describe, expect, it, vi } from 'vitest';
import { answerQuestion, previewCreateNote, summarizeHistory } from './assistantApi';

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
});

function jsonResponse(body: unknown) {
  return {
    json: () => Promise.resolve(body),
    ok: true,
  } as Response;
}
