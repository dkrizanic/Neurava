import { Bot, FilePlus2, RotateCcw, Send, X } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, Field, LoadingState } from '../../../shared/ui';
import { formatIsoDateTime } from '../../../shared/lib/dates';
import { answerQuestion, previewCreateNote, summarizeHistory } from '../api/assistantApi';
import type {
  AssistantMessage,
  AssistantMode,
  SourceReference,
  SummarySections,
} from '../types';

export function AssistantPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [mode, setMode] = useState<AssistantMode>('answer');
  const [question, setQuestion] = useState('');
  const [messages, setMessages] = useState<AssistantMessage[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [lastRequest, setLastRequest] = useState<{ mode: AssistantMode; text: string } | null>(null);
  const latestMessage = messages.at(-1);
  const canRetry = Boolean(
    lastRequest
    && latestMessage?.role === 'assistant'
    && 'type' in latestMessage
    && latestMessage.type === 'error',
  );

  async function submitQuestion(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await sendPrompt(mode, question);
  }

  async function sendPrompt(requestMode: AssistantMode, rawPrompt: string) {
    const trimmedPrompt = rawPrompt.trim();
    if (!trimmedPrompt) {
      setError('Question is required.');
      return;
    }

    setError(null);

    if (requestMode !== 'preview' && isAmbiguous(trimmedPrompt)) {
      appendMessages([
        userMessage(requestMode, trimmedPrompt),
        assistantTextMessage(requestMode, 'clarification', 'Tell me a little more so I can search the right notebook context. Try a topic, decision, person, or remembered phrase.'),
      ]);
      setQuestion(trimmedPrompt);
      return;
    }

    setLastRequest({ mode: requestMode, text: trimmedPrompt });
    appendMessages([userMessage(requestMode, trimmedPrompt)]);
    setQuestion('');
    setIsLoading(true);

    try {
      if (requestMode === 'answer') {
        const answer = await answerQuestion(trimmedPrompt);
        appendMessages([{ answer, id: messageId(), mode: 'answer', role: 'assistant' }]);
      } else if (requestMode === 'summary') {
        const summary = await summarizeHistory(trimmedPrompt);
        appendMessages([{ id: messageId(), mode: 'summary', role: 'assistant', summary }]);
      } else {
        const preview = await previewCreateNote(trimmedPrompt);
        appendMessages([{ id: messageId(), preview, role: 'assistant' }]);
      }
    } catch {
      appendMessages([assistantTextMessage(requestMode, 'error', errorMessageFor(requestMode))]);
    } finally {
      setIsLoading(false);
    }
  }

  function changeMode(nextMode: AssistantMode) {
    setMode(nextMode);
    setError(null);
  }

  async function retryLastRequest() {
    if (!lastRequest || isLoading) {
      return;
    }
    await sendPrompt(lastRequest.mode, lastRequest.text);
  }

  function appendMessages(nextMessages: AssistantMessage[]) {
    setMessages((current) => [...current, ...nextMessages]);
  }

  function removeMessage(messageIdToRemove: string) {
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
        <p>Ask questions, summarize history, and inspect source-backed responses in this session.</p>
      </header>

      <div className="assistant-mode" role="group" aria-label="Assistant mode">
        <Button onClick={() => changeMode('answer')} type="button" variant={mode === 'answer' ? 'primary' : 'secondary'}>
          Answer
        </Button>
        <Button onClick={() => changeMode('summary')} type="button" variant={mode === 'summary' ? 'primary' : 'secondary'}>
          Summary
        </Button>
        <Button onClick={() => changeMode('preview')} type="button" variant={mode === 'preview' ? 'primary' : 'secondary'}>
          Preview
        </Button>
      </div>

      <section className="assistant-thread" aria-label="Assistant conversation">
        {messages.length === 0 ? (
          <div className="assistant-thread__empty">
            <Bot aria-hidden="true" size={24} />
            <p>Start with a specific question or topic from the active workspace.</p>
          </div>
        ) : null}
        {messages.map((message) => (
          <AssistantMessageItem key={message.id} message={message} onCancelPreview={removeMessage} />
        ))}
        {isLoading ? <LoadingState label={loadingLabelFor(mode)} /> : null}
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
          label={inputLabelFor(mode)}
          maxLength={mode === 'preview' ? 4000 : 500}
          name="assistant-question"
          onChange={(event) => setQuestion(event.target.value)}
          placeholder={placeholderFor(mode)}
          value={question}
        />
        <Button disabled={isLoading} icon={<Send aria-hidden="true" size={18} />} type="submit" variant="primary">
          {buttonLabelFor(mode)}
        </Button>
      </form>
    </div>
  );
}

