import { Bot, Check, RotateCcw, Send, X } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, Field, LoadingState } from '../../../shared/ui';
import { formatIsoDateTime } from '../../../shared/lib/dates';
import { answerQuestion, applyCreateNotePreview, fetchAiActionHistory, previewCreateNote } from '../api/assistantApi';
import type {
  AiActionHistorySummary,
  AssistantMessage,
  AssistantActionPreviewResponse,
  SourceReference,
} from '../types';

export function AssistantPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [question, setQuestion] = useState('');
  const [messages, setMessages] = useState<AssistantMessage[]>([]);
  const [history, setHistory] = useState<AiActionHistorySummary[]>([]);
  const [historyError, setHistoryError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [applyingMessageId, setApplyingMessageId] = useState<string | null>(null);
  const [lastRequest, setLastRequest] = useState<string | null>(null);
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

    if (isCreateNoteRequest(trimmedPrompt)) {
      setLastRequest(trimmedPrompt);
      appendMessages([userMessage(trimmedPrompt)]);
      setQuestion('');
      setIsLoading(true);
      try {
        const preview = await previewCreateNote(trimmedPrompt);
        appendMessages([{ id: messageId(), preview, role: 'assistant' }]);
      } catch {
        appendMessages([assistantTextMessage('error', 'Note preview could not be loaded.')]);
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
    } catch {
      appendMessages([assistantTextMessage('error', 'Assistant response could not be loaded.')]);
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
      setHistoryError('AI action history could not be loaded.');
    }
  }

  async function applyPreview(messageIdToApply: string, preview: AssistantActionPreviewResponse) {
    if (applyingMessageId) {
      return;
    }
    setApplyingMessageId(messageIdToApply);
    try {
      const applied = await applyCreateNotePreview(preview.preview);
      setMessages((current) => current.filter((message) => message.id !== messageIdToApply));
      appendMessages([assistantTextMessage('status', `${applied.summary} It is now saved in Notes.`)]);
      await loadHistory();
    } catch {
      appendMessages([assistantTextMessage('error', 'Note change could not be applied.')]);
    } finally {
      setApplyingMessageId(null);
    }
  }

  function cancelPreview(messageIdToRemove: string) {
    setMessages((current) => current.filter((message) => message.id !== messageIdToRemove));
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

      <section className="assistant-thread" aria-label="Assistant conversation">
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
              <p>{record.entityType} {record.changeType}</p>
            </div>
            <time dateTime={record.createdAt}>{formatIsoDateTime(record.createdAt)}</time>
          </article>
        ))}
      </section>
    </div>
  );
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
    const isApplying = applyingMessageId === message.id;
    return (
      <article className="assistant-message assistant-message--assistant" aria-label="AI change preview">
        <p className="eyebrow">Preview</p>
        <h3>{message.preview.summary}</h3>
        <dl className="assistant-preview">
          <div>
            <dt>Title</dt>
            <dd>{message.preview.preview.title}</dd>
          </div>
          <div>
            <dt>Body</dt>
            <dd>{message.preview.preview.body}</dd>
          </div>
          <div>
            <dt>Tags</dt>
            <dd>{message.preview.preview.tags || 'No tags suggested'}</dd>
          </div>
        </dl>
        <div className="assistant-preview__actions">
          <Button
            disabled={isApplying}
            icon={<Check aria-hidden="true" size={16} />}
            onClick={() => onApplyPreview(message.preview)}
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

function messageId() {
  return globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`;
}
