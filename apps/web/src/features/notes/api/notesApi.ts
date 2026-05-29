import { apiBaseUrl } from '../../../shared/api/httpClient';
import type { Note } from '../types';

export async function fetchNotes(signal?: AbortSignal): Promise<Note[]> {
  const response = await fetch(`${apiBaseUrl}/api/v1/notes`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
    signal,
  });

  if (!response.ok) {
    throw new Error('Unable to load notes');
  }

  return response.json() as Promise<Note[]>;
}

export async function createNote(input: { body: string; title: string }): Promise<Note> {
  const response = await fetch(`${apiBaseUrl}/api/v1/notes`, {
    body: JSON.stringify(input),
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'POST',
  });

  if (!response.ok) {
    throw new Error('Unable to create note');
  }

  return response.json() as Promise<Note>;
}

export async function updateNote(noteId: string, input: { body: string; title: string }): Promise<Note> {
  const response = await fetch(`${apiBaseUrl}/api/v1/notes/${noteId}`, {
    body: JSON.stringify(input),
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'PATCH',
  });

  if (!response.ok) {
    throw new Error('Unable to update note');
  }

  return response.json() as Promise<Note>;
}
