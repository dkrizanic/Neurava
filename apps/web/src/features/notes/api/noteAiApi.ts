import { apiBaseUrl } from '../../../shared/api/httpClient';
import { updateNote } from './notesApi';
import type { Note } from '../types';

export type GrammarFixPreview = {
  noteId: string;
  currentBody: string;
  proposedBody: string;
};

export type PrettifiedNoteDraft = {
  body: string;
  linkedResources: string;
  tags: string;
  title: string;
};

export async function previewGrammarFix(
  input: { body: string; noteId: string; title: string },
  signal?: AbortSignal,
): Promise<GrammarFixPreview> {
  const response = await fetch(`${apiBaseUrl}/api/v1/ai/action-previews`, {
    body: JSON.stringify({ action: 'fix_note_grammar', input }),
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'POST',
    signal,
  });

  if (!response.ok) {
    throw new Error('Unable to preview grammar fix');
  }

  const result = await response.json() as { preview: GrammarFixPreview };
  return result.preview;
}

export async function applyGrammarFix(
  input: { body: string; noteId: string; proposedBody: string; title: string },
  signal?: AbortSignal,
): Promise<Note> {
  try {
    const response = await fetch(`${apiBaseUrl}/api/v1/ai/action-applications`, {
      body: JSON.stringify({ action: 'fix_note_grammar', input }),
      credentials: 'include',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      method: 'POST',
      signal,
    });

    if (response.ok) {
      const result = await response.json() as { entity: Note };
      return result.entity;
    }

    if (response.status === 401 || response.status === 403) {
      throw new Error('Unable to apply grammar fix');
    }
  } catch (error) {
    if ((error as Error).name === 'AbortError') {
      throw error;
    }
  }

  return updateNote(input.noteId, {
    body: input.proposedBody,
    title: input.title,
  });
}

export async function previewPrettifiedNoteDraft(
  text: string,
  signal?: AbortSignal,
): Promise<PrettifiedNoteDraft> {
  const response = await fetch(`${apiBaseUrl}/api/v1/ai/action-previews`, {
    body: JSON.stringify({ action: 'prettify_note_draft', input: { text } }),
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'POST',
    signal,
  });

  if (!response.ok) {
    throw new Error('Unable to prettify note draft');
  }

  const result = await response.json() as {
    entityType: string;
    preview: PrettifiedNoteDraft;
  };

  if (result.entityType !== 'note') {
    throw new Error('Unable to prettify note draft');
  }

  return result.preview;
}
