import { fireEvent, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router';
import { vi } from 'vitest';
import { renderWithAuth } from '../../../test/renderWithAuth';
import { archiveNote, createNote, fetchNote, fetchNotes, organizeNote, restoreNote, updateNote } from '../api/notesApi';
import { EditNotePage, NewNotePage, NotesPage } from './NotesPage';

vi.mock('../api/notesApi', () => ({
  createNote: vi.fn(),
  fetchNote: vi.fn(),
  fetchNotes: vi.fn(),
  archiveNote: vi.fn(),
  organizeNote: vi.fn(),
  restoreNote: vi.fn(),
  updateNote: vi.fn(),
}));

const mockedArchiveNote = vi.mocked(archiveNote);
const mockedCreateNote = vi.mocked(createNote);
const mockedFetchNote = vi.mocked(fetchNote);
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

const note = {
  archivedAt: null,
  body: 'Project planning details that should appear as a compact preview.',
  createdAt: '2026-06-01T08:00:00Z',
  editorMode: 'RICH_TEXT' as const,
  favorite: false,
  id: 'note-1',
  linkedResources: '',
  noteDate: '2026-06-01',
  ownerAccountId: 'account-1',
  pinned: false,
  tags: 'planning',
  title: 'Organized note',
  updatedAt: '2026-06-01T08:00:00Z',
  workspaceContextId: 'workspace-1',
};

function currentDateKey() {
  const date = new Date();
  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
  return offsetDate.toISOString().slice(0, 10);
}

describe('NotesPage', () => {
  beforeEach(() => {
    mockedCreateNote.mockReset();
    mockedFetchNote.mockReset();
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

  it('loads today by default and shows an empty day state', async () => {
    mockedFetchNotes.mockResolvedValue([]);

    renderWithAuth(<NotesPage />, authSession);

    await waitFor(() => expect(mockedFetchNotes).toHaveBeenCalledWith(
      expect.objectContaining({ date: currentDateKey() }),
      expect.any(AbortSignal),
    ));
    expect(screen.getByRole('heading', { name: /no notes for this day/i })).toBeInTheDocument();
  });

  it('changes the selected day and opens new note for that day', async () => {
    mockedFetchNotes.mockResolvedValue([]);

    renderWithAuth(<NotesPage />, authSession);

    const dayInput = await screen.findByLabelText('Day', { selector: 'input' });
    fireEvent.change(dayInput, { target: { value: '2026-05-30' } });

    await waitFor(() => expect(mockedFetchNotes).toHaveBeenLastCalledWith(
      expect.objectContaining({ date: '2026-05-30' }),
      expect.any(AbortSignal),
    ));
    expect(screen.getByRole('link', { name: /new note/i })).toHaveAttribute('href', '/notes/new?date=2026-05-30');
  });

  it('creates a note from the dedicated new note page', async () => {
    const user = userEvent.setup();
    mockedCreateNote.mockResolvedValue(note);

    renderWithAuth(<NewNotePage />, { ...authSession, initialPath: '/notes/new?date=2026-05-30' });

    await user.type(screen.getByLabelText(/title/i), 'First note');
    await user.type(screen.getByLabelText(/body/i), 'A useful memory');
    await user.click(screen.getByRole('button', { name: /create note/i }));

    await waitFor(() => expect(mockedCreateNote).toHaveBeenCalledWith({
      body: 'A useful memory',
      noteDate: '2026-05-30',
      title: 'First note',
    }));
  });

  it('shows compact note cards and supports filters plus organization actions', async () => {
    const user = userEvent.setup();
    mockedFetchNotes.mockResolvedValue([note]);
    mockedOrganizeNote.mockResolvedValue({ ...note, favorite: true, pinned: true });
    mockedArchiveNote.mockResolvedValue({ ...note, archivedAt: '2026-06-01T08:30:00Z' });
    mockedRestoreNote.mockResolvedValue(note);

    renderWithAuth(<NotesPage />, authSession);

    expect(await screen.findByRole('heading', { name: /organized note/i })).toBeInTheDocument();
    expect(screen.getByText(/compact preview/i)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /organized note/i })).toHaveAttribute('href', '/notes/note-1');

    await user.click(screen.getByRole('button', { name: /mark favorite/i }));
    await waitFor(() => expect(mockedOrganizeNote).toHaveBeenCalledWith('note-1', expect.objectContaining({ favorite: true })));

    await user.click(screen.getByRole('button', { name: /pinned/i }));
    await user.type(screen.getByLabelText(/search/i), 'planning');

    await waitFor(() => expect(mockedFetchNotes).toHaveBeenLastCalledWith(
      expect.objectContaining({ date: currentDateKey(), q: 'planning' }),
      expect.any(AbortSignal),
    ));

    await user.click(screen.getByRole('button', { name: /archive/i }));
    await waitFor(() => expect(mockedArchiveNote).toHaveBeenCalledWith('note-1'));
  });

  it('loads and saves an existing note on the edit page', async () => {
    const user = userEvent.setup();
    mockedFetchNote.mockResolvedValue(note);
    mockedUpdateNote.mockResolvedValue({
      ...note,
      body: 'Updated body',
      title: 'Updated note',
      updatedAt: '2026-06-01T08:30:00Z',
    });

    renderWithAuth(
      <Routes>
        <Route element={<EditNotePage />} path="/notes/:noteId" />
      </Routes>,
      { ...authSession, initialPath: '/notes/note-1' },
    );

    expect(await screen.findByDisplayValue('Organized note')).toBeInTheDocument();
    await user.clear(screen.getByLabelText(/title/i));
    await user.type(screen.getByLabelText(/title/i), 'Updated note');
    await user.clear(screen.getByLabelText(/body/i));
    await user.type(screen.getByLabelText(/body/i), 'Updated body');
    await user.click(screen.getByRole('button', { name: /save changes/i }));

    await waitFor(() => expect(mockedUpdateNote).toHaveBeenCalledWith('note-1', {
      body: 'Updated body',
      title: 'Updated note',
    }));
  });
});
