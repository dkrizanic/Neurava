import { afterEach, describe, expect, it, vi } from 'vitest';
import { applyGrammarFix, previewGrammarFix } from './noteAiApi';

describe('noteAiApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('previews grammar fixes through the AI preview endpoint when available', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({
      preview: {
        currentBody: "i dont recieve teh update",
        noteId: '11111111-1111-1111-1111-111111111111',
        proposedBody: "I don't receive the update",
      },
    }));

    await expect(previewGrammarFix({
      body: 'i dont recieve teh update',
      noteId: '11111111-1111-1111-1111-111111111111',
      title: 'Grammar note',
    })).resolves.toMatchObject({
      proposedBody: "I don't receive the update",
    });

    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/v1/ai/action-previews', expect.objectContaining({
      method: 'POST',
    }));
  });

  it('throws when grammar preview endpoint fails', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(errorResponse(400));

    await expect(previewGrammarFix({
      body: 'i dont recieve teh update',
      noteId: '11111111-1111-1111-1111-111111111111',
      title: 'Grammar note',
    })).rejects.toThrow(/unable to preview grammar fix/i);
  });

  it('falls back to normal note update when AI apply endpoint fails', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(errorResponse(400))
      .mockResolvedValueOnce(jsonResponse({
        body: "I don't receive the update",
        id: '11111111-1111-1111-1111-111111111111',
        noteDate: '2026-06-02',
        title: 'Grammar note',
      }));

    await expect(applyGrammarFix({
      body: 'i dont recieve teh update',
      noteId: '11111111-1111-1111-1111-111111111111',
      proposedBody: "I don't receive the update",
      title: 'Grammar note',
    })).resolves.toMatchObject({
      body: "I don't receive the update",
    });

    expect(fetchMock).toHaveBeenNthCalledWith(1, 'http://localhost:8080/api/v1/ai/action-applications', expect.objectContaining({
      method: 'POST',
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, 'http://localhost:8080/api/v1/notes/11111111-1111-1111-1111-111111111111', expect.objectContaining({
      method: 'PATCH',
    }));
  });
});

function jsonResponse(body: unknown) {
  return {
    json: () => Promise.resolve(body),
    ok: true,
    status: 200,
  } as Response;
}

function errorResponse(status: number) {
  return {
    ok: false,
    status,
  } as Response;
}