function AssistantMessageItem({
  message,
  onCancelPreview,
}: {
  message: AssistantMessage;
  onCancelPreview: (messageIdToRemove: string) => void;
}) {
  if (message.role === 'user') {
    return (
      <article className="assistant-message assistant-message--user">
        <p className="eyebrow">{inputLabelFor(message.mode)}</p>
        <p>{message.text}</p>
      </article>
    );
  }

  if ('type' in message) {
    return (
      <article className={`assistant-message assistant-message--${message.type}`}>
        <p className="eyebrow">{message.type === 'error' ? 'Error' : 'Clarifying question'}</p>
        <p>{message.text}</p>
      </article>
    );
  }

  if ('preview' in message) {
    return (
      <article className="assistant-message assistant-message--assistant" aria-label="AI change preview">
        <p className="eyebrow">Preview only</p>
        <h3>{message.preview.summary}</h3>
        <dl className="assistant-preview">
          <div>
            <dt>Entity</dt>
            <dd>{message.preview.entityType}</dd>
          </div>
          <div>
            <dt>Change</dt>
            <dd>{message.preview.changeType}</dd>
          </div>
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
          <Button disabled icon={<FilePlus2 aria-hidden="true" size={16} />} type="button" variant="secondary">
            Apply comes next
          </Button>
          <Button
            icon={<X aria-hidden="true" size={16} />}
            onClick={() => onCancelPreview(message.id)}
            type="button"
            variant="secondary"
          >
            Cancel
          </Button>
        </div>
      </article>
    );
  }

  if (message.mode === 'answer') {
    return (
      <article className="assistant-message assistant-message--assistant" aria-label="Source-aware answer">
        {message.answer.enoughSourceContext ? (
          <>
            <p className="eyebrow">Answer</p>
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

  return (
    <article className="assistant-message assistant-message--assistant" aria-label="History summary">
      {message.summary.enoughSourceContext ? (
        <>
          <p className="eyebrow">Summary</p>
          <SummarySectionList sections={message.summary.sections} />
          <SourceReferences sources={message.summary.sources} />
        </>
      ) : (
        <>
          <p className="eyebrow">Not enough source context</p>
          <SummarySectionList sections={message.summary.sections} />
        </>
      )}
    </article>
  );
}

function SummarySectionList({ sections }: { sections: SummarySections }) {
  return (
    <div className="summary-sections">
      <SummarySection items={sections.keyEvents} title="Key Events" />
      <SummarySection items={sections.decisions} title="Decisions" />
      <SummarySection items={sections.unresolvedItems} title="Unresolved Items" />
      <SummarySection items={sections.nextActions} title="Next Actions" />
    </div>
  );
}

function SummarySection({ items, title }: { items: string[]; title: string }) {
  return (
    <section>
      <h3>{title}</h3>
      <ul>
        {items.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </section>
  );
}

function SourceReferences({ sources }: { sources: SourceReference[] }) {
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

function userMessage(mode: AssistantMode, text: string): AssistantMessage {
  return { id: messageId(), mode, role: 'user', text };
}

function assistantTextMessage(mode: AssistantMode, type: 'clarification' | 'error', text: string): AssistantMessage {
  return { id: messageId(), mode, role: 'assistant', text, type };
}

function inputLabelFor(mode: AssistantMode) {
  if (mode === 'answer') {
    return 'Question';
  }
  if (mode === 'summary') {
    return 'Summary topic';
  }
  return 'Note preview input';
}

function placeholderFor(mode: AssistantMode) {
  if (mode === 'answer') {
    return 'What did we decide about the API problem?';
  }
  if (mode === 'summary') {
    return 'Summarize API planning';
  }
  return 'Paste messy notes or a rough idea to preview a clean note';
}

function buttonLabelFor(mode: AssistantMode) {
  if (mode === 'answer') {
    return 'Ask assistant';
  }
  if (mode === 'summary') {
    return 'Summarize history';
  }
  return 'Preview note';
}

function loadingLabelFor(mode: AssistantMode) {
  if (mode === 'answer') {
    return 'Preparing source-aware answer';
  }
  if (mode === 'summary') {
    return 'Preparing history summary';
  }
  return 'Preparing note preview';
}

function errorMessageFor(mode: AssistantMode) {
  if (mode === 'answer') {
    return 'Assistant answer could not be loaded.';
  }
  if (mode === 'summary') {
    return 'History summary could not be loaded.';
  }
  return 'AI change preview could not be loaded.';
}

function messageId() {
  return globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`;
}
