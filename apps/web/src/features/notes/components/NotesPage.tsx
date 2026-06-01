import { Archive, CalendarDays, ChevronLeft, ChevronRight, FileText, Plus, RotateCcw, Star } from 'lucide-react';
import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, EmptyState, Field, LoadingState } from '../../../shared/ui';
import { archiveNote, createNote, fetchNote, fetchNotes, organizeNote, restoreNote, updateNote } from '../api/notesApi';
import type { Note } from '../types';

const dateFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'full',
});

function todayKey() {
  return toDateKey(new Date());
}

function toDateKey(date: Date) {
  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
  return offsetDate.toISOString().slice(0, 10);
}

function parseDateKey(value: string | null) {
  if (!value || !/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return todayKey();
  }

  return value;
}

function shiftDate(value: string, days: number) {
  const date = new Date(`${value}T00:00:00`);
  date.setDate(date.getDate() + days);
  return toDateKey(date);
}

function displayDate(value: string) {
  return dateFormatter.format(new Date(`${value}T00:00:00`));
}

function notePreview(note: Note) {
  const text = note.body.trim();
  if (!text) {
    return 'No body text';
  }

  return text.length > 150 ? `${text.slice(0, 147)}...` : text;
}

export function NotesPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const selectedDate = parseDateKey(searchParams.get('date'));
  const [notes, setNotes] = useState<Note[]>([]);
  const [filters, setFilters] = useState({ archived: false, favorite: false, pinned: false, q: '', tag: '' });
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const headingDate = useMemo(() => displayDate(selectedDate), [selectedDate]);

  useEffect(() => {
    if (!authenticated) {
      setNotes([]);
      return;
    }

    const controller = new AbortController();
    setIsLoading(true);
    setError(null);

    fetchNotes({ ...filters, date: selectedDate }, controller.signal)
      .then(setNotes)
      .catch((fetchError: unknown) => {
        if (fetchError instanceof DOMException && fetchError.name === 'AbortError') {
          return;
        }

        setError('Notes could not be loaded.');
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      });

    return () => controller.abort();
  }, [
    authenticated,
    activeWorkspace?.id,
    selectedDate,
    filters.archived,
    filters.favorite,
    filters.pinned,
    filters.q,
    filters.tag,
  ]);

  function setSelectedDate(value: string) {
    setSearchParams(value === todayKey() ? {} : { date: value });
  }

  async function updateOrganization(note: Note, patch: Partial<Pick<Note, 'favorite' | 'pinned'>>) {
    const draft = { ...note, ...patch };
    setNotes((current) => current.map((item) => (item.id === note.id ? draft : item)));

    try {
      const saved = await organizeNote(note.id, {
        editorMode: draft.editorMode,
        favorite: draft.favorite,
        linkedResources: draft.linkedResources,
        pinned: draft.pinned,
        tags: draft.tags,
      });
      setNotes((current) => current.map((item) => (item.id === saved.id ? saved : item)));
    } catch {
      setError('Note could not be updated.');
      setNotes((current) => current.map((item) => (item.id === note.id ? note : item)));
    }
  }

  async function toggleArchive(note: Note) {
    try {
      const saved = note.archivedAt ? await restoreNote(note.id) : await archiveNote(note.id);
      setNotes((current) => current.filter((item) => item.id !== saved.id));
    } catch {
      setError('Note could not be updated.');
    }
  }

  if (!authenticated) {
    return (
      <div className="route-stack">
        <header className="route-heading">
          <p className="eyebrow">Core workspace</p>
          <h2>Notes</h2>
        </header>
        <SignedOutPrompt />
      </div>
    );
  }

  return (
    <div className="route-stack">
      <header className="notes-header">
        <div>
          <p className="eyebrow">{activeWorkspace?.name ?? 'Active'} workspace</p>
          <h2>Notes for {headingDate}</h2>
        </div>
        <Link className="ui-link-button ui-link-button--primary" to={`/notes/new?date=${selectedDate}`}>
          <Plus aria-hidden="true" size={18} />
          <span>New note</span>
        </Link>
      </header>

      <section className="note-daybar" aria-label="Note day">
        <Button
          aria-label="Previous day"
          icon={<ChevronLeft aria-hidden="true" size={18} />}
          onClick={() => setSelectedDate(shiftDate(selectedDate, -1))}
          variant="secondary"
        >
          Previous
        </Button>
        <label className="field note-daybar__date" htmlFor="note-date">
          <span className="field__label">Day</span>
          <input
            id="note-date"
            onChange={(event) => setSelectedDate(event.target.value)}
            type="date"
            value={selectedDate}
          />
        </label>
        <Button
          aria-label="Next day"
          icon={<ChevronRight aria-hidden="true" size={18} />}
          onClick={() => setSelectedDate(shiftDate(selectedDate, 1))}
          variant="secondary"
        >
          Next
        </Button>
        <Button icon={<CalendarDays aria-hidden="true" size={18} />} onClick={() => setSelectedDate(todayKey())}>
          Today
        </Button>
      </section>

      <section className="note-filters" aria-label="Note filters">
        <Field
          label="Search"
          name="note-search"
          onChange={(event) => setFilters((current) => ({ ...current, q: event.target.value }))}
          placeholder="Search title or body"
          value={filters.q}
        />
        <Field
          label="Tag"
          name="note-tag-filter"
          onChange={(event) => setFilters((current) => ({ ...current, tag: event.target.value }))}
          placeholder="planning"
          value={filters.tag}
        />
        <label className="check-control">
          <input
            checked={filters.favorite}
            onChange={(event) => setFilters((current) => ({ ...current, favorite: event.target.checked }))}
            type="checkbox"
          />
          Favorites
        </label>
        <label className="check-control">
          <input
            checked={filters.pinned}
            onChange={(event) => setFilters((current) => ({ ...current, pinned: event.target.checked }))}
            type="checkbox"
          />
          Pinned
        </label>
        <label className="check-control">
          <input
            checked={filters.archived}
            onChange={(event) => setFilters((current) => ({ ...current, archived: event.target.checked }))}
            type="checkbox"
          />
          Archived
        </label>
      </section>

      {error ? <p className="session-warning">{error}</p> : null}
      {isLoading ? <LoadingState label="Loading notes" /> : null}

      {!isLoading && notes.length === 0 ? (
        <EmptyState
          description="Choose a day or create a note for this date."
          icon={<FileText aria-hidden="true" size={24} />}
          title={filters.q || filters.tag || filters.favorite || filters.pinned || filters.archived ? 'No matching notes' : 'No notes for this day'}
        />
      ) : null}

      {notes.length > 0 ? (
        <section className="notes-list notes-list--compact" aria-label="Workspace notes">
          {notes.map((note) => (
            <article className="note-card note-card--compact" key={note.id}>
              <Link className="note-card__link" to={`/notes/${note.id}`}>
                <div className="note-card__heading">
                  <h3>{note.title}</h3>
                  {note.pinned ? <span className="note-badge">Pinned</span> : null}
                </div>
                <p>{notePreview(note)}</p>
                {note.tags ? <p className="note-card__tags">{note.tags}</p> : null}
              </Link>
              <div className="note-card__meta">
                <time dateTime={note.updatedAt}>{new Date(note.updatedAt).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })}</time>
                <div className="note-card__actions">
                  <Button
                    aria-label={note.favorite ? 'Remove favorite' : 'Mark favorite'}
                    icon={<Star aria-hidden="true" size={16} />}
                    onClick={() => void updateOrganization(note, { favorite: !note.favorite })}
                    variant={note.favorite ? 'primary' : 'secondary'}
                  >
                    Favorite
                  </Button>
                  <Button
                    onClick={() => void updateOrganization(note, { pinned: !note.pinned })}
                    variant={note.pinned ? 'primary' : 'secondary'}
                  >
                    {note.pinned ? 'Pinned' : 'Pin'}
                  </Button>
                  <Button
                    icon={note.archivedAt ? <RotateCcw aria-hidden="true" size={16} /> : <Archive aria-hidden="true" size={16} />}
                    onClick={() => void toggleArchive(note)}
                    variant="secondary"
                  >
                    {note.archivedAt ? 'Restore' : 'Archive'}
                  </Button>
                </div>
              </div>
            </article>
          ))}
        </section>
      ) : null}
    </div>
  );
}

