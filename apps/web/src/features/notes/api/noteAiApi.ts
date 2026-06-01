import { apiBaseUrl } from '../../../shared/api/httpClient';
import type { Note } from '../types';

export type GrammarFixPreview = {
  noteId: string;
  currentBody: string;
  proposedBody: string;
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

  if (!response.ok) {
    throw new Error('Unable to apply grammar fix');
  }

  const result = await response.json() as { entity: Note };
  return result.entity;
}
