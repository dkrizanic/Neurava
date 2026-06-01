import { Bot, Check, RotateCcw, Send, X } from 'lucide-react';
import { useEffect, useRef, useState, type FormEvent } from 'react';
import { Link } from 'react-router';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, Field, LoadingState } from '../../../shared/ui';
import { formatIsoDateTime } from '../../../shared/lib/dates';
import {
  answerQuestion,
  applyCreateNotePreview,
  applyPlanPreview,
  applyReminderPreview,
  fetchAiActionHistory,
  previewCreatePlan,
  previewCreateNote,
  previewCreateReminder,
  revertAiAction,
} from '../api/assistantApi';
import type {
  AiActionHistorySummary,
  AssistantMessage,
  AssistantActionPreviewResponse,
  NoteChangePreview,
  PlanChangePreview,
  ReminderChangePreview,
  SourceReference,
} from '../types';

export function AssistantPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [question, setQuestion] = useState('');
  const [messages, setMessages] = useState<AssistantMessage[]>([]);
  const [history, setHistory] = useState<AiActionHistorySummary[]>([]);
  const [historyError, setHistoryError] = useState<string | null>(null);
  const [revertingHistoryId, setRevertingHistoryId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [applyingMessageId, setApplyingMessageId] = useState<string | null>(null);
  const [lastRequest, setLastRequest] = useState<string | null>(null);
  const threadRef = useRef<HTMLElement | null>(null);
  const stickToBottomRef = useRef(true);
  const latestMessage = messages.at(-1);
  const canRetry = Boolean(
    lastRequest
    && latestMessage?.role === 'assistant'
    && 'type' in latestMessage
    && latestMessage.type === 'error',
  );

  useEffect(() => {
    if (!authenticated) {
      return undefined;
    }

    const controller = new AbortController();
    void loadHistory(controller.signal);
    return () => controller.abort();
  }, [authenticated]);

  useEffect(() => {
    const thread = threadRef.current;
    if (!thread) {
      return undefined;
    }

    const onScroll = () => {
      const distanceFromBottom = thread.scrollHeight - thread.scrollTop - thread.clientHeight;
      stickToBottomRef.current = distanceFromBottom < 32;
    };

    thread.addEventListener('scroll', onScroll);
    return () => thread.removeEventListener('scroll', onScroll);
  }, []);

  useEffect(() => {
    const thread = threadRef.current;
    if (!thread || !stickToBottomRef.current) {
      return;
    }
    thread.scrollTop = thread.scrollHeight;
  }, [isLoading, messages]);

  async function submitQuestion(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await sendPrompt(question);
  }

  async function sendPrompt(rawPrompt: string) {
    const trimmedPrompt = rawPrompt.trim();
    if (!trimmedPrompt) {
      setError('Question is required.');
      return;
    }

    setError(null);

    if (isCreateReminderRequest(trimmedPrompt)) {
      setLastRequest(trimmedPrompt);
      appendMessages([userMessage(trimmedPrompt)]);
      setQuestion('');
      setIsLoading(true);
      try {
        const preview = await previewCreateReminder(trimmedPrompt);
        appendMessages([{ id: messageId(), preview, role: 'assistant' }]);
      } catch (previewError) {
        appendMessages([assistantTextMessage('error', assistantApiMessage(previewError, 'Reminder preview could not be loaded.'))]);
      } finally {
        setIsLoading(false);
      }
      return;
    }

    if (isCreatePlanRequest(trimmedPrompt)) {
      setLastRequest(trimmedPrompt);
      appendMessages([userMessage(trimmedPrompt)]);
      setQuestion('');
      setIsLoading(true);
      try {
        const preview = await previewCreatePlan(trimmedPrompt);
        appendMessages([{ id: messageId(), preview, role: 'assistant' }]);
      } catch (previewError) {
        appendMessages([assistantTextMessage('error', assistantApiMessage(previewError, 'Plan preview could not be loaded.'))]);
      } finally {
        setIsLoading(false);
      }
      return;
    }

    if (isCreateNoteRequest(trimmedPrompt)) {
      setLastRequest(trimmedPrompt);
      appendMessages([userMessage(trimmedPrompt)]);
      setQuestion('');
      setIsLoading(true);
      try {
        const preview = await previewCreateNote(trimmedPrompt);
        appendMessages([{ id: messageId(), preview, role: 'assistant' }]);
      } catch (previewError) {
        appendMessages([assistantTextMessage('error', assistantApiMessage(previewError, 'Note preview could not be loaded.'))]);
      } finally {
        setIsLoading(false);
      }
      return;
    }

    if (isAmbiguous(trimmedPrompt)) {
      appendMessages([
        userMessage(trimmedPrompt),
        assistantTextMessage('clarification', 'Tell me a little more so I can search the right notebook context. Try a topic, decision, person, or remembered phrase.'),
      ]);
      setQuestion(trimmedPrompt);
      return;
    }

    setLastRequest(trimmedPrompt);
    appendMessages([userMessage(trimmedPrompt)]);
    setQuestion('');
    setIsLoading(true);

    try {
      const answer = await answerQuestion(trimmedPrompt);
      appendMessages([{ answer, id: messageId(), role: 'assistant' }]);
    } catch (answerError) {
      appendMessages([assistantTextMessage('error', assistantApiMessage(answerError, 'Assistant response could not be loaded.'))]);
    } finally {
      setIsLoading(false);
    }
  }

  async function retryLastRequest() {
    if (!lastRequest || isLoading) {
      return;
    }
    await sendPrompt(lastRequest);
  }

  function appendMessages(nextMessages: AssistantMessage[]) {
    setMessages((current) => [...current, ...nextMessages]);
  }

  async function loadHistory(signal?: AbortSignal) {
    try {
      const records = await fetchAiActionHistory(signal);
      setHistory(records);
      setHistoryError(null);
    } catch (loadError) {
      if (loadError instanceof DOMException && loadError.name === 'AbortError') {
        return;
      }
      setHistoryError(assistantApiMessage(loadError, 'AI action history could not be loaded.'));
    }
  }

  async function applyPreview(messageIdToApply: string, preview: AssistantActionPreviewResponse) {
    if (applyingMessageId) {
      return;
    }
    setApplyingMessageId(messageIdToApply);
    try {
      const applied = await applyAssistantPreview(preview);
      setMessages((current) => current.filter((message) => message.id !== messageIdToApply));
      appendMessages([assistantTextMessage('status', savedStatus(applied.summary, applied.entityType))]);
      await loadHistory();
    } catch (applyError) {
      appendMessages([assistantTextMessage('error', assistantApiMessage(applyError, 'AI change could not be applied.'))]);
    } finally {
      setApplyingMessageId(null);
    }
  }

  function cancelPreview(messageIdToRemove: string) {
    setMessages((current) => current.filter((message) => message.id !== messageIdToRemove));
  }

  async function revertHistory(record: AiActionHistorySummary) {
    if (revertingHistoryId) {
      return;
    }
    setRevertingHistoryId(record.id);
    try {
      const reverted = await revertAiAction(record.id);
      setHistory((current) => current.map((item) => (item.id === reverted.id ? reverted : item)));
      appendMessages([assistantTextMessage('status', reverted.revertSummary ?? 'AI action reverted.')]);
      await loadHistory();
    } catch (revertError) {
      appendMessages([
        assistantTextMessage(
          'error',
          assistantApiMessage(revertError, 'AI action could not be reverted. The related data may no longer exist.'),
        ),
      ]);
    } finally {
      setRevertingHistoryId(null);
    }
  }

  if (!authenticated) {
    return (
      <div className="route-stack">
        <header className="route-heading">
          <p className="eyebrow">AI workspace</p>
          <h2>Assistant</h2>
        </header>
        <SignedOutPrompt />
      </div>
    );
  }

  return (
    <div className="route-stack">
      <header className="route-heading">
        <p className="eyebrow">{activeWorkspace?.name ?? 'Active'} workspace</p>
        <h2>Assistant</h2>
        <p>Ask anything about your workspace. The assistant will use your notes as source context when it can.</p>
      </header>

      <section className="assistant-thread" aria-label="Assistant conversation" ref={threadRef}>
        {messages.length === 0 ? (
          <div className="assistant-thread__empty">
            <Bot aria-hidden="true" size={24} />
            <p>Start with a specific question, search request, or summary request from the active workspace.</p>
          </div>
        ) : null}
        {messages.map((message) => (
          <AssistantMessageItem
            applyingMessageId={applyingMessageId}
            key={message.id}
            message={message}
            onApplyPreview={(preview) => void applyPreview(message.id, preview)}
            onCancelPreview={() => cancelPreview(message.id)}
          />
        ))}
        {isLoading ? <LoadingState label="Thinking with your workspace context" /> : null}
      </section>

      {canRetry ? (
        <Button
          icon={<RotateCcw aria-hidden="true" size={16} />}
          onClick={() => void retryLastRequest()}
          type="button"
          variant="secondary"
        >
          Retry last request
        </Button>
      ) : null}

      {error && error !== 'Question is required.' ? (
        <p className="session-warning" role="alert">
          {error}
        </p>
      ) : null}

      <form className="assistant-question" onSubmit={(event) => void submitQuestion(event)}>
        <Field
          error={error === 'Question is required.' ? error : undefined}
          label="Message"
          maxLength={1000}
          name="assistant-question"
          onChange={(event) => setQuestion(event.target.value)}
          placeholder="Ask, search, or summarize your notes"
          value={question}
        />
        <Button disabled={isLoading} icon={<Send aria-hidden="true" size={18} />} type="submit" variant="primary">
          Send
        </Button>
      </form>

      <section className="assistant-history" aria-label="Recent AI changes">
        <div>
          <p className="eyebrow">AI action history</p>
          <h3>Recent AI changes</h3>
        </div>
        {historyError ? <p className="session-warning">{historyError}</p> : null}
        {!historyError && history.length === 0 ? (
          <p className="muted-text">No AI changes have been applied yet.</p>
        ) : null}
        {history.map((record) => (
          <article className="assistant-history__item" key={record.id}>
            <div>
              <h4>{record.summary}</h4>
              <p>{record.revertedAt ? `Reverted: ${record.revertSummary}` : `${record.entityType} ${record.changeType}`}</p>
            </div>
            <div className="assistant-history__meta">
              <time dateTime={record.createdAt}>{formatIsoDateTime(record.createdAt)}</time>
              {!record.revertedAt && record.changeType === 'create' ? (
                <Button
                  disabled={revertingHistoryId === record.id}
                  icon={<RotateCcw aria-hidden="true" size={16} />}
                  onClick={() => void revertHistory(record)}
                  type="button"
                  variant="secondary"
                >
                  {revertingHistoryId === record.id ? 'Reverting' : 'Revert'}
                </Button>
              ) : null}
            </div>
          </article>
        ))}
      </section>
    </div>
  );
}

