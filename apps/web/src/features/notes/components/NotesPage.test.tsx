import { fireEvent, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { renderWithAuth } from '../../../test/renderWithAuth';
import { archiveNote, createNote, fetchNotes, organizeNote, restoreNote, updateNote } from '../api/notesApi';
import { NotesPage } from './NotesPage';

vi.mock('../api/notesApi', () => ({
  createNote: vi.fn(),
  fetchNotes: vi.fn(),
  archiveNote: vi.fn(),
  organizeNote: vi.fn(),
  restoreNote: vi.fn(),
  updateNote: vi.fn(),
}));

const mockedArchiveNote = vi.mocked(archiveNote);
const mockedCreateNote = vi.mocked(createNote);
const mockedFetchNotes = vi.mocked(fetchNotes);
const mockedOrganizeNote = vi.mocked(organizeNote);
const mockedRestoreNote = vi.mocked(restoreNote);
const mockedUpdateNote = vi.mocked(updateNote);

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

describe('NotesPage', () => {
  beforeEach(() => {
    mockedCreateNote.mockReset();
    mockedFetchNotes.mockReset();
    mockedArchiveNote.mockReset();
    mockedOrganizeNote.mockReset();
    mockedRestoreNote.mockReset();
    mockedUpdateNote.mockReset();
    vi.useRealTimers();
  });

  it('prompts anonymous users to sign in', () => {
    renderWithAuth(<NotesPage />);

    expect(screen.getByRole('heading', { name: /notes/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /sign in to unlock your notebook/i })).toBeInTheDocument();
  });

  it('shows an empty state when the active workspace has no notes', async () => {
    mockedFetchNotes.mockResolvedValue([]);

    renderWithAuth(<NotesPage />, authSession);

    await waitFor(() => expect(mockedFetchNotes).toHaveBeenCalledTimes(1));
    expect(screen.getByRole('heading', { name: /no notes yet/i })).toBeInTheDocument();
  });

  it('creates a note and adds it to the workspace list', async () => {
    const user = userEvent.setup();
    mockedFetchNotes.mockResolvedValue([]);
    mockedCreateNote.mockResolvedValue({
      body: 'A useful memory',
      archivedAt: null,
      createdAt: '2026-05-30T00:00:00Z',
      editorMode: 'RICH_TEXT',
      favorite: false,
      id: 'note-1',
      linkedResources: '',
      ownerAccountId: 'account-1',
      pinned: false,
      tags: '',
      title: 'First note',
      updatedAt: '2026-05-30T00:00:00Z',
      workspaceContextId: 'workspace-1',
    });

    renderWithAuth(<NotesPage />, authSession);

    await user.type(screen.getByLabelText(/title/i), 'First note');
    await user.type(screen.getByLabelText(/body/i), 'A useful memory');
    await user.click(screen.getByRole('button', { name: /create note/i }));

    await waitFor(() => expect(mockedCreateNote).toHaveBeenCalledWith({
      body: 'A useful memory',
      title: 'First note',
    }));
    expect(screen.getByDisplayValue('First note')).toBeInTheDocument();
    expect(screen.getByDisplayValue('A useful memory')).toBeInTheDocument();
  });

  it('autosaves edits and shows saved feedback', async () => {
    mockedFetchNotes.mockResolvedValue([{
      archivedAt: null,
      body: 'Old body',
      createdAt: '2026-05-30T00:00:00Z',
      editorMode: 'RICH_TEXT',
      favorite: false,
      id: 'note-1',
      linkedResources: '',
      ownerAccountId: 'account-1',
      pinned: false,
      tags: '',
      title: 'Editable note',
      updatedAt: '2026-05-30T00:00:00Z',
      workspaceContextId: 'workspace-1',
    }]);
    mockedUpdateNote.mockResolvedValue({
      body: 'Updated body',
      archivedAt: null,
      createdAt: '2026-05-30T00:00:00Z',
      editorMode: 'RICH_TEXT',
      favorite: false,
      id: 'note-1',
      linkedResources: '',
      ownerAccountId: 'account-1',
      pinned: false,
      tags: '',
      title: 'Editable note',
      updatedAt: '2026-05-30T00:01:00Z',
      workspaceContextId: 'workspace-1',
    });

    renderWithAuth(<NotesPage />, authSession);

    const body = await screen.findByLabelText(/body for editable note/i);
    fireEvent.change(body, { target: { value: 'Updated body' } });

    expect(screen.getByText('Saving')).toBeInTheDocument();

    await waitFor(() => expect(mockedUpdateNote).toHaveBeenLastCalledWith('note-1', {
      body: 'Updated body',
      title: 'Editable note',
    }), { timeout: 2000 });
    expect(await screen.findByText('Saved')).toBeInTheDocument();
  });

  it('keeps typed content visible when autosave fails', async () => {
    mockedFetchNotes.mockResolvedValue([{
      archivedAt: null,
      body: 'Old body',
      createdAt: '2026-05-30T00:00:00Z',
      editorMode: 'RICH_TEXT',
      favorite: false,
      id: 'note-1',
      linkedResources: '',
      ownerAccountId: 'account-1',
      pinned: false,
      tags: '',
      title: 'Editable note',
      updatedAt: '2026-05-30T00:00:00Z',
      workspaceContextId: 'workspace-1',
    }]);
    mockedUpdateNote.mockRejectedValue(new Error('nope'));

    renderWithAuth(<NotesPage />, authSession);

    const title = await screen.findByLabelText(/title for editable note/i);
    fireEvent.change(title, { target: { value: 'Local draft' } });

    await waitFor(() => expect(mockedUpdateNote).toHaveBeenCalled(), { timeout: 2000 });
    expect(screen.getByDisplayValue('Local draft')).toBeInTheDocument();
    expect(await screen.findByText('Save failed')).toBeInTheDocument();
  });

  it('organizes, filters, archives, and restores notes', async () => {
    const user = userEvent.setup();
    const note = {
      archivedAt: null,
      body: 'Project planning details',
      createdAt: '2026-05-30T00:00:00Z',
      editorMode: 'RICH_TEXT' as const,
      favorite: false,
      id: 'note-1',
      linkedResources: '',
      ownerAccountId: 'account-1',
      pinned: false,
      tags: '',
      title: 'Organized note',
      updatedAt: '2026-05-30T00:00:00Z',
      workspaceContextId: 'workspace-1',
    };
    mockedFetchNotes.mockResolvedValue([note]);
    mockedOrganizeNote.mockResolvedValue({ ...note, favorite: true, pinned: true, tags: 'planning', editorMode: 'MARKDOWN' });
    mockedArchiveNote.mockResolvedValue({ ...note, archivedAt: '2026-05-30T00:01:00Z' });
    mockedRestoreNote.mockResolvedValue(note);

    renderWithAuth(<NotesPage />, authSession);

    await user.click(await screen.findByRole('button', { name: /mark favorite/i }));
    await waitFor(() => expect(mockedOrganizeNote).toHaveBeenCalledWith('note-1', expect.objectContaining({ favorite: true })));

    await user.click(screen.getByRole('button', { name: /pinned/i }));
    await user.selectOptions(screen.getByLabelText(/mode/i), 'MARKDOWN');
    await user.type(screen.getByLabelText(/search/i), 'planning');

    await waitFor(() => expect(mockedFetchNotes).toHaveBeenLastCalledWith(expect.objectContaining({ q: 'planning' }), expect.any(AbortSignal)));

    await user.click(screen.getByRole('button', { name: /archive/i }));
    await waitFor(() => expect(mockedArchiveNote).toHaveBeenCalledWith('note-1'));
  });
});