export function EditNotePage() {
  const { activeWorkspace, authenticated } = useAuth();
  const { noteId } = useParams();
  const navigate = useNavigate();
  const [note, setNote] = useState<Note | null>(null);
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!authenticated || !noteId) {
      setNote(null);
      return;
    }

    const controller = new AbortController();
    setIsLoading(true);
    setError(null);

    fetchNote(noteId, controller.signal)
      .then((loadedNote) => {
        setNote(loadedNote);
        setTitle(loadedNote.title);
        setBody(loadedNote.body);
      })
      .catch((fetchError: unknown) => {
        if (fetchError instanceof DOMException && fetchError.name === 'AbortError') {
          return;
        }

        setError('Note could not be loaded.');
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      });

    return () => controller.abort();
  }, [authenticated, noteId]);

  async function submitNote(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!noteId || !note) {
      return;
    }

    const trimmedTitle = title.trim();
    if (!trimmedTitle) {
      setError('Title is required.');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const saved = await updateNote(noteId, { body, title: trimmedTitle });
      setNote(saved);
      setTitle(saved.title);
      setBody(saved.body);
      navigate(saved.noteDate === todayKey() ? '/notes' : `/notes?date=${saved.noteDate}`);
    } catch {
      setError('Note could not be saved.');
    } finally {
      setIsSubmitting(false);
    }
  }

  if (!authenticated) {
    return (
      <div className="route-stack">
        <header className="route-heading">
          <p className="eyebrow">Core workspace</p>
          <h2>Edit note</h2>
        </header>
        <SignedOutPrompt />
      </div>
    );
  }

  return (
    <div className="route-stack">
      <header className="notes-header">
        <div>
          <p className="eyebrow">{activeWorkspace?.name ?? 'Active'} workspace</p>
          <h2>Edit note</h2>
        </div>
        <Link className="ui-link-button" to={note?.noteDate && note.noteDate !== todayKey() ? `/notes?date=${note.noteDate}` : '/notes'}>
          Back to notes
        </Link>
      </header>

      {isLoading ? <LoadingState label="Loading note" /> : null}
      {error && !title.trim() ? <p className="session-warning">{error}</p> : null}

      {!isLoading && note ? (
        <form className="note-composer note-composer--focused" onSubmit={submitNote}>
          <Field
            error={error && !title.trim() ? error : undefined}
            label="Title"
            maxLength={180}
            name="edit-note-title"
            onChange={(event) => setTitle(event.target.value)}
            value={title}
          />
          <label className="field" htmlFor="edit-note-body">
            <span className="field__label">Body</span>
            <textarea
              id="edit-note-body"
              maxLength={20000}
              name="edit-note-body"
              onChange={(event) => setBody(event.target.value)}
              value={body}
            />
          </label>
          {error && title.trim() ? <p className="session-warning">{error}</p> : null}
          <Button disabled={isSubmitting} type="submit" variant="primary">
            {isSubmitting ? 'Saving' : 'Save changes'}
          </Button>
        </form>
      ) : null}
    </div>
  );
}