function savedStatus(summary: string, entityType: string) {
  if (entityType === 'note') {
    return `${summary} It is now saved in Notes.`;
  }
  if (entityType === 'reminder') {
    return `${summary} It is now saved in Reminders.`;
  }
  if (entityType === 'plan') {
    return `${summary} It is now saved in Plans.`;
  }
  return `${summary} It is now saved.`;
}

function applyAssistantPreview(preview: AssistantActionPreviewResponse) {
  if (preview.action === 'create_note') {
    return applyCreateNotePreview(preview.preview as NoteChangePreview);
  }
  if (preview.action === 'create_reminder') {
    return applyReminderPreview(preview.preview as ReminderChangePreview);
  }
  if (preview.action === 'create_plan') {
    return applyPlanPreview(preview.preview as PlanChangePreview);
  }
  throw new Error(`Unsupported preview action: ${preview.action}`);
}

function AssistantMessageItem({
  applyingMessageId,
  message,
  onApplyPreview,
  onCancelPreview,
}: {
  applyingMessageId: string | null;
  message: AssistantMessage;
  onApplyPreview: (preview: AssistantActionPreviewResponse) => void;
  onCancelPreview: () => void;
}) {
  if (message.role === 'user') {
    return (
      <article className="assistant-message assistant-message--user">
        <p className="eyebrow">You</p>
        <p>{message.text}</p>
      </article>
    );
  }

  if ('type' in message) {
    return (
      <article className={`assistant-message assistant-message--${message.type}`}>
        <p className="eyebrow">{assistantTextLabel(message.type)}</p>
        <p>{message.text}</p>
      </article>
    );
  }

  if ('preview' in message) {
    return (
      <PreviewMessageItem
        applyingMessageId={applyingMessageId}
        messageId={message.id}
        onApplyPreview={onApplyPreview}
        onCancelPreview={onCancelPreview}
        preview={message.preview}
      />
    );
  }

  return (
    <article className="assistant-message assistant-message--assistant" aria-label="Assistant answer">
      {message.answer.enoughSourceContext ? (
        <>
          <p className="eyebrow">Assistant</p>
          <p>{message.answer.answer}</p>
          <SourceReferences sources={message.answer.sources} />
        </>
      ) : (
        <>
          <p className="eyebrow">Not enough source context</p>
          <p>{message.answer.answer}</p>
        </>
      )}
    </article>
  );
}

