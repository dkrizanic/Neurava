import { Bell, CheckCircle2, RotateCcw } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, EmptyState, Field, LoadingState } from '../../../shared/ui';
import { applyReminderPreview, previewCreateReminder } from '../../assistant/api/assistantApi';
import { completeReminder, createReminder, fetchReminders, reopenReminder } from '../api/remindersApi';
import type { Reminder, UpsertReminderInput } from '../types';
import type { ReminderChangePreview } from '../../assistant/types';

function localDateTimeValue(date: Date) {
  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
  return offsetDate.toISOString().slice(0, 16);
}

function toInstant(value: string) {
  return new Date(value).toISOString();
}

export function RemindersPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [reminders, setReminders] = useState<Reminder[]>([]);
  const [includeCompleted, setIncludeCompleted] = useState(false);
  const [form, setForm] = useState({
    calendarSyncEnabled: true,
    details: '',
    dueAt: localDateTimeValue(new Date(Date.now() + 24 * 60 * 60 * 1000)),
    relatedContext: '',
    title: '',
  });
  const [aiText, setAiText] = useState('');
  const [preview, setPreview] = useState<ReminderChangePreview | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!authenticated) {
      setReminders([]);
      return;
    }
    const controller = new AbortController();
    setIsLoading(true);
    setError(null);
    fetchReminders(includeCompleted, controller.signal)
      .then(setReminders)
      .catch((loadError: unknown) => {
        if (loadError instanceof DOMException && loadError.name === 'AbortError') {
          return;
        }
        setError('Reminders could not be loaded.');
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      });
    return () => controller.abort();
  }, [authenticated, activeWorkspace?.id, includeCompleted]);

  async function submitReminder(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!form.title.trim()) {
      setError('Title is required.');
      return;
    }
    const input: UpsertReminderInput = { ...form, dueAt: toInstant(form.dueAt), title: form.title.trim() };
    try {
      const saved = await createReminder(input);
      setReminders((current) => [saved, ...current]);
      setForm((current) => ({ ...current, details: '', relatedContext: '', title: '' }));
      setError(null);
    } catch {
      setError('Reminder could not be saved.');
    }
  }

  async function previewAiReminder() {
    if (!aiText.trim()) {
      setError('AI reminder text is required.');
      return;
    }
    try {
      const response = await previewCreateReminder(aiText);
      setPreview(response.preview as ReminderChangePreview);
      setError(null);
    } catch {
      setError('AI reminder preview could not be created.');
    }
  }

  async function applyAiReminder() {
    if (!preview) {
      return;
    }
    try {
      const response = await applyReminderPreview(preview);
      setReminders((current) => [response.entity as Reminder, ...current]);
      setPreview(null);
      setAiText('');
    } catch {
      setError('AI reminder could not be applied.');
    }
  }

  async function toggleComplete(reminder: Reminder) {
    try {
      const saved = reminder.completedAt ? await reopenReminder(reminder.id) : await completeReminder(reminder.id);
      setReminders((current) => current.map((item) => (item.id === saved.id ? saved : item)));
    } catch {
      setError('Reminder could not be updated.');
    }
  }

  if (!authenticated) {
    return (
      <div className="route-stack">
        <header className="route-heading">
          <p className="eyebrow">Planning</p>
          <h2>Reminders</h2>
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
          <h2>Reminders</h2>
        </div>
        <label className="check-control">
          <input checked={includeCompleted} onChange={(event) => setIncludeCompleted(event.target.checked)} type="checkbox" />
          Completed
        </label>
      </header>

      {error ? <p className="session-warning">{error}</p> : null}

      <form className="note-composer" onSubmit={submitReminder}>
        <Field label="Title" name="reminder-title" onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} value={form.title} />
        <Field label="Due" name="reminder-due" onChange={(event) => setForm((current) => ({ ...current, dueAt: event.target.value }))} type="datetime-local" value={form.dueAt} />
        <Field label="Context" name="reminder-context" onChange={(event) => setForm((current) => ({ ...current, relatedContext: event.target.value }))} value={form.relatedContext} />
        <label className="field" htmlFor="reminder-details">
          <span className="field__label">Details</span>
          <textarea id="reminder-details" onChange={(event) => setForm((current) => ({ ...current, details: event.target.value }))} value={form.details} />
        </label>
        <label className="check-control">
          <input checked={form.calendarSyncEnabled} onChange={(event) => setForm((current) => ({ ...current, calendarSyncEnabled: event.target.checked }))} type="checkbox" />
          Sync to Calendar
        </label>
        <Button icon={<Bell aria-hidden="true" size={18} />} type="submit" variant="primary">Create reminder</Button>
      </form>

      <section className="assistant-panel" aria-label="AI reminder creator">
        <label className="field" htmlFor="ai-reminder">
          <span className="field__label">AI reminder</span>
          <textarea id="ai-reminder" onChange={(event) => setAiText(event.target.value)} value={aiText} />
        </label>
        <Button onClick={() => void previewAiReminder()} type="button" variant="secondary">Preview AI reminder</Button>
        {preview ? (
          <div className="ai-preview">
            <h3>{preview.title}</h3>
            <p>{preview.details}</p>
            <p>{new Date(preview.dueAt).toLocaleString()}</p>
            <Button onClick={() => void applyAiReminder()} type="button" variant="primary">Apply reminder</Button>
          </div>
        ) : null}
      </section>

      {isLoading ? <LoadingState label="Loading reminders" /> : null}
      {!isLoading && reminders.length === 0 ? (
        <EmptyState description="Create a reminder or preview one from AI." icon={<Bell aria-hidden="true" size={24} />} title="No reminders yet" />
      ) : null}
      <section className="notes-list notes-list--compact" aria-label="Reminder list">
        {reminders.map((reminder) => (
          <article className="note-card note-card--compact" key={reminder.id}>
            <div className="note-card__heading">
              <h3>{reminder.title}</h3>
              <span className="note-badge">{reminder.calendarSyncState}</span>
            </div>
            <p>{reminder.details || reminder.relatedContext || 'No extra details'}</p>
            <div className="note-card__meta">
              <time dateTime={reminder.dueAt}>{new Date(reminder.dueAt).toLocaleString()}</time>
              <Button icon={reminder.completedAt ? <RotateCcw aria-hidden="true" size={16} /> : <CheckCircle2 aria-hidden="true" size={16} />} onClick={() => void toggleComplete(reminder)} variant="secondary">
                {reminder.completedAt ? 'Reopen' : 'Complete'}
              </Button>
            </div>
          </article>
        ))}
      </section>
    </div>
  );
}

