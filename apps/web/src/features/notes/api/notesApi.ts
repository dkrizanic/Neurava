import { apiBaseUrl } from '../../../shared/api/httpClient';
import type { Note } from '../types';

export type NoteFilters = {
  archived?: boolean;
  favorite?: boolean;
  pinned?: boolean;
  q?: string;
  tag?: string;
};

function notesUrl(filters: NoteFilters = {}) {
  const params = new URLSearchParams();
  if (filters.archived) params.set('archived', 'true');
  if (filters.favorite) params.set('favorite', 'true');
  if (filters.pinned) params.set('pinned', 'true');
  if (filters.q) params.set('q', filters.q);
  if (filters.tag) params.set('tag', filters.tag);
  const query = params.toString();
  return `${apiBaseUrl}/api/v1/notes${query ? `?${query}` : ''}`;
}

export async function fetchNotes(filters?: NoteFilters, signal?: AbortSignal): Promise<Note[]> {
  const response = await fetch(notesUrl(filters), {
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

export async function organizeNote(
  noteId: string,
  input: Pick<Note, 'editorMode' | 'favorite' | 'linkedResources' | 'pinned' | 'tags'>,
): Promise<Note> {
  const response = await fetch(`${apiBaseUrl}/api/v1/notes/${noteId}/organization`, {
    body: JSON.stringify(input),
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'PATCH',
  });

  if (!response.ok) {
    throw new Error('Unable to organize note');
  }

  return response.json() as Promise<Note>;
}

export async function archiveNote(noteId: string): Promise<Note> {
  return patchNoteState(noteId, 'archive');
}

export async function restoreNote(noteId: string): Promise<Note> {
  return patchNoteState(noteId, 'restore');
}

async function patchNoteState(noteId: string, action: 'archive' | 'restore'): Promise<Note> {
  const response = await fetch(`${apiBaseUrl}/api/v1/notes/${noteId}/${action}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
    method: 'PATCH',
  });

  if (!response.ok) {
    throw new Error('Unable to update note state');
  }

  return response.json() as Promise<Note>;
}