function PreviewMessageItem({
  applyingMessageId,
  messageId,
  onApplyPreview,
  onCancelPreview,
  preview,
}: {
  applyingMessageId: string | null;
  messageId: string;
  onApplyPreview: (preview: AssistantActionPreviewResponse) => void;
  onCancelPreview: () => void;
  preview: AssistantActionPreviewResponse;
}) {
  const [draftPreview, setDraftPreview] = useState(preview);
  const isApplying = applyingMessageId === messageId;

  useEffect(() => {
    setDraftPreview(preview);
  }, [preview]);

  return (
    <article className="assistant-message assistant-message--assistant" aria-label="AI change preview">
      <p className="eyebrow">Preview</p>
      <h3>{draftPreview.summary}</h3>
      <PreviewEditor preview={draftPreview} setPreview={setDraftPreview} />
      <div className="assistant-preview__actions">
        <Button
          disabled={isApplying}
          icon={<Check aria-hidden="true" size={16} />}
          onClick={() => onApplyPreview(draftPreview)}
          type="button"
          variant="primary"
        >
          {isApplying ? 'Applying' : 'Apply'}
        </Button>
        <Button
          disabled={isApplying}
          icon={<X aria-hidden="true" size={16} />}
          onClick={onCancelPreview}
          type="button"
          variant="secondary"
        >
          Cancel
        </Button>
      </div>
    </article>
  );
}

