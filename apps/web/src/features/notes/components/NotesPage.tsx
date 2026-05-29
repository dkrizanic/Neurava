import { Archive, FileText, Plus, RotateCcw, Star } from 'lucide-react';
import { useEffect, useRef, useState, type FormEvent } from 'react';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, EmptyState, Field, LoadingState } from '../../../shared/ui';
import { archiveNote, createNote, fetchNotes, organizeNote, restoreNote, updateNote } from '../api/notesApi';
import type { Note } from '../types';

type SaveState = {
  message: string;
  status: 'idle' | 'saving' | 'saved' | 'error';
};

export function NotesPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [notes, setNotes] = useState<Note[]>([]);
  const notesRef = useRef<Note[]>([]);
  const saveTimers = useRef<Record<string, ReturnType<typeof setTimeout>>>({});
  const [saveStates, setSaveStates] = useState<Record<string, SaveState>>({});
  const [filters, setFilters] = useState({ archived: false, favorite: false, pinned: false, q: '', tag: '' });
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    notesRef.current = notes;
  }, [notes]);

  useEffect(() => () => {
    Object.values(saveTimers.current).forEach(clearTimeout);
  }, []);

  useEffect(() => {
    if (!authenticated) {
      setNotes([]);
      return;
    }

    const controller = new AbortController();
    setIsLoading(true);
    setError(null);

    fetchNotes(filters, controller.signal)
      .then(setNotes)
      .catch(() => setError('Notes could not be loaded.'))
      .finally(() => setIsLoading(false));

    return () => controller.abort();
  }, [authenticated, activeWorkspace?.id, filters.archived, filters.favorite, filters.pinned, filters.q, filters.tag]);

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
      const note = await createNote({ body, title: trimmedTitle });
      setNotes((current) => [note, ...current]);
      setSaveStates((current) => ({
        ...current,
        [note.id]: { message: 'Saved', status: 'saved' },
      }));
      setTitle('');
      setBody('');
    } catch {
      setError('Note could not be saved.');
    } finally {
      setIsSubmitting(false);
    }
  }

  async function updateOrganization(note: Note, patch: Partial<Pick<Note, 'editorMode' | 'favorite' | 'linkedResources' | 'pinned' | 'tags'>>) {
    const draft = { ...note, ...patch };
    setNotes((current) => current.map((item) => (item.id === note.id ? draft : item)));
    setSaveStates((current) => ({
      ...current,
      [note.id]: { message: 'Saving', status: 'saving' },
    }));

    try {
      const saved = await organizeNote(note.id, {
        editorMode: draft.editorMode,
        favorite: draft.favorite,
        linkedResources: draft.linkedResources,
        pinned: draft.pinned,
        tags: draft.tags,
      });
      setNotes((current) => current.map((item) => (item.id === saved.id ? saved : item)));
      setSaveStates((current) => ({
        ...current,
        [saved.id]: { message: 'Saved', status: 'saved' },
      }));
    } catch {
      setSaveStates((current) => ({
        ...current,
        [note.id]: { message: 'Save failed', status: 'error' },
      }));
    }
  }

  async function toggleArchive(note: Note) {
    try {
      const saved = note.archivedAt ? await restoreNote(note.id) : await archiveNote(note.id);
      setNotes((current) => current.filter((item) => item.id !== saved.id));
    } catch {
      setSaveStates((current) => ({
        ...current,
        [note.id]: { message: 'Save failed', status: 'error' },
      }));
    }
  }

  function editNote(noteId: string, field: 'body' | 'title', value: string) {
    const currentNote = notesRef.current.find((note) => note.id === noteId);
    if (!currentNote) {
      return;
    }

    const draft = {
      ...currentNote,
      [field]: value,
    };

    setNotes((current) => current.map((note) => (note.id === noteId ? draft : note)));
    scheduleSave(draft);
  }

  function scheduleSave(note: Note) {
    setSaveStates((current) => ({
      ...current,
      [note.id]: { message: 'Saving', status: 'saving' },
    }));

    clearTimeout(saveTimers.current[note.id]);
    saveTimers.current[note.id] = setTimeout(() => {
      void saveNote(note);
    }, 650);
  }

  async function saveNote(draft: Note) {
    if (!draft.title.trim()) {
      setSaveStates((current) => ({
        ...current,
        [draft.id]: { message: 'Title is required', status: 'error' },
      }));
      return;
    }

    try {
      const saved = await updateNote(draft.id, { body: draft.body, title: draft.title.trim() });
      setNotes((current) => current.map((note) => {
        if (note.id !== saved.id) {
          return note;
        }

        if (note.title !== draft.title || note.body !== draft.body) {
          return note;
        }

        return saved;
      }));
      setSaveStates((current) => ({
        ...current,
        [saved.id]: { message: 'Saved', status: 'saved' },
      }));
    } catch {
      setSaveStates((current) => ({
        ...current,
        [draft.id]: { message: 'Save failed', status: 'error' },
      }));
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
      <header className="route-heading">
        <p className="eyebrow">{activeWorkspace?.name ?? 'Active'} workspace</p>
        <h2>Notes</h2>
      </header>

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

      <form className="note-composer" onSubmit={submitNote}>
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
        <Button disabled={isSubmitting} icon={<Plus aria-hidden="true" size={18} />} type="submit" variant="primary">
          {isSubmitting ? 'Saving' : 'Create note'}
        </Button>
      </form>

      {error && title.trim() ? <p className="session-warning">{error}</p> : null}

      {isLoading ? <LoadingState label="Loading notes" /> : null}

      {!isLoading && notes.length === 0 ? (
        <EmptyState
          description="Create the first note for this workspace."
          icon={<FileText aria-hidden="true" size={24} />}
          title={filters.q || filters.tag || filters.favorite || filters.pinned || filters.archived ? 'No matching notes' : 'No notes yet'}
        />
      ) : null}

      {notes.length > 0 ? (
        <section className="notes-list" aria-label="Workspace notes">
          {notes.map((note) => (
            <article className="note-card" key={note.id}>
              <label className="field" htmlFor={`note-title-${note.id}`}>
                <span className="field__label">Title</span>
                <input
                  aria-label={`Title for ${note.title || 'Untitled note'}`}
                  id={`note-title-${note.id}`}
                  maxLength={180}
                  onChange={(event) => editNote(note.id, 'title', event.target.value)}
                  value={note.title}
                />
              </label>
              <label className="field" htmlFor={`note-body-${note.id}`}>
                <span className="field__label">Body</span>
                <textarea
                  aria-label={`Body for ${note.title || 'Untitled note'}`}
                  id={`note-body-${note.id}`}
                  maxLength={20000}
                  onChange={(event) => editNote(note.id, 'body', event.target.value)}
                  value={note.body}
                />
              </label>
              <div className="note-card__tools">
                <Field
                  aria-label={`Tags for ${note.title || 'Untitled note'}`}
                  label="Tags"
                  name={`note-tags-${note.id}`}
                  onBlur={() => void updateOrganization(note, { tags: note.tags })}
                  onChange={(event) => setNotes((current) => current.map((item) => (
                    item.id === note.id ? { ...item, tags: event.target.value } : item
                  )))}
                  placeholder="planning, work"
                  value={note.tags}
                />
                <label className="field" htmlFor={`note-mode-${note.id}`}>
                  <span className="field__label">Mode</span>
                  <select
                    id={`note-mode-${note.id}`}
                    onChange={(event) => void updateOrganization(note, { editorMode: event.target.value as Note['editorMode'] })}
                    value={note.editorMode}
                  >
                    <option value="RICH_TEXT">Rich text - meeting notes and structured ideas</option>
                    <option value="MARKDOWN">Markdown - headings, lists, and technical notes</option>
                    <option value="JOURNAL">Journal - reflective personal notes</option>
                  </select>
                </label>
                <Field
                  aria-label={`Linked resources for ${note.title || 'Untitled note'}`}
                  label="Links"
                  name={`note-links-${note.id}`}
                  onBlur={() => void updateOrganization(note, { linkedResources: note.linkedResources })}
                  onChange={(event) => setNotes((current) => current.map((item) => (
                    item.id === note.id ? { ...item, linkedResources: event.target.value } : item
                  )))}
                  placeholder="PLAN:123, PROJECT:abc"
                  value={note.linkedResources}
                />
              </div>
              <div className="note-card__meta">
                <span className={`save-status save-status--${saveStates[note.id]?.status ?? 'idle'}`} role="status">
                  {saveStates[note.id]?.message ?? 'Saved'}
                </span>
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
                <time dateTime={note.updatedAt}>{new Date(note.updatedAt).toLocaleString()}</time>
              </div>
            </article>
          ))}
        </section>
      ) : null}
    </div>
  );
}