export function NewNotePage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [noteDate, setNoteDate] = useState(parseDateKey(searchParams.get('date')));
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function submitNote(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmedTitle = title.trim();

    if (!trimmedTitle) {
      setError('Title is required.');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      await createNote({ body, noteDate, title: trimmedTitle });
      navigate(noteDate === todayKey() ? '/notes' : `/notes?date=${noteDate}`);
    } catch {
      setError('Note could not be saved.');
    } finally {
      setIsSubmitting(false);
    }
  }

  if (!authenticated) {
    return (
      <div className="route-stack">
        <header className="route-heading">
          <p className="eyebrow">Core workspace</p>
          <h2>New note</h2>
        </header>
        <SignedOutPrompt />
      </div>
    );
  }

  return (
    <div className="route-stack">
      <header className="notes-header">
        <div>
          <p className="eyebrow">{activeWorkspace?.name ?? 'Active'} workspace</p>
          <h2>New note</h2>
        </div>
        <Link className="ui-link-button" to={noteDate === todayKey() ? '/notes' : `/notes?date=${noteDate}`}>
          Back to notes
        </Link>
      </header>

      <form className="note-composer note-composer--focused" onSubmit={submitNote}>
        <Field
          label="Day"
          name="new-note-date"
          onChange={(event) => setNoteDate(event.target.value)}
          type="date"
          value={noteDate}
        />
        <Field
          error={error && !title.trim() ? error : undefined}
          label="Title"
          maxLength={180}
          name="note-title"
          onChange={(event) => setTitle(event.target.value)}
          placeholder="Meeting notes, idea, or useful context"
          value={title}
        />
        <label className="field" htmlFor="note-body">
          <span className="field__label">Body</span>
          <textarea
            id="note-body"
            maxLength={20000}
            name="note-body"
            onChange={(event) => setBody(event.target.value)}
            placeholder="Write the details here"
            value={body}
          />
        </label>
        {error && title.trim() ? <p className="session-warning">{error}</p> : null}
        <Button disabled={isSubmitting} icon={<Plus aria-hidden="true" size={18} />} type="submit" variant="primary">
          {isSubmitting ? 'Saving' : 'Create note'}
        </Button>
      </form>
    </div>
  );
}