function PreviewEditor({
  preview,
  setPreview,
}: {
  preview: AssistantActionPreviewResponse;
  setPreview: (preview: AssistantActionPreviewResponse) => void;
}) {
  if (preview.entityType === 'note') {
    const note = preview.preview as NoteChangePreview;
    return (
      <div className="assistant-preview-editable-grid">
        <label className="assistant-preview-field">
          <span>Title</span>
          <input
            onChange={(event) => setPreview({ ...preview, preview: { ...note, title: event.target.value } })}
            type="text"
            value={note.title}
          />
        </label>
        <label className="assistant-preview-field">
          <span>Body</span>
          <textarea
            onChange={(event) => setPreview({ ...preview, preview: { ...note, body: event.target.value } })}
            rows={4}
            value={note.body}
          />
        </label>
        <label className="assistant-preview-field">
          <span>Tags</span>
          <input
            onChange={(event) => setPreview({ ...preview, preview: { ...note, tags: event.target.value } })}
            placeholder="No tags suggested"
            type="text"
            value={note.tags}
          />
        </label>
        <label className="assistant-preview-field">
          <span>Links</span>
          <textarea
            onChange={(event) => setPreview({ ...preview, preview: { ...note, linkedResources: event.target.value } })}
            placeholder="No links suggested"
            rows={3}
            value={note.linkedResources}
          />
        </label>
      </div>
    );
  }
  if (preview.entityType === 'reminder') {
    const reminder = preview.preview as ReminderChangePreview;
    return (
      <div className="assistant-preview-editable-grid">
        <label className="assistant-preview-field">
          <span>Title</span>
          <input
            onChange={(event) => setPreview({ ...preview, preview: { ...reminder, title: event.target.value } })}
            type="text"
            value={reminder.title}
          />
        </label>
        <label className="assistant-preview-field">
          <span>Details</span>
          <textarea
            onChange={(event) => setPreview({ ...preview, preview: { ...reminder, details: event.target.value } })}
            rows={3}
            value={reminder.details}
          />
        </label>
        <label className="assistant-preview-field">
          <span>Due</span>
          <input
            onChange={(event) => setPreview({ ...preview, preview: { ...reminder, dueAt: event.target.value } })}
            type="text"
            value={reminder.dueAt}
          />
        </label>
        <label className="assistant-preview-field assistant-preview-field--inline">
          <input
            checked={reminder.calendarSyncEnabled}
            onChange={(event) => setPreview({ ...preview, preview: { ...reminder, calendarSyncEnabled: event.target.checked } })}
            type="checkbox"
          />
          <span>Calendar sync</span>
        </label>
        <label className="assistant-preview-field">
          <span>Related context</span>
          <textarea
            onChange={(event) => setPreview({ ...preview, preview: { ...reminder, relatedContext: event.target.value } })}
            rows={2}
            value={reminder.relatedContext}
          />
        </label>
      </div>
    );
  }
  const plan = preview.preview as PlanChangePreview;
  return (
    <div className="assistant-preview-editable-grid">
      <label className="assistant-preview-field">
        <span>Title</span>
        <input
          onChange={(event) => setPreview({ ...preview, preview: { ...plan, title: event.target.value } })}
          type="text"
          value={plan.title}
        />
      </label>
      <label className="assistant-preview-field">
        <span>Goal</span>
        <textarea
          onChange={(event) => setPreview({ ...preview, preview: { ...plan, goal: event.target.value } })}
          rows={2}
          value={plan.goal}
        />
      </label>
      <label className="assistant-preview-field">
        <span>Items</span>
        <textarea
          onChange={(event) => setPreview({ ...preview, preview: { ...plan, items: event.target.value } })}
          rows={4}
          value={plan.items}
        />
      </label>
      <label className="assistant-preview-field">
        <span>Links</span>
        <textarea
          onChange={(event) => setPreview({ ...preview, preview: { ...plan, linkedResources: event.target.value } })}
          rows={2}
          value={plan.linkedResources}
        />
      </label>
    </div>
  );
}

function SourceReferences({ sources }: { sources: SourceReference[] }) {
  if (sources.length === 0) {
    return null;
  }

  return (
    <section className="assistant-sources" aria-label="Source references">
      <h3>Source References</h3>
      {sources.map((source) => (
        <article className="assistant-source" key={source.id}>
          <div>
            <p className="eyebrow">{source.type} source</p>
            <h4>{source.title}</h4>
          </div>
          <p>{source.snippet}</p>
          <div className="memory-result__meta">
            <time dateTime={source.sourceUpdatedAt}>{formatIsoDateTime(source.sourceUpdatedAt)}</time>
            <span>{Math.round(source.score * 100)}% match</span>
          </div>
          {source.type === 'note' ? (
            <Link className="assistant-source__open-link" to={`/notes/${source.id}`}>
              Open note and edit
            </Link>
          ) : null}
        </article>
      ))}
    </section>
  );
}

function isAmbiguous(prompt: string) {
  return prompt.split(/\s+/).filter(Boolean).length < 2;
}

function isCreateNoteRequest(prompt: string) {
  const normalized = prompt.toLowerCase().replace(/[^a-z0-9]+/g, ' ').trim();
  return /\b(?:create|make|add|new|start|write)\b.*\bnotes?\b/.test(normalized)
    || /\bnotes?\b.*\b(?:create|make|add|new|start|write)\b/.test(normalized);
}

function isCreateReminderRequest(prompt: string) {
  const normalized = prompt.toLowerCase().replace(/[^a-z0-9]+/g, ' ').trim();
  return /\b(?:create|make|add|new|set)\b.*\breminders?\b/.test(normalized)
    || /\breminders?\b.*\b(?:create|make|add|new|set)\b/.test(normalized);
}

function isCreatePlanRequest(prompt: string) {
  const normalized = prompt.toLowerCase().replace(/[^a-z0-9]+/g, ' ').trim();
  return /\b(?:create|make|add|new|generate|build)\b.*\bplans?\b/.test(normalized)
    || /\bplans?\b.*\b(?:create|make|add|new|generate|build)\b/.test(normalized);
}

function userMessage(text: string): AssistantMessage {
  return { id: messageId(), role: 'user', text };
}

function assistantTextMessage(type: 'clarification' | 'error' | 'status', text: string): AssistantMessage {
  return { id: messageId(), role: 'assistant', text, type };
}

function assistantTextLabel(type: 'clarification' | 'error' | 'status') {
  if (type === 'error') {
    return 'Error';
  }
  if (type === 'status') {
    return 'Assistant';
  }
  return 'Clarifying question';
}

function assistantApiMessage(error: unknown, fallback: string) {
  if (error instanceof Error && error.message === 'AUTH_REQUIRED') {
    return 'Your API session is not signed in. Sign in again, then retry the request.';
  }
  return fallback;
}

function messageId() {
  return globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`;
}
